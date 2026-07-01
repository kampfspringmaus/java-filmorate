package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.FilmErrorMessages;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.Mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    private static final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
    private final String commonErrorText = "Ошибка при добавлении фильма: %s %s";
    private final String successfulCreation = "информация о фильме %s добавлена: %s";
    private final String successfulUpdate = "информация о фильме %s изменена. Новые данные: %s";

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaDbStorage mpaStorage, GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public List<FilmResponse> getAll() {
        return filmStorage.getAll().stream()
                .map(film -> {
                    Mpa mpa = mpaStorage.get(film.getMpaId());
                    Collection<Genre> genres = genreStorage.getFilmGenres(film.getGenres());
                    Set<Integer> likes = filmStorage.getLikes(film.getId());
                    return FilmMapper.mapToFilmResponse(film, mpa, genres, likes);
                })
                .sorted(Comparator.comparingInt(FilmResponse::getId)) // сортируем по ID
                .collect(Collectors.toList()); // используем List, чтобы порядок был стабильным
    }

    public FilmResponse get(Integer filmId) {
        Film film = filmStorage.get(filmId);
        Mpa mpa = mpaStorage.get(film.getMpaId());
        Collection<Genre> genres = genreStorage.getFilmGenres(film.getGenres());
        Set<Integer> likes = filmStorage.getLikes(filmId);
        return FilmMapper.mapToFilmResponse(film, mpa, genres, likes);
    }

    public FilmResponse create(NewFilmRequest request) {
        if (request.nameIsBlank()) {
            log.info(String.format(commonErrorText, request, FilmErrorMessages.emptyFilmName));
            throw new ConditionsNotMetException(FilmErrorMessages.emptyFilmName);
        }
        if (request.lengthMoreThan200()) {
            log.info(String.format(commonErrorText, request, FilmErrorMessages.tooLongDescription));
            throw new ConditionsNotMetException(FilmErrorMessages.tooLongDescription);
        }
        if (request.badReleaseDate(firstFilmDate)) {
            log.info(String.format(commonErrorText, request, FilmErrorMessages.tooOldFilm));
            throw new ConditionsNotMetException(FilmErrorMessages.tooOldFilm);
        }
        if (request.durationIsNotPositive()) {
            log.info(String.format(commonErrorText, request, FilmErrorMessages.negativeFilmDuration));
            throw new ConditionsNotMetException(FilmErrorMessages.negativeFilmDuration);
        }
        Film film = filmStorage.create(FilmMapper.mapToFilm(request));
        Mpa mpa = mpaStorage.get(film.getMpaId());
        Collection<Genre> genres = genreStorage.getFilmGenres(film.getGenres());
        Set<Integer> likes = filmStorage.getLikes(film.getId());
        return FilmMapper.mapToFilmResponse(film, mpa, genres, likes);
    }

    public FilmResponse update(UpdateFilmRequest request) {
        Film requestForUpdate = FilmMapper.mapToFilm(request);
        Film updatedFilm = filmStorage.get(requestForUpdate.getId());
        if (requestForUpdate.getName() != null && !requestForUpdate.getName().isBlank()) {
            updatedFilm.setName(requestForUpdate.getName());
        }
        if (requestForUpdate.getDescription() != null && requestForUpdate.getDescription().length() < 200) {
            updatedFilm.setDescription(requestForUpdate.getDescription());
        }

        if (requestForUpdate.getReleaseDate() != null && requestForUpdate.getReleaseDate().isAfter(firstFilmDate)) {
            updatedFilm.setReleaseDate(requestForUpdate.getReleaseDate());
        }
        if (requestForUpdate.getDuration() != null && requestForUpdate.getDuration() > 0) {
            updatedFilm.setDuration(requestForUpdate.getDuration());
        }
        if (requestForUpdate.getGenres() != null) {
            updatedFilm.setGenres(requestForUpdate.getGenres());
        }
        if (requestForUpdate.getMpaId() != null) {
            updatedFilm.setMpaId(requestForUpdate.getMpaId());
        }
        updatedFilm = filmStorage.update(updatedFilm);
        Mpa mpa = mpaStorage.get(updatedFilm.getMpaId());
        Collection<Genre> genres = genreStorage.getFilmGenres(updatedFilm.getGenres());
        Set<Integer> likes = filmStorage.getLikes(updatedFilm.getId());
        return FilmMapper.mapToFilmResponse(updatedFilm, mpa, genres, likes);


    }

    public FilmResponse putLike(Integer filmId, Integer userId) {
        if (!filmStorage.filmIsPresent(filmId)) {
            throw new NotFoundException("Нет такого фильма");
        }

        if (!userStorage.userIsPresent(userId)) {
            throw new NotFoundException("Нет такого пользователя");
        }

        Film result = filmStorage.addLike(filmId, userId);
        Mpa mpa = mpaStorage.get(result.getMpaId());
        Collection<Genre> genres = genreStorage.getFilmGenres(result.getGenres());
        Set<Integer> likes = filmStorage.getLikes(filmId);
        return FilmMapper.mapToFilmResponse(result, mpa, genres, likes);
    }

    public FilmResponse cancelLike(Integer filmId, Integer userId) {
        if (!filmStorage.filmIsPresent(filmId)) {
            throw new NotFoundException("Нет такого фильма");
        }

        if (!userStorage.userIsPresent(userId)) {
            throw new NotFoundException("Нет такого пользователя");
        }

        Film result = filmStorage.cancelLike(filmId, userId);
        Mpa mpa = mpaStorage.get(result.getMpaId());
        Collection<Genre> genres = genreStorage.getFilmGenres(result.getGenres());
        Set<Integer> likes = filmStorage.getLikes(filmId);
        return FilmMapper.mapToFilmResponse(result, mpa, genres, likes);
    }

    public List<FilmResponse> getTopRatedFilms(Integer count) {
        List<FilmResponse> result = filmStorage.getTopRated(count).stream()
                //.sorted(Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder()))
                //.limit(count)
                .map(film -> {
                    Mpa mpa = mpaStorage.get(film.getMpaId());
                    Collection<Genre> genres = genreStorage.getFilmGenres(film.getGenres());
                    Set<Integer> likes = filmStorage.getLikes(film.getId());
                    return FilmMapper.mapToFilmResponse(film, mpa, genres, likes);
                })
                .collect(Collectors.toList());
        return result;
    }
}

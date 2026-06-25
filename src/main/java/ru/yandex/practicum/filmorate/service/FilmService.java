package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.FilmErrorMessages;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final static LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
    private final String commonErrorText = "Ошибка при добавлении фильма: %s %s";
    private final String successfulCreation = "информация о фильме %s добавлена: %s";
    private final String successfulUpdate = "информация о фильме %s изменена. Новые данные: %s";

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<FilmResponse> getAll() {
        Collection<Film> films = filmStorage.getAll();
        Collection<FilmResponse> result = films.stream()
                .map(film -> FilmMapper.mapToFilmResponse(film))
                .collect(Collectors.toSet());
        return result;


    }

    public FilmResponse get(Integer filmId) {
        Film film = filmStorage.get(filmId);
        return FilmMapper.mapToFilmResponse(film);
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
        if (request.DurationIsNotPositive()) {
            log.info(String.format(commonErrorText, request, FilmErrorMessages.negativeFilmDuration));
            throw new ConditionsNotMetException(FilmErrorMessages.negativeFilmDuration);
        }
        Film film = filmStorage.create(FilmMapper.mapToFilm(request));
        return FilmMapper.mapToFilmResponse(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film putLike(Integer filmId, Integer userId) {
        if (!filmStorage.filmIsPresent(filmId)) {
            throw new NotFoundException("Нет такого фильма");
        }

        if (!userStorage.userIsPresent(userId)) {
            throw new NotFoundException("Нет такого пользователя");
        }

        Film result = filmStorage.get(filmId);
        result.getLikes().add(userId);
        return result;
    }

    public Film cancelLike(Integer filmId, Integer userId) {
        if (!filmStorage.filmIsPresent(filmId)) {
            throw new NotFoundException("Нет такого фильма");
        }

        if (!userStorage.userIsPresent(userId)) {
            throw new NotFoundException("Нет такого пользователя");
        }

        Film result = filmStorage.get(filmId);
        result.getLikes().remove(userId);
        return result;
    }

    public Collection<Film> getTopRatedFilms(Integer count) {
        Collection<Film> result = filmStorage.getAll().stream()
                .sorted(Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
        return result;
    }
}

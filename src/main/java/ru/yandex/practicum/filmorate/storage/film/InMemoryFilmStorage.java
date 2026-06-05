package ru.yandex.practicum.filmorate.storage.film;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.FilmErrorMessages;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Data
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);

    private final String commonErrorText = "Ошибка при добавлении фильма: %s %s";
    private final String successfulCreation = "информация о фильме %s добавлена: %s";
    private final String successfulUpdate = "информация о фильме %s изменена. Новые данные: %s";

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film create(@RequestBody Film film) {
        if (!checkNameBlank(film)) {
            log.info(String.format(commonErrorText, film, FilmErrorMessages.emptyFilmName));
            throw new ConditionsNotMetException(FilmErrorMessages.emptyFilmName);
        }
        if (!checkDescriptionLength(film)) {
            log.info(String.format(commonErrorText, film, FilmErrorMessages.tooLongDescription));
            throw new ConditionsNotMetException(FilmErrorMessages.tooLongDescription);
        }
        if (!checkReleaseDate(film)) {
            log.info(String.format(commonErrorText, film, FilmErrorMessages.tooOldFilm));
            throw new ConditionsNotMetException(FilmErrorMessages.tooOldFilm);
        }
        if (!checkDuration(film)) {
            log.info(String.format(commonErrorText, film, FilmErrorMessages.negativeFilmDuration));
            throw new ConditionsNotMetException(FilmErrorMessages.negativeFilmDuration);
        }
        int filmId = getNextId();
        film.setId(filmId);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        films.put(filmId, film);
        log.info(String.format(successfulCreation, film.getId(), film));
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.info(String.format(FilmErrorMessages.filmNotFound, film.getId()));
            throw new NotFoundException(String.format(FilmErrorMessages.filmNotFound, film.getId()));
        } else if (film.getName() == null && film.getDuration() == null && film.getReleaseDate() == null
                && film.getDescription() == null) {
            return films.get(film.getId());
        } else {
            Film oldFilmData = films.get(film.getId());

            if (checkNameBlank(film)) {
                oldFilmData.setName(film.getName());
            }
            if (checkDescriptionLength(film)) {
                oldFilmData.setName(film.getName());
            }
            if (checkReleaseDate(film)) {
                oldFilmData.setReleaseDate(film.getReleaseDate());
            }
            if (checkDuration(film)) {
                oldFilmData.setDuration(film.getDuration());
            }
            log.info(String.format(successfulUpdate, oldFilmData.getId(), oldFilmData));
            return film;
        }
    }

    @Override
    public boolean filmIsPresent(Integer filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public Film get(Integer filmId) {
        return films.get(filmId);
    }

    private boolean checkNameBlank(Film film) {
        return !film.getName().isBlank();
    }

    private boolean checkDescriptionLength(Film film) {
        return Optional.ofNullable(film.getDescription()).map((String::length)).orElse(0) <= 200;
    }

    private boolean checkReleaseDate(Film film) {
        return film.getReleaseDate().isAfter(firstFilmDate);
    }

    private boolean checkDuration(Film film) {
        return film.getDuration() > 0;
    }

    private int getNextId() {
        int maxId = films.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        return ++maxId;
    }

}

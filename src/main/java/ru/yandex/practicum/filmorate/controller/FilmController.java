package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
    private String error;

    @PostMapping
    public Film create(@RequestBody Film film) {
        //final String COMMON_ERROR_TEXT = "Ошибка при добавлении фильма: " + film.toString() + " ";
        if (!checkNameBlank(film)) {
            error = "Название фильма не может быть пустым";
            log.info(getErrorText(film) + error);
            throw new ConditionsNotMetException(error);
        }
        if (!checkDescriptionLength(film)) {
            error = "Слишком длинное название фильма";
            log.info(getErrorText(film) + error);
            throw new ConditionsNotMetException(error);
        }
        if (!checkReleaseDate(film)) {
            error = "Дата выхода фильма превосходит всё известное археологам";
            log.info(getErrorText(film) + error);
            throw new ConditionsNotMetException(error);
        }
        if (!checkDuration(film)) {
            error = "Длительность фильма не может быть отрицательной";
            log.info(getErrorText(film) + error);
            throw new ConditionsNotMetException(error);
        }
        int filmId = getNextId();
        film.setId(filmId);
        films.put(filmId, film);
        String result = "информация о фильме " + film.getId() + "добавлена: " + film;
        log.info(result);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            error = "Фильм с id " + film.getId() + " не найден";
            log.info(error);
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        } else if (film.getName() == null && film.getDuration() == null && film.getReleaseDate() == null
        && film.getDescription() == null) {
            return films.get(film.getId());
        } else {
            Film oldFilmData = films.get(film.getId());

            if (!checkNameBlank(film)) {
                film.setName(oldFilmData.getName());
            }
            if (!checkDescriptionLength(film)) {
                film.setName(oldFilmData.getName());
            }
            if (!checkReleaseDate(film)) {
                film.setReleaseDate(oldFilmData.getReleaseDate());
            }
            if (!checkDuration(film)) {
                film.setDuration(oldFilmData.getDuration());
            }
            films.put(film.getId(), film);
            String result = "информация о фильме " + film.getId() + "изменена. предыдущие данные: " + oldFilmData.toString() +
                    " новые данные: " + film;
            log.info(result);
            return film;
        }
    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
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

    private String getErrorText(Film film) {
        return "Ошибка при добавлении фильма: " + film.toString() + " ";
    }
}

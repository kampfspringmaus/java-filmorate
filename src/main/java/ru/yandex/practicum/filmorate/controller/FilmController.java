package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film create(@RequestBody Film film) {

        if (!checkNameBlank(film)) {
            throw new ConditionsNotMetException("Название фильма не может быть пустым");
        }
        if (!checkNameLength(film)) {
            throw new ConditionsNotMetException("Слишком длинное название фильма");
        }
        if (!checkReleaseDate(film)) {
            throw new ConditionsNotMetException("Дата выхода фильма превосходит всё известное археологам");
        }
        if (!checkDuration(film)) {
            throw new ConditionsNotMetException("Длительность фильма не может быть отрицательной");
        }
        int filmId = getNextId();
        film.setId(filmId);
        films.put(filmId, film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
       if (!films.keySet().contains(film.getId())) {
           throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
       } else {
           Film oldFilmData = films.get(film.getId());

           if (!checkNameBlank(film)) {
               film.setName(oldFilmData.getName());
           }
           if (!checkNameLength(film)) {
               film.setName(oldFilmData.getName());
           }
           if (!checkReleaseDate(film)) {
               film.setReleaseDate(oldFilmData.getReleaseDate());
           }
           if (!checkDuration(film)) {
               film.setDuration(oldFilmData.getDuration());
           }
           films.put(film.getId(), film);
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

    private boolean checkNameLength(Film film) {
        return film.getName().length() <= 200;
    }

    private boolean checkReleaseDate(Film film) {
        return film.getReleaseDate().isBefore(FIRST_FILM_DATE);
    }

    private boolean checkDuration(Film film) {
        return film.getDuration().isPositive();
    }

    private int getNextId() {
        int maxId = films.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        return ++maxId;
    }
}

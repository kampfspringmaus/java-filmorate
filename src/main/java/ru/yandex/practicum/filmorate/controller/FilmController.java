package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@RequestBody Film film) {

    }

    @PutMapping
    public Film update(@RequestBody Film film) {

    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();

    }
/*
    название не может быть пустым;
    максимальная длина описания — 200 символов;
    дата релиза — не раньше 28 декабря 1895 года;
    продолжительность фильма должна быть положительным числом.*/

    private int getNextId() {
        int maxId = films.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        return ++maxId;
    }
}

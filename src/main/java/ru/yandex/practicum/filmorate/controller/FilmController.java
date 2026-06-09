package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
   // FilmStorage inMemoryFilmStorage;
    FilmService filmService;

    @Autowired
    public FilmController( FilmService filmService) {//FilmStorage filmStorage,
        //this.inMemoryFilmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        return filmService.update(film);
    }
    //PUT /films/{id}/like/{userId}
    @PutMapping("/{id}/like/{userId}")
    public Film putLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.putLike(filmId, userId);
    }

    //DELETE /films/{id}/like/{userId}
    @DeleteMapping("/{id}/like/{userId}")
    public Film cancelLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.cancelLike(filmId, userId);
    }

    //GET /films/popular?count={count}
    @GetMapping("/popular")
    public Collection<Film> getTopRatedFilms(@RequestParam(defaultValue = "10") Integer count) {
        return filmService.getTopRatedFilms(count);
    }

}

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
    FilmStorage inMemoryFilmStorage;
    FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.inMemoryFilmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return inMemoryFilmStorage.getAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return inMemoryFilmStorage.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        return inMemoryFilmStorage.update(film);
    }

    /* @PutMapping("/Likes")
    public Film likeAction(@RequestParam Integer filmId, @RequestParam Integer userId,
                           @RequestParam LikeActions action) {
        return filmService.likeAction(filmId, userId, action);

    }*/


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

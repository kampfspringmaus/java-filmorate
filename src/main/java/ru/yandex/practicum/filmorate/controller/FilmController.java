package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<FilmResponse> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public FilmResponse getFilm(@PathVariable("id") Integer filmId) {
        return filmService.get(filmId);
    }

    @PostMapping
    public FilmResponse create(@RequestBody NewFilmRequest request) {
        return filmService.create(request);
    }

    @PutMapping
    public FilmResponse update(@RequestBody UpdateFilmRequest request) {
        return filmService.update(request);
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

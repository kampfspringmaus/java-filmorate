package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
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

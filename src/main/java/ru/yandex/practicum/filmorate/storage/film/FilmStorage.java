package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    boolean filmIsPresent(Integer filmId);

    Film get(Integer filmId);

    Film addLike(Integer filmId, Integer userId);

    Film cancelLike(Integer filmId, Integer userId);

    Set<Integer> getLikes(Integer filmId);
}


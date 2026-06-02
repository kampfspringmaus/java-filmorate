package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.WrongArgumentException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    public Film likeAction(Integer filmId, Integer userId, LikeActions action) {
        if (!filmStorage.filmIsPresent(filmId)) {
            throw new WrongArgumentException("Нет такого фильма");
        }

        if (!userStorage.userIsPresent(userId)) {
            throw new WrongArgumentException("Нет такого пользователя");
        }

        Film result = filmStorage.get(filmId);
        if (action == LikeActions.ADD) {
            result.getLikes().add(userId);
        } else if (action == LikeActions.REMOVE) {
            result.getLikes().remove(userId);
        } else {
            throw new WrongArgumentException("Неизвестная команда действия с like`ом");
        }

        return result;

    }

    public Collection<Film> getTopRatedFilms() {
        Collection<Film> result = filmStorage.getAll().stream()
                .sorted(Comparator.comparing(film -> film.getLikes().size()))
                .limit(10)
                .collect(Collectors.toList());

        return result;
    }


}

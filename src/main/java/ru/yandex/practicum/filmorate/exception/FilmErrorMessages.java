package ru.yandex.practicum.filmorate.exception;

public class FilmErrorMessages {
    public static final String EMPTY_NAME = "Название фильма не может быть пустым";
    public static final String TOO_LONG_DESCRIPTION = "Слишком длинное описание фильма";
    public static final String TOO_OLD_FILM = "Дата выхода фильма превосходит всё известное археологам";
    public static final String NEGATIVE_FILM_DURATION = "Длительность фильма не может быть отрицательной";
    public static final String FILM_NOT_FOUND = "Фильм с id %s не найден";
}

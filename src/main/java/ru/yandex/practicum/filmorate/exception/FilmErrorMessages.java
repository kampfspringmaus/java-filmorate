package ru.yandex.practicum.filmorate.exception;

public class FilmErrorMessages {
    public static final String emptyFilmName = "Название фильма не может быть пустым";
    public static final String tooLongDescription = "Слишком длинное описание фильма";
    public static final String tooOldFilm = "Дата выхода фильма превосходит всё известное археологам";
    public static final String negativeFilmDuration = "Длительность фильма не может быть отрицательной";
    public static final String filmNotFound = "Фильм с id %s не найден";
}

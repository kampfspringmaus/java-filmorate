package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
public class NewFilmRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    // Mpa может отсутствовать (null), если клиент его не прислал
    private MpaDto mpa;

    // Genres может отсутствовать или быть пустым списком
    private List<GenreDto> genres;

    public boolean nameIsBlank() {
        return name.isBlank();
    }

    public boolean lengthMoreThan200() {
        return !(Optional.ofNullable(description.length()).orElse(0) <= 200);
    }

    public boolean badReleaseDate(LocalDate date) {
        return releaseDate.isBefore(date);
    }

    public boolean DurationIsNotPositive() {
        return duration <= 0;
    }

}


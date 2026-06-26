package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
public class UpdateFilmRequest {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
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

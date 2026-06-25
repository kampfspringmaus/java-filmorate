package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmResponse {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    // Mpa может отсутствовать (null), если клиент его не прислал
    private MpaDto mpa;

    // Genres может отсутствовать или быть пустым списком
    private List<GenreDto> genres;
}

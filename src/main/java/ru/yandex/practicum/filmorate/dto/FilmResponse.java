package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Data
public class FilmResponse {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Mpa mpa;
   private Collection<Genre> genres;
    private Set<Integer> likes;
}

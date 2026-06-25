package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FilmMapper {
    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        if (request.getGenres() != null) {
            Set<Integer> genres = request.getGenres().stream()
                            .map(genreDto -> genreDto.getId()).collect(Collectors.toSet());

            film.setGenres(genres);
        }
        if (request.getMpa() != null) {
            film.setMpaId(request.getMpa().getId());
        }
     //написать для MPA
        return film;
    }

    public static FilmResponse mapToFilmResponse (Film film) {
        FilmResponse response = new FilmResponse();
        response.setId(film.getId());
        response.setName(film.getName());
        response.setDescription(film.getDescription());
        response.setReleaseDate(film.getReleaseDate());
        response.setDuration(film.getDuration());
        MpaDto mpaDto = new MpaDto();
        mpaDto.setId(film.getMpaId());
        response.setMpa(mpaDto);
        List<GenreDto> dtoList = film.getGenres().stream()
                        .map(id -> {
                            GenreDto dto = new GenreDto();
                            dto.setId(id);
                            return dto;
                        }).collect(Collectors.toList());

        response.setGenres(dtoList);
        return response;
    }

}



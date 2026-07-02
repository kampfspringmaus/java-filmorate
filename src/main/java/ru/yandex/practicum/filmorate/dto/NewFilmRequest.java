package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.FilmErrorMessages;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
public class NewFilmRequest {
    @NotBlank(message = FilmErrorMessages.emptyFilmName)
    private String name;
    @Size(max = 200, message = FilmErrorMessages.tooLongDescription)
    private String description;
    @NotNull
    @PastOrPresent(message = FilmErrorMessages.FilmFromTheFuture)
    private LocalDate releaseDate;
    @Positive(message = FilmErrorMessages.negativeFilmDuration)
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

    public boolean durationIsNotPositive() {
        return duration <= 0;
    }

}


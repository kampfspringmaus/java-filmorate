package ru.yandex.practicum.filmorate.storage.genre;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
@Data
@Slf4j
@Component
public class GenreDbStorage {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<Genre> mapperGenre;
    private static final String FIND_ALL_GENRE_QUERY = "SELECT * FROM genre order by genre_id";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT * FROM genre where genre_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapperGenre) {
        this.jdbc = jdbc;
        this.mapperGenre = mapperGenre;
    }
    public Collection<Genre> getAll() {
        return jdbc.query(FIND_ALL_GENRE_QUERY, mapperGenre);
    }

    public Genre get(Integer mpaId) {
        try {
            Genre result = jdbc.queryForObject(FIND_GENRE_BY_ID_QUERY, mapperGenre, mpaId);
            return result;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("жанр не найден");
        }
    }
}

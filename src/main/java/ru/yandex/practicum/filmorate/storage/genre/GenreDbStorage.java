package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    public Genre get(Integer genreId) {
        try {
            Genre result = jdbc.queryForObject(FIND_GENRE_BY_ID_QUERY, mapperGenre, genreId);
            return result;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("жанр не найден");
        }
    }

    public Collection<Genre> getFilmGenres(Set<Integer> genres) {
        if (genres == null || genres.isEmpty()) {
            return Collections.emptyList();
        }

        // Формируем строку из "?, ?, ?" под количество ID
        String placeholders = String.join(",", Collections.nCopies(genres.size(), "?"));
        String sql = "SELECT * FROM genre WHERE genre_id IN (" + placeholders + ") ORDER BY genre_id";

        List<Genre> result = jdbc.query(sql, mapperGenre, genres.toArray(new Integer[0]));

        return result;

        /*   return genres.stream()
                .map(genreId -> get(genreId))
                .collect(Collectors.toSet());*/
    }

}

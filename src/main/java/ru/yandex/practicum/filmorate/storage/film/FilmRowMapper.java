package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("film_id"));
        film.setName(resultSet.getString("film_name"));
        film.setDescription(resultSet.getString("film_description"));
        Timestamp ts = resultSet.getTimestamp("film_releaseDate");
        if (ts != null) {
            film.setReleaseDate(ts.toLocalDateTime().toLocalDate());
        }
        film.setDuration(resultSet.getInt("film_duration"));
        film.setMpaId(resultSet.getInt("film_rating_id"));
        return film;
    }
}

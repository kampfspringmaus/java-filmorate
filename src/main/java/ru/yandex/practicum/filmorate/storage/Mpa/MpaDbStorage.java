package ru.yandex.practicum.filmorate.storage.Mpa;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Data
@Slf4j
@Component
public class MpaDbStorage {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<Mpa> mapperMpa;
    private static final String FIND_ALL_MPA_QUERY = "SELECT * FROM mpa_rating order by rating_id";
    private static final String FIND_MPA_BY_ID_QUERY = "SELECT * FROM mpa_rating where rating_id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapperMpa) {
        this.jdbc = jdbc;
        this.mapperMpa = mapperMpa;
    }

    public Collection<Mpa> getAll() {
        return jdbc.query(FIND_ALL_MPA_QUERY, mapperMpa);
    }

    public Mpa get(Integer mpaId) {
        try {
            Mpa result = jdbc.queryForObject(FIND_MPA_BY_ID_QUERY, mapperMpa, mpaId);
            return result;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("рейтинг не найден");
        }
    }


}

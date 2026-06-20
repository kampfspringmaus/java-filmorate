package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

public class UserDbStorage implements UserStorage {

    protected final JdbcTemplate jdbc;
    protected final RowMapper<User> mapper;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String CREATE_USER_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?) returning id";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ?" +
            "  WHERE id = ?";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users where user_id = ?";

    public Collection<User> getAll() {
        return jdbc.query(FIND_ALL_USERS_QUERY, mapper);
    }


    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);
        user.setId(id);
        return user;
    }

    public User update(User user) {
        int rowsUpdated = jdbc.update(UPDATE_USER_QUERY, user.getEmail(),user.getLogin(),user.getName(),user.getBirthday());
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return user;
    }

    public boolean userIsPresent(Integer userId) {
        try {
            User result = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, mapper);
            return true;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }

    public User get(Integer userId) {
        try {
        User result = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, mapper);
        return result;
    } catch (EmptyResultDataAccessException ignored) {
        throw new NotFoundException("Пользователь не найден");
    }
}
}

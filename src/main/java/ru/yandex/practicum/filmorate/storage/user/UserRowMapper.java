package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setEmail(resultSet.getString("user_email"));
        user.setLogin(resultSet.getString("user_login"));
        user.setName(resultSet.getString("user_name"));
        Timestamp ts = resultSet.getTimestamp("user_birthday");
if (ts != null) {
    user.setBirthday(ts.toLocalDateTime().toLocalDate());
} else {
    user.setBirthday(null);
}

return user;

    }

}

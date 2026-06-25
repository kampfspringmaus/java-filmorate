package ru.yandex.practicum.filmorate.storage.friendsList;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FriendsList;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendsRowMapper implements RowMapper<FriendsList> {
    @Override
    public FriendsList mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        FriendsList fr = new FriendsList();
        fr.setFriend_id(resultSet.getInt("friend_id"));
        fr.setFriendship_status(resultSet.getString("status_description"));
        return fr;
    }
}


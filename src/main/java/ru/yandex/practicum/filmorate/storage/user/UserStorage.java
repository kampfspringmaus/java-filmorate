package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    Collection<User> getAll();

    User create(User user);

    User update(User user);

    boolean userIsPresent(Integer userId);

    User get(Integer userId);

    User addFriend(Integer user1, Integer user2);

    User deleteFriend(Integer userId, Integer friendId);

    User confirmFriendship(Integer user1, Integer user2);

    boolean checkFriendship(Integer user1, Integer user2);

    List<User>  getFriendList(Integer userId);

    List<User>  getCommonFriends(Integer user1, Integer user2);
}

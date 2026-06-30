package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongArgumentException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;


@Service
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    /*public User create(User user) {
        return userStorage.create(user);
    }*/

    public User create(NewUserRequest request) {
        return userStorage.create(UserMapper.mapToUser(request));
    }

    public User update(UpdateUserRequest request) {
        User user = userStorage.get(request.getId());
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        user = UserMapper.updateUserFields(user, request);
        userStorage.update(user);
        return user;
    }



   /* public User update(User user) {
        return userStorage.update(user);
    }*/

    public User addFriend(Integer user1, Integer user2) {
        if (user1.equals(user2)) {
            throw new WrongArgumentException("Пользователь не может добавить в друзья сам себя");
        }
        if (user1 == null || user2 == null) {
            throw new WrongArgumentException("Значение ID пользователя не может быть пустым");
        }
        if (!userStorage.userIsPresent(user1) || !userStorage.userIsPresent(user2)) {
            throw new NotFoundException("как минимум один из пользователей не существует");
        }
        if (user1 <= 0 || user2 <= 0) {
            throw new WrongArgumentException("ID пользователя должно быть положительным числом");
        }
        if (userStorage.checkFriendship(user1, user2)) {
            return userStorage.get(user1);
        }
        User result = userStorage.addFriend(user1, user2);
        if (userStorage.checkFriendship(user2, user1)) {
            userStorage.confirmFriendship(user2, user1);
        }
        return result;
    }

    public User removeFriend(Integer user1, Integer user2) {
        if (user1.equals(user2)) {
            throw new WrongArgumentException("Пользователь не может удалить из друзей сам себя");
        }
        if (user1 == null || user2 == null) {
            throw new WrongArgumentException("Значение ID пользователя не может быть пустым");
        }
        if (!userStorage.userIsPresent(user1) || !userStorage.userIsPresent(user2)) {
            throw new NotFoundException("как минимум один из пользователей не существует");
        }
        if (user1 <= 0 || user2 <= 0) {
            throw new WrongArgumentException("ID пользователя должно быть положительным числом");
        }
        //userStorage.get(user1).getFriendsList().remove(user2);
        //userStorage.get(user2).getFriendsList().remove(user1);
        return userStorage.deleteFriend(user1, user2);

    }

    public Collection<User> getFriendsList(Integer userId) {
        if (!userStorage.userIsPresent(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }


        return userStorage.getFriendList(userId);
    }

    public Collection<User> getCommonFriends(Integer user1, Integer user2) {


        return userStorage.getCommonFriends(user1, user2);
    }
}

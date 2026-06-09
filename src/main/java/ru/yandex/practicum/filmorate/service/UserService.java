package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongArgumentException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

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
        userStorage.get(user1).getFriendsList().add(user2);
        userStorage.get(user2).getFriendsList().add(user1);
        return userStorage.get(user1);

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
        userStorage.get(user1).getFriendsList().remove(user2);
        userStorage.get(user2).getFriendsList().remove(user1);
        return userStorage.get(user1);

    }

    public Collection<User> getFriendsList(Integer user) {
        if (!userStorage.userIsPresent(user)) {
            throw new NotFoundException("Пользователь не найден");
        }

        Set<Integer> userFriends = userStorage.get(user).getFriendsList();

        Collection<User> result = new ArrayList<>();
        userFriends.forEach(userId -> {
            User user1 = userStorage.get(userId);
            if (user1 != null) {
                result.add(user1);
            }
        });
        return result;
    }

    public Collection<User> getCommonFriends(Integer user1, Integer user2) {


        Set<Integer> user1Friends = userStorage.get(user1).getFriendsList();
        Set<Integer> user2Friends = userStorage.get(user2).getFriendsList();
        Set<Integer> commonFriends = user1Friends.stream()
                .filter(user2Friends::contains)
                .collect(Collectors.toSet());

        Collection<User> result = new ArrayList<>();
        commonFriends.forEach(userId -> {
            User user = userStorage.get(userId);
            if (user != null) {
                result.add(user);
            }
        });
        return result;
    }
}

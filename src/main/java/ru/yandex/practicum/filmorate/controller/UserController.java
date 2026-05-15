package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User create(@RequestBody User user) {
    /*
    электронная почта не может быть пустой и должна содержать символ @;
логин не может быть пустым и содержать пробелы;
имя для отображения может быть пустым — в таком случае будет использован логин;
дата рождения не может быть в будущем.*/
        if (!checkEmail(user)) {
            throw new ConditionsNotMetException("Почта не может быть пустой и должна содержать знак \"@\"");
        }
        if (!checkLogin(user)) {
            throw new ConditionsNotMetException("логин не может быть пустым и содержать пробелы");
        }
        if (!checkName(user)) {
            user.setName(user.getLogin());
        }
        if (!checkBirthday(user)) {
            throw new ConditionsNotMetException("дата рождения не может быть в будущем");
        }
        int userId = getNextId();
        user.setId(userId);
        users.put(userId, user);
        return user;


    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (!users.keySet().contains(user.getId())) {
            throw new NotFoundException("пользователь с id " + user.getId() + " не найден");
        } else {

        }
return null;
    }

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }


    private int getNextId() {
        int maxId = users.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        return ++maxId;
    }

    private boolean checkEmail(User user) {
        return user.getEmail().contains("@") && !user.getEmail().isEmpty();

    }

    private boolean checkLogin(User user) {
        return !user.getLogin().isEmpty() && !user.getLogin().contains(" ");

    }

    private boolean checkName(User user) {
        return !user.getName().isBlank();
    }

    private boolean checkBirthday(User user) {
        return user.getBirthday().isBefore(LocalDate.now());

    }

}

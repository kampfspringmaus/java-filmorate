package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private InMemoryUserStorage inMemoryUserStorage;
    /*private final Map<Integer, User> users = new HashMap<>();
    private final String commonErrorText = "Ошибка при добавлении пользователя: %s %s";
    private final String successfulCreation = "информация о пользователе %s добавлена: %s";
    private final String successfulUpdate = "информация о пользователе %s изменена. Новые данные: %s";
    private final String userNotFound = "пользователь с id %s не найден";
*/
    @GetMapping
    public Collection<User> getAll() {
        return inMemoryUserStorage.getAll();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return inMemoryUserStorage.create(user);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        return inMemoryUserStorage.update(user);
    }



/*    private int getNextId() {
        int maxId = users.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        return ++maxId;
    }

    private boolean checkEmail(User user) {
        return user.getEmail().contains("@") && !user.getEmail().isEmpty();
    }


    private boolean checkLogin(User user) {
        return !user.getLogin().isEmpty() && !user.getLogin().contains(" ");
    }

    private boolean checkName(User user) {
        if (user.getName() == null) {
            return false;
        }
        return !user.getName().isBlank();
    }

    private boolean checkBirthday(User user) {
        return user.getBirthday().isBefore(LocalDate.now());
    }*/
}

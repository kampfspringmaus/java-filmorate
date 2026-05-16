package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User create(@RequestBody User user) {
        final String COMMON_ERROR_TEXT = "Ошибка при добавлении пользователя: " + user.toString() + " ";

        if (!checkEmail(user)) {
            String error = "Почта не может быть пустой и должна содержать знак \"@\"";
            log.info(COMMON_ERROR_TEXT + error);
            throw new ConditionsNotMetException(error);
        }
        if (!checkLogin(user)) {
            String error = "логин не может быть пустым или содержать пробелы";
            log.info(COMMON_ERROR_TEXT + error);
            throw new ConditionsNotMetException(error);
        }
        if (!checkName(user)) {
            log.info("У пользователя " + user.toString() + " пустое имя. Вместо имени будет подставлен логин");
            user.setName(user.getLogin());
        }
        if (!checkBirthday(user)) {
            String error = "дата рождения не может быть в будущем";
            log.info(COMMON_ERROR_TEXT + error);
            throw new ConditionsNotMetException(error);
        }
        int userId = getNextId();
        user.setId(userId);
        users.put(userId, user);
        String result = "информация о пользователе " + user.getId() + "добавлена: " + user.toString();
        log.info(result);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (!users.keySet().contains(user.getId())) {
            String error = "пользователь с id " + user.getId() + " не найден";
            log.info(error);
            throw new NotFoundException("пользователь с id " + user.getId() + " не найден");
        } else {
            User oldUserData = users.get(user.getId());
            if (!checkEmail(user)) {
                user.setEmail(oldUserData.getEmail());
            }
            if (!checkLogin(user)) {
                user.setLogin(oldUserData.getLogin());
            }
            if (!checkName(user)) {
                user.setName(user.getLogin());
            }
            if (!checkBirthday(user)) {
                user.setBirthday(oldUserData.getBirthday());
            }
            users.put(user.getId(), user);
            String result = "информация о пользователе " + user.getId() + "изменена. предыдущие данные: " + oldUserData.toString() +
                    " новые данные: " + user.toString();
            log.info(result);
            return user;
        }
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
        if (user.getName() == null) {
            return false;
        }
        if (user.getName().isBlank()) {
            return false;
        }
        return true;
    }

    private boolean checkBirthday(User user) {
        return user.getBirthday().isBefore(LocalDate.now());
    }
}

package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserErrorMessages;
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
    private final String COMMON_ERROR_TEXT = "Ошибка при добавлении пользователя: %s %s";
    private final String SUCCESSFUL_CREATION = "информация о пользователе %s добавлена: %s";
    private final String SUCESSFUL_UPDATE = "информация о пользователе %s изменена. Новые данные: %s";
    private final String USER_NOT_FOUND = "пользователь с id %s не найден";

    @PostMapping
    public User create(@RequestBody User user) {

        if (!checkEmail(user)) {
            log.info(String.format(COMMON_ERROR_TEXT, user, UserErrorMessages.BLANK_OR_WRONG_EMAIL));
            throw new ConditionsNotMetException(UserErrorMessages.BLANK_OR_WRONG_EMAIL);
        }
        if (!checkLogin(user)) {
            log.info(String.format(COMMON_ERROR_TEXT, user, UserErrorMessages.EMPTY_OR_SPACES_LOGIN));
            throw new ConditionsNotMetException(UserErrorMessages.EMPTY_OR_SPACES_LOGIN);
        }
        if (!checkName(user)) {
            log.info("У пользователя " + user + " пустое имя. Вместо имени будет подставлен логин");
            user.setName(user.getLogin());
        }
        if (!checkBirthday(user)) {
            log.info(String.format(COMMON_ERROR_TEXT, user, UserErrorMessages.BIRTHDAY_IN_FUTURE));
            throw new ConditionsNotMetException(UserErrorMessages.BIRTHDAY_IN_FUTURE);
        }
        int userId = getNextId();
        user.setId(userId);
        users.put(userId, user);
        String result = "информация о пользователе " + user.getId() + "добавлена: " + user;
        log.info(String.format(SUCCESSFUL_CREATION, user.getId(), user));
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            log.info(String.format(USER_NOT_FOUND, user.getId()));
            throw new NotFoundException(String.format(USER_NOT_FOUND, user.getId()));
        } else if (user.getEmail() == null && user.getLogin() == null && user.getName() == null
                && user.getBirthday() == null) {
            return users.get(user.getId());
        } else {
            User oldUserData = users.get(user.getId());
            if (checkEmail(user)) {
                oldUserData.setEmail(user.getEmail());
            }
            if (checkLogin(user)) {
                oldUserData.setLogin(user.getLogin());
            }
            if (checkName(user)) {
                oldUserData.setName(user.getLogin());
            }
            if (checkBirthday(user)) {
                oldUserData.setBirthday(user.getBirthday());
            }
            log.info(String.format(SUCESSFUL_UPDATE, user.getId(), user));
            return user;
        }
    }

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    private int getNextId() {
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
    }
}

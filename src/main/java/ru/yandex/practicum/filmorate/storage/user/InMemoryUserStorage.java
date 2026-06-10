package ru.yandex.practicum.filmorate.storage.user;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserErrorMessages;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Data
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final String commonErrorText = "Ошибка при добавлении пользователя: %s %s";
    private final String successfulCreation = "информация о пользователе %s добавлена: %s";
    private final String successfulUpdate = "информация о пользователе %s изменена. Новые данные: %s";
    private final String userNotFound = "пользователь с id %s не найден";

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User create(User user) {

        if (!checkEmail(user)) {
            log.info(String.format(commonErrorText, user, UserErrorMessages.blankOrWrongEmail));
            throw new ConditionsNotMetException(UserErrorMessages.blankOrWrongEmail);
        }
        if (!checkLogin(user)) {
            log.info(String.format(commonErrorText, user, UserErrorMessages.emptyOrSpacesLogin));
            throw new ConditionsNotMetException(UserErrorMessages.emptyOrSpacesLogin);
        }
        if (!checkName(user)) {
            log.info("У пользователя " + user + " пустое имя. Вместо имени будет подставлен логин");
            user.setName(user.getLogin());
        }
        if (!checkBirthday(user)) {
            log.info(String.format(commonErrorText, user, UserErrorMessages.birthdayInFuture));
            throw new ConditionsNotMetException(UserErrorMessages.birthdayInFuture);
        }
        int userId = getNextId();
        user.setId(userId);
        if (user.getFriendsList() == null) {
            user.setFriendsList(new HashSet<>());
        }
        users.put(userId, user);
        String result = "информация о пользователе " + user.getId() + "добавлена: " + user;
        log.info(String.format(successfulCreation, user.getId(), user));
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.info(String.format(userNotFound, user.getId()));
            throw new NotFoundException(String.format(userNotFound, user.getId()));
        }
        User oldUserData = users.get(user.getId());
        if (user.getEmail() != null) {
            if (checkEmail(user)) {
                oldUserData.setEmail(user.getEmail());
            }
        }
        if (user.getLogin() != null) {
            if (checkLogin(user)) {
                oldUserData.setLogin(user.getLogin());
            }
        }
        if (user.getName() != null) {
            if (checkName(user)) {
                oldUserData.setName(user.getLogin());
            } else {
                oldUserData.setName(user.getName());
            }
        }
        if (user.getBirthday() != null) {
            if (checkBirthday(user)) {
                oldUserData.setBirthday(user.getBirthday());
            }
        }

            log.info(String.format(successfulUpdate, user.getId(), user));
            return user;

    }

    @Override
    public boolean userIsPresent(Integer userId) {
        return users.containsKey(userId);
    }

    @Override
    public User get(Integer userId) {
        return users.get(userId);
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

package ru.yandex.practicum.filmorate.storage.user;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserErrorMessages;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Slf4j
@Component
public class UserDbStorage implements UserStorage {

    protected final JdbcTemplate jdbc;
    protected final RowMapper<User> mapper;


    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }


    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String CREATE_USER_QUERY = "INSERT INTO users(user_email, user_login, user_name, user_birthday)" +
            "VALUES (?, ?, ?, ?)";// RETURNING user_id
    private static final String UPDATE_USER_QUERY = "UPDATE users SET user_email = ?, user_login = ?, user_name = ?, user_birthday = ?" +
            "  WHERE user_id = ?";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users where user_id = ?";
    private static final String FIND_FRIENDS_BY_USER_ID = "SELECT friend_id from friendships where user_id = ?";
    private static final String CREATE_USER_FRIEND = "INSERT into friendships (user_id, friend_id, status_id) VALUES " +
            "(?, ?, 0)";
    private static final String DELETE_USER_FRIEND = "DELETE FROM friendships where user_id = ? and friend_id = ?";
    private static final String CHECK_FRIENDSHIP_QUERY = "SELECT count(*) from friendships where user_id = ? and friend_id = ?";
    private static final String CONFIRM_FRIENDSHIP_QUERY = "UPDATE friendships SET status_id = 1 where user_id = ? and " +
            "friend_id = ?";
    private static final String GET_FRIENDLIST_BY_USER_ID = "SELECT friend_id FROM friendships where user_id = ?";
    private static final String GET_COMMON_FRIENDS = "SELECT friend_id FROM friendships where user_id = ? and friend_id in" +
            "(select friend_id from friendships where user_id = ?)";
    private final String commonErrorText = "Ошибка при добавлении пользователя: %s %s";

            /*"select fr.friend_id as friend_id, fs.status_description " +
            "as status_description from friendships fr \n join friendship_status fs on (fr.status_id=fs.status_id)\n" +
            "    where user_id = ?";*/

    //private static final String FIND
    public Collection<User> getAll() {
        return jdbc.query(FIND_ALL_USERS_QUERY, mapper);
    }


    public User create(User user) {
        if (badEmail(user)) {
            log.info(String.format(commonErrorText, user, UserErrorMessages.blankOrWrongEmail));
            throw new ConditionsNotMetException(UserErrorMessages.blankOrWrongEmail);
        }
        if (badLogin(user)) {
            log.info(String.format(commonErrorText, user, UserErrorMessages.emptyOrSpacesLogin));
            throw new ConditionsNotMetException(UserErrorMessages.emptyOrSpacesLogin);
        }
        if (badName(user)) {
            log.info("У пользователя " + user + " пустое имя. Вместо имени будет подставлен логин");
            user.setName(user.getLogin());
        }
        if (badBirthday(user)) {
            log.info(String.format(commonErrorText, user, UserErrorMessages.birthdayInFuture));
            throw new ConditionsNotMetException(UserErrorMessages.birthdayInFuture);
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsInserted = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3, user.getName());
            ps.setObject(4, user.getBirthday());
            //System.out.println( user.getEmail()+' '+user.getLogin()+' '+user.getName()+' '+user.getBirthday());
            return ps;
        }, keyHolder);

        if (rowsInserted == 0) {
            throw new InternalServerException("Не удалось создать пользователя");
        }

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id == null) {
            throw new InternalServerException("Не удалось получить ID созданного пользователя");
        }
        user.setId(id);
        return user;
    }

    public User update(User user) {
        int rowsUpdated = jdbc.update(UPDATE_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return user;
    }

    public boolean userIsPresent(Integer userId) {
        try {
            User result = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, mapper, userId);
            return true;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }

    public User get(Integer userId) {
        try {
            User result = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, mapper, userId);
            Set<Integer> friends = jdbc.queryForList(FIND_FRIENDS_BY_USER_ID, Integer.class, userId
            ).stream().collect(Collectors.toSet());
            result.setFriendsList(friends);

            return result;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public User addFriend(Integer userId, Integer friendId) {
        if (!userIsPresent(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!userIsPresent(friendId)) {
            throw new NotFoundException("Друг не найден");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsInserted = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_USER_FRIEND, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, userId);
            ps.setObject(2, friendId);
            //System.out.println( user.getEmail()+' '+user.getLogin()+' '+user.getName()+' '+user.getBirthday());
            return ps;
        }, keyHolder);

        if (rowsInserted == 0) {
            throw new InternalServerException("Этот пользователь уже добавлен в друзья");
        }
        return get(userId);
    }

    @Override
    public User deleteFriend(Integer userId, Integer friendId) {
        if (!userIsPresent(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!userIsPresent(friendId)) {
            throw new NotFoundException("Друг не найден");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsDeleted = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(DELETE_USER_FRIEND, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, userId);
            ps.setObject(2, friendId);
            //System.out.println( user.getEmail()+' '+user.getLogin()+' '+user.getName()+' '+user.getBirthday());
            return ps;
        }, keyHolder);

        if (rowsDeleted == 0) {

            log.info(String.format("Пользователи с id %s и %s не являются друзьями", userId, friendId));
            // throw new InternalServerException("Эти люди не являются друзьями");
        }
        return get(userId);
    }

    @Override
    public User confirmFriendship(Integer user1, Integer user2) {
        int rowsUpdated = jdbc.update(CONFIRM_FRIENDSHIP_QUERY, user1, user2);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные о дружбе");
        }
        return get(user1);
    }

    @Override
    public boolean checkFriendship(Integer user1, Integer user2) {
        Integer isFriend = jdbc.queryForObject(CHECK_FRIENDSHIP_QUERY, Integer.class, user1, user2);
        return isFriend > 0;
    }

    @Override
    public List<User>  getFriendList(Integer userId) {
        List<User> result = jdbc.queryForList(GET_FRIENDLIST_BY_USER_ID, Integer.class, userId)
                .stream()
                .map(this::get)
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<User>  getCommonFriends(Integer user1, Integer user2) {
        List<User>  result = jdbc.queryForList(GET_COMMON_FRIENDS, Integer.class, user1, user2)
                .stream()
                .map(this::get)
                .collect(Collectors.toList());
        return result;
    }

    private boolean badEmail(User user) {
        return !user.getEmail().contains("@") || user.getEmail().isEmpty();
    }

    private boolean badLogin(User user) {
        return user.getLogin().isEmpty() || user.getLogin().contains(" ");
    }

    private boolean badName(User user) {
        if (user.getName() == null) {
            return true;
        }
        return user.getName().isBlank();
    }

    private boolean badBirthday(User user) {
        return user.getBirthday().isAfter(LocalDate.now());
    }
}

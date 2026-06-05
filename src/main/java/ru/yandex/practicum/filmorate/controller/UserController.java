package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private UserStorage inMemoryUserStorage;
    private UserService userService;

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
   // PUT /users/{id}/friends/{friendId}
    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id") Integer user1, @PathVariable("friendId") Integer user2 ) {
        return userService.addFriend(user1, user2);
    }
    //DELETE /users/{id}/friends/{friendId}
    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable("id") Integer user1, @PathVariable("friendId") Integer user2){
        return userService.removeFriend(user1, user2);
    }

    //GET /users/{id}/friends
    @GetMapping("/users/{id}/friends")
    public Collection<User> getFriendsList (@PathVariable("id") Integer userId) {
        return userService.getFriendsList(userId);
    }
    //GET /users/{id}/friends/common/{otherId}
@GetMapping("/users/{id}/friends/common/{otherId}")
public Collection<User> getCommonFriends(@PathVariable("id") Integer user1,
                                         @PathVariable("otherId") Integer user2) {
        return userService.getCommonFriends(user1,user2);
}
}

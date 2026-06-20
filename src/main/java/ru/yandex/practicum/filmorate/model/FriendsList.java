package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
public class FriendsList {
    Map<String,String> friendsList = new HashMap<>();

    public void getAll() {

    }

    public void getConfirmed() {

    }

    public void getPending() {

    }
}

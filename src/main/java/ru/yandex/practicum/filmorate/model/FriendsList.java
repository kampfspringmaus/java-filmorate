package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
public class FriendsList {
   private Integer friend_id;
   private String friendship_status;
           private Map<Integer,String> friendsList = new HashMap<>();
}

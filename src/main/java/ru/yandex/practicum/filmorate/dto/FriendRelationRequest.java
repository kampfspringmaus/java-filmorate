package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class FriendRelationRequest {
    private Integer user_id;
    private Integer friend_id;
}

package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.UserErrorMessages;
import java.time.LocalDate;

@Data
public class NewUserRequest {
    @NotBlank(message = UserErrorMessages.blankOrWrongEmail)
    @Pattern(regexp = ".*@.*", message = UserErrorMessages.blankOrWrongEmail)
    private String email;
    @NotBlank(message = UserErrorMessages.birthdayInFuture)
    private String login;
    private String name;
    @PastOrPresent(message = UserErrorMessages.birthdayInFuture)
    private LocalDate birthday;

   /* private boolean checkEmail(User user) {
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

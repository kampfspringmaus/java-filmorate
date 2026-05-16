package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;


@SpringBootTest
class FilmorateApplicationTests {
    private static final String BASE = "http://localhost:8080";
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() throws Exception {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        FilmorateApplication fa = new FilmorateApplication();
        String[] args = {};
        fa.main(args);
    }

    @Test
    void contextLoads() {

    }

    @Test
    @Order(1)
    void createFilmIsSuccessful() {
        Film film1 = Film.builder()
                .name("Миска")
                .description("Фильм о миске и том, что было дальше")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(45))
                .build();
        Gson gson = new Gson();
        String filmBody = gson.toJson(film1);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .POST(HttpRequest.BodyPublishers.ofString(filmBody))
                .build();
        try {
            HttpResponse<String> response = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(200, response.statusCode(), "Создание фильма с валидными" +
                    "параметрами должно проходить успешно");

        } catch (IOException | InterruptedException e) {
            System.out.println("createFilmIsSuccessful" + e.getMessage());
        }
    }


    @Test
    @Order(2)
    void createUserIsSuccessful() {
        User user1 = User.builder()
                .email("p.antipov@yandex.practicum")
                .login("p.antipov")
                .name("Pavlik")
                .birthday(LocalDate.of(1989, 03, 11))
                .build();
        Gson gson = new Gson();
        String userBody = gson.toJson(user1);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .POST(HttpRequest.BodyPublishers.ofString(userBody))
                .build();
        try {
            HttpResponse<String> response = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(200, response.statusCode(), "Создание пользователя с валидными" +
                    "параметрами должно проходить успешно");
        } catch (IOException | InterruptedException e) {
            System.out.println("createUserIsSuccessful" + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void getFilmsReturn1Film() {
        Gson gson = new Gson();
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .GET()
                .build();

        try {
            HttpResponse<String> response2 = client.send(req2,
                    HttpResponse.BodyHandlers.ofString());
            JsonArray filmArray = JsonParser.parseString(response2.body()).getAsJsonArray();
            List<Film> filmList = gson.fromJson(filmArray, List.class);
            Assertions.assertEquals(200, response2.statusCode(), "Запрос списка " +
                    "пользователей должен возвращать код 200");
            Assertions.assertEquals("Миска", filmList.get(0).getName(),
                    "У первого элемента массива название фильма - Миска");
        } catch (IOException | InterruptedException e) {
            System.out.println("getFilmsReturn1Film" + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void getUsersReturn2Users() {
        User user2 = User.builder()
                .email("p.antipov@yandex.pathca")
                .login("p.antipovs")
                .name("Paul")
                .birthday(LocalDate.of(1989, 07, 21))
                .build();
        Gson gson = new Gson();
        String userBody = gson.toJson(user2);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .POST(HttpRequest.BodyPublishers.ofString(userBody))
                .build();
        try {
            HttpResponse<String> response = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("getUsersReturn2Users Ошибка тут" + e.getMessage());
        }
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .GET()
                .build();

        try {
            HttpResponse<String> response2 = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
            JsonArray userArray = JsonParser.parseString(response2.body()).getAsJsonArray();
            List<User> userList = gson.fromJson(userArray, List.class);
            Assertions.assertEquals(200, response2.statusCode(), "Запрос списка " +
                    "пользователей должен возвращать код 200");
            Assertions.assertEquals("p.antipov@yandex.pathca", userList.get(1).getEmail(),
                    "У второго элемента массива должен email p.antipov@yandex.pathca");

        } catch (IOException | InterruptedException e) {
            System.out.println("getUsersReturn2Users или тут" + e.getMessage());
        }
    }

    @Test
    void updateNonExistentFilmReturns500() {
        Film film1 = Film.builder()
                .id(1000)
                .name("Миска")
                .description("Фильм о миске и том, что было дальше")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(45))
                .build();
        Gson gson = new Gson();
        String filmBody = gson.toJson(film1);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(filmBody))
                .build();
        try {
            HttpResponse<String> response = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(500, response.statusCode(), "Обновление несуществующего фильма" +
                    " должно возвращать код 500");

        } catch (IOException | InterruptedException e) {
            System.out.println("updateNonExistentFilmReturns500" + e.getMessage());
        }
    }

    @Test
    void updateNonExistentUserReturns500() {
        User user1 = User.builder()
                .id(1000)
                .email("p.antipov@yandex.practicum")
                .login("p.antipov")
                .name("Pavlik")
                .birthday(LocalDate.of(1989, 03, 11))
                .build();
        Gson gson = new Gson();
        String userBody = gson.toJson(user1);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(userBody))
                .build();
        try {
            HttpResponse<String> response = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(500, response.statusCode(), "Обновление несуществующего пользователя" +
                    " должно возвращать код 500");
        } catch (IOException | InterruptedException e) {
            System.out.println("updateNonExistentUserReturns500" + e.getMessage());
        }

    }

    @Test
    void createFilmWithReleaseYear1870Returns500() {
        Film film1 = Film.builder()
                .name("Миска")
                .description("Фильм о миске и том, что было дальше")
                .releaseDate(LocalDate.of(1870, 01, 01))
                .duration(Duration.ofMinutes(45))
                .build();
        Gson gson = new Gson();
        String filmBody = gson.toJson(film1);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .POST(HttpRequest.BodyPublishers.ofString(filmBody))
                .build();
        try {
            HttpResponse<String> response = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(500, response.statusCode(), "Создание фильма " +
                    "с датой выхода 1870 год должно возвращать код 500");

        } catch (IOException | InterruptedException e) {
            System.out.println("createFilmWithReleaseYear1870Returns500" + e.getMessage());
        }
    }

    @Test
    void createUserWithWrongEmailReturns500() {
        User user1 = User.builder()
                .id(1000)
                .email("p.antipov")
                .login("p.antipov")
                .name("Pavlik")
                .birthday(LocalDate.of(1989, 03, 11))
                .build();
        Gson gson = new Gson();
        String userBody = gson.toJson(user1);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .POST(HttpRequest.BodyPublishers.ofString(userBody))
                .build();
        try {
            HttpResponse<String> response = client.send(req,
                    HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(500, response.statusCode(), "Создание пользователя " +
                    "с невалидным email должно возвращать код 500");
        } catch (IOException | InterruptedException e) {
            System.out.println("createUserWithWrongEmailReturns500" + e.getMessage());
        }
    }

    @Test
    void postRequestWithNoBodyReturns400() {
        Gson gson = new Gson();
        String reqBody = gson.toJson("");
        HttpRequest reqFilms = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        HttpRequest reqUsers = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        try {
            HttpResponse<String> responseFilms = client.send(reqFilms,
                    HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> responseUsers = client.send(reqUsers,
                    HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(400, responseFilms.statusCode(), "При пустом запросе " +
                    "должен возвращаться код 400\"Bad Request\"");
            Assertions.assertEquals(400, responseUsers.statusCode(), "При пустом запросе " +
                    "должен возвращаться код 400\"Bad Request\"");
        } catch (IOException | InterruptedException e) {

            System.out.println("postRequestWithNoBodyReturns400 " + e.getMessage());
        }
    }

}

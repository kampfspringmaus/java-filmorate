package ru.yandex.practicum.filmorate;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
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

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {


    private static final String BASE = "http://localhost:8080";
    private static HttpClient client;
    private AssertionError lastAssertionError;
    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString())).registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString())).setPrettyPrinting().create();
    private final static String SuccessTestPreText = "Тест выполнился успешно ";

    @BeforeAll
    static void beforeAll() throws Exception {
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @AfterEach
    void logFailures() {
        if (lastAssertionError != null) {
            log.error("Ошибка в тесте: ", lastAssertionError);
        }
    }

    @Test
    @Order(1)
    void createFilmIsSuccessful() throws IOException, InterruptedException {
        Film film1 = Film.builder().name("Миска").description("Фильм о миске и том, что было дальше").releaseDate(LocalDate.of(1970, 01, 01)).duration(45).build();
        //Gson gson = new Gson();
        log.info("тест пройден");

        String filmBody = gson.toJson(film1);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/films")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(filmBody)).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode(), "Создание фильма с валидными" + "параметрами должно проходить успешно");
        log.info(SuccessTestPreText + "createFilmIsSuccessful");
    }

    @Test
    @Order(2)
    void createUserIsSuccessful() throws IOException, InterruptedException {
        User user1 = User.builder().email("p.antipov@yandex.practicum").login("p.antipov").name("Pavlik").birthday(LocalDate.of(1989, 03, 11)).build();
        String userBody = gson.toJson(user1);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/users")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(userBody)).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode(), "Создание пользователя с валидными" + "параметрами должно проходить успешно");
        log.info(SuccessTestPreText + "createUserIsSuccessful");
    }

    @Test
    @Order(3)
    void getFilmsReturn1Film() throws IOException, InterruptedException {
        HttpRequest req2 = HttpRequest.newBuilder().uri(URI.create(BASE + "/films")).GET().build();

        HttpResponse<String> response2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
        JsonArray filmArray = JsonParser.parseString(response2.body()).getAsJsonArray();
        List<Film> filmList = gson.fromJson(filmArray, new ListOfFilmsTypeToken().getType());
        Assertions.assertEquals(200, response2.statusCode(), "Запрос списка " + "пользователей должен возвращать код 200");
        Assertions.assertEquals("Миска", filmList.get(0).getName(), "У первого элемента массива название фильма - Миска");
        log.info(SuccessTestPreText + "getFilmsReturn1Film");
    }

    @Test
    @Order(4)
    void getUsersReturn2Users() throws IOException, InterruptedException {
        User user2 = User.builder().email("p.antipov@yandex.pathca").login("p.antipovs").name("Paul").birthday(LocalDate.of(1989, 07, 21)).build();
        String userBody = gson.toJson(user2);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/users")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(userBody)).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

        HttpRequest req2 = HttpRequest.newBuilder().uri(URI.create(BASE + "/users")).GET().build();

        HttpResponse<String> response2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
        JsonArray userArray = JsonParser.parseString(response2.body()).getAsJsonArray();
        List<User> userList = gson.fromJson(userArray, new ListOfUsersTypeToken().getType());
        Assertions.assertEquals(200, response2.statusCode(), "Запрос списка " + "пользователей должен возвращать код 200");
        Assertions.assertEquals("p.antipov@yandex.pathca", userList.get(0).getEmail(), "У первого элемента массива должен email p.antipov@yandex.pathca");
        log.info(SuccessTestPreText + "getUsersReturn2Users");
    }

    @Test
    void updateNonExistentFilmReturns500() throws IOException, InterruptedException {
        Film film1 = Film.builder().id(1000).name("Миска").description("Фильм о миске и том, что было дальше").releaseDate(LocalDate.now()).duration(45).build();
        String filmBody = gson.toJson(film1);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/films")).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(filmBody)).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode(), "Обновление несуществующего фильма" + " должно возвращать код 500");
        log.info(SuccessTestPreText + "updateNonExistentFilmReturns500");
    }

    @Test
    void updateNonExistentUserReturns500() throws IOException, InterruptedException {
        User user1 = User.builder().id(1000).email("p.antipov@yandex.practicum").login("p.antipov").name("Pavlik").birthday(LocalDate.of(1989, 03, 11)).build();
        String userBody = gson.toJson(user1);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/users")).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(userBody)).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode(), "Обновление несуществующего пользователя" + " должно возвращать код 500");
        log.info(SuccessTestPreText + "updateNonExistentUserReturns500");
    }

    @Test
    void createFilmWithReleaseYear1870Returns500() throws IOException, InterruptedException {
        Film film1 = Film.builder().name("Миска").description("Фильм о миске и том, что было дальше").releaseDate(LocalDate.of(1870, 01, 01)).duration(45).build();
        String filmBody = gson.toJson(film1);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/films")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(filmBody)).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode(), "Создание фильма " + "с датой выхода 1870 год должно возвращать код 500");
        log.info(SuccessTestPreText + "createFilmWithReleaseYear1870Returns500");
    }

    @Test
    void createUserWithWrongEmailReturns500() throws IOException, InterruptedException {
        User user1 = User.builder().id(1000).email("p.antipov").login("p.antipov").name("Pavlik").birthday(LocalDate.of(1989, 03, 11)).build();
        String userBody = gson.toJson(user1);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/users")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(userBody)).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode(), "Создание пользователя " + "с невалидным email должно возвращать код 500");
        log.info(SuccessTestPreText + "createUserWithWrongEmailReturns500");
    }

    @Test
    void postRequestWithNoBodyReturns400() throws IOException, InterruptedException {
        String reqBody = gson.toJson("");
        HttpRequest reqFilms = HttpRequest.newBuilder().uri(URI.create(BASE + "/films")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(reqBody)).build();
        HttpRequest reqUsers = HttpRequest.newBuilder().uri(URI.create(BASE + "/users")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(reqBody)).build();

        HttpResponse<String> responseFilms = client.send(reqFilms, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> responseUsers = client.send(reqUsers, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, responseFilms.statusCode(), "При пустом запросе " + "должен возвращаться код 400\"Bad Request\"");
        Assertions.assertEquals(400, responseUsers.statusCode(), "При пустом запросе " + "должен возвращаться код 400\"Bad Request\"");
        log.info(SuccessTestPreText + "postRequestWithNoBodyReturns400");
    }

}

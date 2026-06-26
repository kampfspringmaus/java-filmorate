package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongArgumentException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<Film> mapper;
    protected final RowMapper<Mpa> mapperMpa;
    protected final RowMapper<Genre> mapperGenre;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, RowMapper<Mpa> mapperMpa, RowMapper<Genre> mapperGenre) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.mapperMpa = mapperMpa;
        this.mapperGenre = mapperGenre;
    }

    private static final String FIND_ALL_FILMS_QUERY = "SELECT film_id FROM films";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films where film_id = ?";
    private static final String FIND_FILM_GENRES_BY_FILM_ID = "SELECT genre_id from genre where genre_id in (select film_genre_id" +
            " from  films_genres where film_id = ?)";
    private static final String FIND_FILM_MPA_BY_FILM_ID = "SELECT * FROM association_rating where rating_id = " +
            "(select film_rating_id FROM films WHERE film_id = ?)";
    private static final String FIND_FILM_LIKES_BY_FILM_ID = "SELECT user_id FROM like_lists where film_id = ?";
    private static final String CREATE_FILM_QUERY = "INSERT INTO films (film_name, film_description, film_releasedate, " +
            "film_duration, film_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String CREATE_FILM_GENRES_QUERY = "INSERT INTO films_genres (film_id, film_genre_id) VALUES (?, ?)";
    private static final String CHECK_MPA_EXISTANCE_QUERY = "SELECT COUNT(*) FROM association_rating where rating_id = ?";
    private static final String CHECK_GENRE_EXISTANCE_QUERY = "SELECT COUNT(*) FROM genre where genre_id = ?";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET film_name = ?, film_description = ?, film_releaseDate = ?," +
            "film_duration = ?, film_rating_id = ? WHERE film_id = ?";
    private static final String DELETE_FILMS_GENRES = "DELETE from films_genres where film_id = ?";





    public Collection<Film> getAll() {


        Set<Integer> filmIds = jdbc.queryForList(FIND_ALL_FILMS_QUERY, Integer.class
        ).stream().collect(Collectors.toSet());
        Collection<Film> result = filmIds.stream()
                .map(filmId -> get(filmId))
                .collect(Collectors.toSet());

        return result;
    }

    public Film create(Film film) {
        int mpaExistance =jdbc.queryForObject(CHECK_MPA_EXISTANCE_QUERY, Integer.class, film.getMpaId());
        if (mpaExistance == 0) {
            throw new NotFoundException("Такого рейтинга не существует");
        }

        film.getGenres().stream()
                .forEach(genre_id -> {
                            int genreExistance = jdbc.queryForObject(CHECK_GENRE_EXISTANCE_QUERY, Integer.class, genre_id);
                            if (genreExistance == 0) {
                                throw new NotFoundException("Такого жанра не существует");
                            }
                });
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsInserted = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_FILM_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4, film.getDuration());
            ps.setObject(5, film.getMpaId());
            //System.out.println( user.getEmail()+' '+user.getLogin()+' '+user.getName()+' '+user.getBirthday());
            return ps;
        }, keyHolder);
        if (rowsInserted == 0) {
            throw new InternalServerException("Не удалось создать фильм");
        }

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id == null) {
            throw new InternalServerException("Не удалось получить ID созданного фильма");
        }
        film.setId(id);

        if (film.getGenres() != null) {
            film.getGenres().stream()
                    .forEach(genre_id ->
                            jdbc.update(CREATE_FILM_GENRES_QUERY, film.getId(), genre_id)
                    );

        }

        return film;
    }


    public Film update(Film film) {
        int rowsUpdated = jdbc.update(UPDATE_FILM_QUERY, film.getName(), film.getDescription(),film.getReleaseDate(),
                film.getDuration(), film.getMpaId(), film.getId());


        if (film.getGenres() != null) {
            int rowsDeleted = jdbc.update(DELETE_FILMS_GENRES, film.getId());
            film.getGenres().stream()
                    .forEach(genre_id ->
                            jdbc.update(CREATE_FILM_GENRES_QUERY, film.getId(), genre_id)
                    );

        }
        return film;
    }


    public boolean filmIsPresent(Integer filmId) {
        try {
            Film result = jdbc.queryForObject(FIND_FILM_BY_ID_QUERY, mapper, filmId);
            return true;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }

    public Film get(Integer filmId) {
        try {
            Film result = jdbc.queryForObject(FIND_FILM_BY_ID_QUERY, mapper, filmId);
            result.setGenres(new HashSet<>(jdbc.queryForList(FIND_FILM_GENRES_BY_FILM_ID, Integer.class, filmId)));

            //result.setMpa(mpa);

            Set<Integer> likes = jdbc.queryForList(FIND_FILM_LIKES_BY_FILM_ID, Integer.class, filmId
            ).stream().collect(Collectors.toSet());
            result.setLikes(likes);


            return result;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Фильм не найден");
        }

    }
}

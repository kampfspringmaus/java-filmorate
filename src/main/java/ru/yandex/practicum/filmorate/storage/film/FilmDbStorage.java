package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Statement;
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
    private static final String FIND_ALL_FILMS_QUERY = "SELECT film_id FROM films";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films where film_id = ?";
    private static final String FIND_FILM_GENRES_BY_FILM_ID = "SELECT genre_id from genre where genre_id in (select film_genre_id" +
            " from  films_genres where film_id = ?)";
    private static final String FIND_FILM_MPA_BY_FILM_ID = "SELECT * FROM mpa_rating where rating_id = " +
            "(select film_rating_id FROM films WHERE film_id = ?)";
    private static final String FIND_FILM_LIKES_BY_FILM_ID = "SELECT user_id FROM like_lists where film_id = ?";
    private static final String CREATE_FILM_QUERY = "INSERT INTO films (film_name, film_description, film_releasedate, " +
            "film_duration, film_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String CREATE_FILM_GENRES_QUERY = "INSERT INTO films_genres (film_id, film_genre_id) VALUES (?, ?)";
    private static final String CHECK_FILM_EXISTANCE_QUERY = "SELECT COUNT(*) FROM films where film_id = ?";
    private static final String CHECK_MPA_EXISTANCE_QUERY = "SELECT COUNT(*) FROM mpa_rating where rating_id = ?";
    private static final String CHECK_GENRE_EXISTANCE_QUERY = "SELECT COUNT(*) FROM genre where genre_id = ?";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET film_name = ?, film_description = ?, film_releaseDate = ?," +
            "film_duration = ?, film_rating_id = ? WHERE film_id = ?";
    private static final String DELETE_FILMS_GENRES = "DELETE from films_genres where film_id = ?";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO like_lists (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM like_lists WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_BY_FILM_ID = "SELECT user_id FROM like_lists WHERE film_id = ?";
    private static final String FIND_TOP_RATED_FILMS = "    SELECT f.film_id" +
            "    FROM films f" +
            "    JOIN (" +
            "          SELECT film_id, count(*) as likes" +
            "           FROM like_lists" +
            "           GROUP BY film_id" +
            "           ORDER BY COUNT(*) DESC" +
            "           LIMIT ?" +
            "            ) fl ON f.film_id = fl.film_id " +
            "    ORDER BY likes desc";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, RowMapper<Mpa> mapperMpa, RowMapper<Genre> mapperGenre) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.mapperMpa = mapperMpa;
        this.mapperGenre = mapperGenre;
    }


    @Override
    public Collection<Film> getAll() {
        Set<Integer> filmIds = jdbc.queryForList(FIND_ALL_FILMS_QUERY, Integer.class
        ).stream().collect(Collectors.toSet());
        Collection<Film> result = filmIds.stream()
                .map(filmId -> get(filmId))
                .collect(Collectors.toSet());
        return result;
    }

    @Override
    public Film create(Film film) {
        int mpaExistance = jdbc.queryForObject(CHECK_MPA_EXISTANCE_QUERY, Integer.class, film.getMpaId());
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

    @Override
    public Film update(Film film) {
        int rowsUpdated = jdbc.update(UPDATE_FILM_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(),
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

    @Override
    public boolean filmIsPresent(Integer filmId) {
        int result = jdbc.queryForObject(CHECK_FILM_EXISTANCE_QUERY, Integer.class, filmId);
        return result > 0;
    }

    @Override
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

    @Override
    public Film addLike(Integer filmId, Integer userId) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int likeInsertion = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_LIKE_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, filmId);
            ps.setObject(2, userId);
            return ps;
        }, keyHolder);
        if (likeInsertion == 0) {
            throw new InternalServerException("Не удалось поставить лайк");
        }
        return get(filmId);
    }

    @Override
    public Film cancelLike(Integer filmId, Integer userId) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int likeDeletion = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(DELETE_LIKE_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, filmId);
            ps.setObject(2, userId);
            return ps;
        }, keyHolder);
        if (likeDeletion == 0) {
            throw new InternalServerException("Не удалось удалить лайк");
        }
        return get(filmId);
    }

    @Override
    public Set<Integer> getLikes(Integer filmId) {
        Set<Integer> filmLikes = jdbc.queryForList(FIND_LIKES_BY_FILM_ID, Integer.class, filmId
        ).stream().collect(Collectors.toSet());
        return filmLikes;
    }

    @Override
    public List<Film> getTopRated(Integer count) {
        List<Integer> filmIds = jdbc.queryForList(FIND_TOP_RATED_FILMS, Integer.class, count
        ).stream().collect(Collectors.toList());
        List<Film> result = filmIds.stream()
                .map(filmId -> get(filmId))
                .collect(Collectors.toList());
        return result;
    }

}

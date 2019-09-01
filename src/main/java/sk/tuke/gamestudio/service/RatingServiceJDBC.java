package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.service.exception.RatingException;

import java.sql.*;

public class RatingServiceJDBC implements RatingService {
    public static final String URL = "jdbc:postgresql://localhost:5432/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";

    public static final String INSERT_RATING =
            "INSERT INTO rating (game, player, rating, ratedon) VALUES (?, ?, ?, ?)";

    public static final String SELECT_AVG =
            "SELECT game, player, rating, ratedon FROM rating WHERE game = ?;";

    public static final String SELECT_RATING =
            "SELECT game, player, rating, ratedon FROM rating WHERE game = ? AND player = ?;";


    @Override
    public void setRating(Rating rating) throws RatingException {
        RatingService ratingService = new RatingServiceJDBC();
        int tmp = ratingService.getRating(rating.getGame(), rating.getPlayer());
        if (tmp == -1) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                try (PreparedStatement ps = connection.prepareStatement(INSERT_RATING)) {
                    ps.setString(1, rating.getGame());
                    ps.setString(2, rating.getPlayer());
                    ps.setInt(3, rating.getRating());
                    ps.setDate(4, new Date(rating.getRatedon().getTime()));

                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RatingException("Error saving rating", e);
            }
        } else {
            //update
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        int rating_sum = 0;
        int counter = 0;
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_AVG)) {
                ps.setString(1, game);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Rating rating = new Rating(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getInt(3),
                                rs.getTimestamp(4)
                        );
                        rating_sum = rating_sum + rating.getRating();
                        counter++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Error loading rating", e);
        }
        return rating_sum / counter;
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_RATING)) {
                ps.setString(1, game);
                ps.setString(2, player);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(3);
                    } else {
                        return -1;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Error loading rating", e);
        }
    }
/*
    public static void main(String[] args) throws Exception {
        RatingService ratingService = new RatingServiceJDBC();
        System.out.println(ratingService.getRating("Mlyny" , "ADdwadawdaO"));
    }
*/
}

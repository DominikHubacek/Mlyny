package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.exception.ScoreException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreServiceJDBC implements ScoreService {
    public static final String URL = "jdbc:postgresql://localhost:5432/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";

    public static final String INSERT_SCORE =
            "INSERT INTO score (game, player, points, played_on) VALUES (?, ?, ?, ?)";

    public static final String SELECT_SCORE =
            "SELECT game, player, points, played_on FROM score WHERE game = ? ORDER BY points ASC;";

    @Override
    public void addScore(Score score) throws ScoreException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_SCORE)) {
                ps.setString(1, score.getGame());
                ps.setString(2, score.getPlayer());
                ps.setInt(3, score.getPoints());
                ps.setDate(4, new Date(score.getPlayedOn().getTime()));

                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ScoreException("Error saving score", e);
        }
    }

    @Override
    public List<Score> getBestScores(String game) throws ScoreException {
        List<Score> scores = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_SCORE)) {
                ps.setString(1, game);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Score score = new Score(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getInt(3),
                                rs.getTimestamp(4)
                        );
                        scores.add(score);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ScoreException(e.getMessage());
        }
        return scores;
    }
/*
    public static void main(String[] args) throws Exception {
        ScoreService scoreService = new ScoreServiceJDBC();
        try {
            List<Score> scoreList = scoreService.getBestScores("Mlyny");
            for (Score s: scoreList) {
                System.out.println("Hra: " + s.getGame());
                System.out.println("Meno: " + s.getPlayer());
                System.out.println("Body: " + s.getPoints());
                System.out.println("Cas: " + s.getPlayedOn());
                System.out.println();
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
*/
}

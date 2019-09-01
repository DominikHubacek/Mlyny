package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.exception.CommentException;
import sk.tuke.gamestudio.service.exception.ScoreException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentServiceJDBC implements CommentService {
    public static final String URL = "jdbc:postgresql://localhost:5432/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";

    public static final String INSERT_COMMENT =
            "INSERT INTO comment (player, game, comment, commented_on) VALUES (?, ?, ?, ?)";

    public static final String SELECT_COMMENT =
            "SELECT player, game, comment, commented_on FROM comment WHERE game = ?";

    @Override
    public void addComment(Comment comment) throws CommentException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_COMMENT)) {
                ps.setString(1, comment.getPlayer());
                ps.setString(2, comment.getGame());
                ps.setString(3, comment.getComment());
                ps.setDate(4, new Date(comment.getCommentedOn().getTime()));

                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CommentException("Error saving comment", e);
        }
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException {
        List<Comment> comments = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_COMMENT)) {
                ps.setString(1, game);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Comment comment = new Comment(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getTimestamp(4)
                        );
                        comments.add(comment);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ScoreException(e.getMessage());
        }
        return comments;
    }
/*
    public static void main(String[] args) throws Exception {
        CommentService commentService = new CommentServiceJDBC();
        try {
            List<Comment> commentList = commentService.getComments("Mlyny");
            for (Comment c : commentList) {
                System.out.println("Hra: " + c.getGame());
                System.out.println("Meno: " + c.getPlayer());
                System.out.println("Koment: " + c.getComment());
                System.out.println("Cas: " + c.getCommentedOn());
                System.out.println();
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    */
}

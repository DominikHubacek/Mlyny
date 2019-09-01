package sk.tuke.gamestudio.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@NamedQuery(name = "Score.getBestScores", query = "select s from Score s where s.game=:game order by s.points DESC ")
public class Score implements Comparable<Score>, Serializable {

    @Id
    @GeneratedValue
    private int ident;

    private String player;

    private int points;

    private String game;

    private Date playedOn;

    public Score() {
    }

    public Score(String game, String player, int points, Date playedOn) {
        this.player = player;
        this.points = points;
        this.game = game;
        this.playedOn = playedOn;
    }



    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Date getPlayedOn() {
        return playedOn;
    }

    public void setPlayedOn(Date playedOn) {
        this.playedOn = playedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return points == score.points &&
                player.equals(score.player) &&
                game.equals(score.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, points, game);
    }

    @Override
    public String toString() {
        return "player='" + player + '\'' +
                ", points=" + points +
                ", game='" + game + '\'' +
                ", playedOn=" + playedOn;
    }

    @Override
    public int compareTo(Score o) {
        return o.points - this.points;
    }

    public int getIdent() {
        return ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }
}

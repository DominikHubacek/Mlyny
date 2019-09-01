package sk.tuke.gamestudio.game.hubacek.ninemen.gameCore;

public class Player {
    private String name;
    private static int numberOfRocks = 4; //max number of rocks
    private int rocks = numberOfRocks;  //number of rocks in stack
    private Field field;
    private int playerNumber;
    private TileState tileState;
    private int remainingRocks = numberOfRocks; //total number of rocks, in player stack and on board
    private int score = 0;
    private int finalScore = 0;

    Player(Field field, int playerNumber, TileState tileState) {
        this.field = field;
        this.playerNumber = playerNumber;
        this.tileState = tileState;
    }

    public int getFinalScore() {
        return 1000 / score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    void addScore() {
        score++;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    int getNumberOfRocks() {
        return numberOfRocks;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TileState getTileState() {
        return tileState;
    }

    int getRemainingRocks() {
        return remainingRocks;
    }

    boolean placeRock(int x, int y) {
        if(field.getTile(x,y).getState().equals(TileState.Empty)) {
            if(rocks == 0){
                return false;
            }
            field.getTile(x,y).setState(tileState);
            rocks--;
            return true;
        }
        else {
            return false;
        }
    }

    public int getRocks() {
        return rocks;
    }

    void rockDestroyed(int x, int y) {
        field.getTile(x, y).setState(TileState.Empty);
        remainingRocks--;
    }
}
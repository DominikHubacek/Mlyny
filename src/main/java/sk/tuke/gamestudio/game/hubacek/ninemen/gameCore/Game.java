package sk.tuke.gamestudio.game.hubacek.ninemen.gameCore;

import static java.lang.Math.abs;

public class Game {
    private GameState gameState = GameState.PLAYING;
    private Player active;
    private Player enemy;
    private final Field field = new Field();
    private final Player player1 = new Player(field, 1, TileState.P1);
    private final Player player2 = new Player(field, 2, TileState.P2);
    private int tmpx = 10;
    private int tmpy = 10;
    private boolean selecting = false;
    private boolean killing = false;

    public void setKilling(boolean killing) {
        this.killing = killing;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    public boolean getKilling() {
        return killing;
    }

    public boolean getSelecting() {
        return selecting;
    }

    public void setTmpx(int tmpx) {
        this.tmpx = tmpx;
    }

    public void setTmpy(int tmpy) {
        this.tmpy = tmpy;
    }

    public int getTmpx() {
        return tmpx;
    }

    public int getTmpy() {
        return tmpy;
    }

    public Game(){
        active = player1;
        enemy = player2;
    }

    public boolean checkPass(TileState tileState) {
        if (tileState.equals(TileState.Next) || tileState.equals(TileState.Horizontal) || tileState.equals(TileState.Vertical)) {
            return true;
        }
        return false;
    }


    /**
     * Change current player
     */
    private void changePlayer() {
        if (active == player1) {
            active = player2;
            enemy = player1;
        } else {
            active = player1;
            enemy = player2;
        }
    }

    /**
     * check if selected tile is yours and remove it from field
     *
     * @param x = row
     * @param y = col
     * @return true if selected tile belongs to active player
     */
    public boolean selectYourTile(int x, int y) {
        if(active.getTileState() == field.getTile(x,y).getState()){
            field.getTile(x,y).setState(TileState.Empty);
            return true;
        }
        return false;
    }

    /**
     * in starting mode - puts a rock on an empty tile
     * in replacing mode - puts a rock on an empty tile + add score (1 move = 1 point)
     * if the player will have a row of 3, it wont change player
     * @param x = row
     * @param y = col
     * @return true - if move succ. || false - if move failed
     */
    public boolean move(int x, int y) {
        if (gameState == GameState.REPLACING) {
            field.getTile(x, y).setState(active.getTileState());
            active.addScore();
            if (!searchForRow(x, y)) {
                changePlayer();
            }
            return true;
        } else {
            if (active.placeRock(x, y)) {
                if (!searchForRow(x, y)) {
                    changePlayer();
                }
                if (active.getRocks() == 0) {
                    gameState = GameState.REPLACING;
                }
                return true;
            }
        }
        return false;
    }



    /**
     * check if selected tile is a part of a row of three
     * @param x = row
     * @param y = col
     * @return true - part of a row || false - not in a row
     */
    public boolean searchForRow(int x, int y) {
        TileState tileState = getField().getTile(x, y).getState();
        int couterx = 0;
        int coutery = 0;
        boolean tmp1 = true;
        boolean tmp2 = true;
        boolean tmp3 = true;
        boolean tmp4 = true;

        for(int i = 1; i < 7; i++) {
            if (x + i < 7) {
                if (field.getTile(x + i, y).getState().equals(tileState)) {
                    if (tmp1) {
                        couterx++;
                    }
                } else if (!((checkPass(field.getTile(x + i, y).getState()))) || (field.getTile(x + i, y).getState().equals(tileState))) {
                    tmp1 = false;
                }
            }

            if (x - i >= 0) {
                if (field.getTile(x - i, y).getState().equals(tileState)) {
                    if (tmp2) {
                        couterx++;
                    }
                } else if (!((checkPass(field.getTile(x - i, y).getState()))) || (field.getTile(x - i, y).getState().equals(tileState))) {
                    tmp2 = false;
                }
            }

            if(y+i < 7) {
                if (field.getTile(x, y + i).getState().equals(tileState)) {
                    if (tmp3) {
                        coutery++;
                    }
                } else if (!((checkPass(field.getTile(x, y + i).getState()))) || (field.getTile(x, y + i).getState().equals(tileState))) {
                    tmp3 = false;
                }
            }

            if(y-i >= 0) {
                if (field.getTile(x, y - i).getState().equals(tileState)) {
                    if(tmp4){
                        coutery++;
                    }
                } else if (!((checkPass(field.getTile(x, y - i).getState()))) || (field.getTile(x, y - i).getState().equals(tileState))) {
                    tmp4 = false;
                }
            }
        }
        return (couterx == 2) || (coutery == 2);
    }

    /**
     * check if tile belongs to enemy
     *
     * @param x - row
     * @param y - col
     * @return true - tile belongs to enemy || false
     */
    private boolean checkIfEnemy(int x, int y) {
        return field.getTile(x, y).getState() == enemy.getTileState();
    }

    /**
     * beginning method for searching a destroying enemy tile
     *
     * @param x - row
     * @param y - col
     * @return true - if tile was destroyed || false
     */
    public boolean searchAndDestroy(int x, int y) {
        if (!(checkIfEnemy(x, y))) {                        //check, if selected tile matches the opponent type of tile
            return false;
        }
        if (!(checkIfAttackPossible(x,y))){
            return false;
        }

        enemy.rockDestroyed(x, y);
        changePlayer();
        checkForWin();
        return true;
    }

    /**
     * check if game is won and selecting the winner (changing gamestate)
     */
    public void checkForWin(){
        if(player1.getRemainingRocks() < 3){
            gameState = GameState.P2WIN;
            active = player2;
        }
        else if(player2.getRemainingRocks() < 3){
            gameState = GameState.P1WIN;
            active = player1;
        }
    }

    /**
     * checking if selected tile can be destroyed
     *
     * @param x - row
     * @param y - col
     * @return true - can be destroyed || false
     */
    public boolean checkIfAttackPossible(int x, int y) {

        int rocksOnBoard = enemy.getNumberOfRocks() - enemy.getRocks() - (enemy.getNumberOfRocks() - enemy.getRemainingRocks());
        if (rocksOnBoard < 4) {
            return true;
        }
        if (rocksOnBoard % 3 != 0) {
            if (searchForRow(x, y)) {
                return false;
            }
        }
        if (!(searchForRow(x, y))){
            return true;
        }
        if (searchForRow(x, y)) {
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 7; col++) {
                    if (field.getTile(row, col).getState().equals(enemy.getTileState())) {
                        if(!(searchForRow(row,col))){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * check if tile can be replaced
     * @param row - row from
     * @param col - col from
     * @param rw - row to
     * @param cl - col to
     * @return true - if possible || false (return tile to initial spot)
     */
    public boolean replacementPossible(int row, int col, int rw, int cl) {

        int a = abs(rw - row);
        int b = abs(cl - col);

        if(active.getRemainingRocks() == 3){
            if(field.getTile(rw,cl).getState().equals(TileState.Empty)){
                return true;
            }
        }

        if ((a != 0) && (b != 0)) {
            getField().getTile(row, col).setState(getActive().getTileState());
            return false;
        }

        if(!(field.getTile(rw,cl).getState().equals(TileState.Empty))){
            getField().getTile(row, col).setState(getActive().getTileState());
            return false;
        }

        int r;
        int c;
        for (int i = 1; i < a; i++) {
            if (row < rw) {
                r = row;
            } else {
                r = rw;
            }
            if (!(checkPass(getField().getTile(r + i, col).getState()))) {
                getField().getTile(row, col).setState(getActive().getTileState());
                return false;
            }
        }

        for (int i = 1; i < b; i++) {
            if (col < cl) {
                c = col;
            } else {
                c = cl;
            }
            if (!(checkPass(getField().getTile(row, c + i).getState()))) {
                getField().getTile(row, col).setState(getActive().getTileState());
                return false;
            }
        }
        return true;
    }

    public boolean checkReplacementPossible(int row, int col, int rw, int cl) {

        int a = abs(rw - row);
        int b = abs(cl - col);

        if (active.getRemainingRocks() == 3) {
            if (field.getTile(rw, cl).getState().equals(TileState.Empty)) {
                return true;
            }
        }

        if ((a != 0) && (b != 0)) {
            return false;
        }

        if (!(field.getTile(rw, cl).getState().equals(TileState.Empty))) {
            return false;
        }

        int r;
        int c;
        for (int i = 1; i < a; i++) {
            if (row < rw) {
                r = row;
            } else {
                r = rw;
            }
            if (!(checkPass(getField().getTile(r + i, col).getState()))) {
                return false;
            }
        }

        for (int i = 1; i < b; i++) {
            if (col < cl) {
                c = col;
            } else {
                c = cl;
            }
            if (!(checkPass(getField().getTile(row, c + i).getState()))) {
                return false;
            }
        }
        return true;
    }

    public boolean checkIfSelectPossible(int x, int y) {
       /* int couterx = 0;
        int coutery = 0;

        for (int i = 1; i < 4; i++) {
            if (x + i < 7) {
                if (field.getTile(x + i, y).getState().equals(TileState.Empty)) {
                    couterx++;
                }
            }

            if (x - i >= 0) {
                if (field.getTile(x - i, y).getState().equals(TileState.Empty)) {
                    couterx++;
                }
            }

            if (y + i < 7) {
                if (field.getTile(x, y + i).getState().equals(TileState.Empty)) {
                    coutery++;
                }
            }

            if (y - i >= 0) {
                if (field.getTile(x, y - i).getState().equals(TileState.Empty)) {
                    coutery++;
                }
            }
        }
        return (couterx > 0) || (coutery > 0);*/
        int couterx = 0;
        int coutery = 0;
        boolean tmp1 = true;
        boolean tmp2 = true;
        boolean tmp3 = true;
        boolean tmp4 = true;

        for (int i = 1; i < 7; i++) {
            if (x + i < 7) {
                if (field.getTile(x + i, y).getState().equals(TileState.Empty)) {
                    if (tmp1) {
                        return true;
                    }
                } else if (!((checkPass(field.getTile(x + i, y).getState())))) {
                    tmp1 = false;
                }
            }

            if (x - i >= 0) {
                if (field.getTile(x - i, y).getState().equals(TileState.Empty)) {
                    if (tmp2) {
                        return true;
                    }
                } else if (!((checkPass(field.getTile(x - i, y).getState())))) {
                    tmp2 = false;
                }
            }

            if (y + i < 7) {
                if (field.getTile(x, y + i).getState().equals(TileState.Empty)) {
                    if (tmp3) {
                        return true;
                    }
                } else if (!((checkPass(field.getTile(x, y + i).getState())))) {
                    tmp3 = false;
                }
            }

            if (y - i >= 0) {
                if (field.getTile(x, y - i).getState().equals(TileState.Empty)) {
                    if (tmp4) {
                        return true;
                    }
                } else if (!((checkPass(field.getTile(x, y - i).getState())))) {
                    tmp4 = false;
                }
            }
        }
        return false;
    }

    public Player getEnemy() {
        return enemy;
    }

    public Field getField() {
        return field;
    }

    public Player getActive() {
        return active;
    }

    public GameState gameState() {
        return gameState;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setActive(Player active) { this.active = active; }
}
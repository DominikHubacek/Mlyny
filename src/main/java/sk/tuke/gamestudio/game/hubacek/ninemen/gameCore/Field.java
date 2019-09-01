package sk.tuke.gamestudio.game.hubacek.ninemen.gameCore;

public class Field {

    private final Tile[][] tiles;

    public Field() {
        tiles = new Tile[7][7];
        initializeTiles();
    }

    private void initializeTiles(){
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                tiles[i][j] = new Tile();
            }
        }
        for (int y = 0; y<=1; y++) {
            for (int i = 0; i < 4; i = i + 3) {
                tiles[0][1 + i + y].setState(TileState.Horizontal);//pozdlzne
                tiles[1 + i + y][0].setState(TileState.Vertical);//zhoradole
                tiles[6][1 + i + y].setState(TileState.Horizontal);//pozdlzne
                tiles[1 + i + y][6].setState(TileState.Vertical);//zhoradole
                tiles[1][2 + (y * 2)].setState(TileState.Horizontal);//pozdlzne
                tiles[5][2 + (y * 2)].setState(TileState.Horizontal);//pozdlzne
                tiles[2 + (y * 2)][1].setState(TileState.Vertical);//zhoradole
                tiles[2 + (y * 2)][5].setState(TileState.Vertical);//zhoradole
            }
        }
        tiles[3][3].setState(TileState.Stop);
    }

    public Tile getTile(int x, int y){
        return tiles[x][y];
    }


}

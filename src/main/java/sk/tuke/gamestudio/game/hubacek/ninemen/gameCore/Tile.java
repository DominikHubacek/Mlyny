package sk.tuke.gamestudio.game.hubacek.ninemen.gameCore;

public class Tile {
    private TileState state = TileState.Empty;

    public TileState getState() {
        return state;
    }

    public void setState(TileState state) {
        this.state = state;
    }
}

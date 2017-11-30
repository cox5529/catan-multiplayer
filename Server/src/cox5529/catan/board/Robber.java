package cox5529.catan.board;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Robber {

	private int diagonal;
	private int column;

	@JsonIgnore
	private CatanTile tile;

	public Robber(int diagonal, int column, CatanTile tile) {
		this.diagonal = diagonal;
		this.column = column;
		this.tile = tile;
	}

	public int getDiagonal() {
		return diagonal;
	}

	public int getColumn() {
		return column;
	}

	public CatanTile getTile() {
		return tile;
	}

	public void setTile(CatanTile tile, int diagonal, int column) {
		if (this.tile != null) this.tile.setRobber(false);
		tile.setRobber(true);
		this.tile = tile;
		this.diagonal = diagonal;
		this.column = column;
	}
}

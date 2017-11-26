package cox5529.catan.board;


import cox5529.catan.Card;

import java.util.Arrays;

public class CatanBoard {

	private CatanTile[] tiles;
	private CatanEdge[] edges;

	public CatanBoard() {
		fillArrays();
	}

	public CatanTile[] getTiles() {
		return tiles;
	}

	public CatanEdge[] getEdges() {
		return edges;
	}

	private void fillArrays() {
		tiles = new CatanTile[19];
		tiles[0] = new CatanTile(Card.None);
		tiles[1] = new CatanTile(Card.Wheat);
		tiles[2] = new CatanTile(Card.Wheat);
		tiles[3] = new CatanTile(Card.Wheat);
		tiles[4] = new CatanTile(Card.Wheat);
		tiles[5] = new CatanTile(Card.Brick);
		tiles[6] = new CatanTile(Card.Brick);
		tiles[7] = new CatanTile(Card.Brick);
		tiles[8] = new CatanTile(Card.Stone);
		tiles[9] = new CatanTile(Card.Stone);
		tiles[10] = new CatanTile(Card.Stone);
		tiles[11] = new CatanTile(Card.Sheep);
		tiles[12] = new CatanTile(Card.Sheep);
		tiles[13] = new CatanTile(Card.Sheep);
		tiles[14] = new CatanTile(Card.Sheep);
		tiles[15] = new CatanTile(Card.Wood);
		tiles[16] = new CatanTile(Card.Wood);
		tiles[17] = new CatanTile(Card.Wood);
		tiles[18] = new CatanTile(Card.Wood);

		edges = new CatanEdge[6];
		edges[0] = new CatanEdge(new CatanPort(Card.All));
		edges[1] = new CatanEdge(new CatanPort(Card.Stone));
		edges[2] = new CatanEdge(new CatanPort(Card.All), new CatanPort(Card.Sheep));
		edges[3] = new CatanEdge(new CatanPort(Card.All), new CatanPort(Card.Brick));
		edges[4] = new CatanEdge(new CatanPort(Card.All), new CatanPort(Card.Wheat));
		edges[5] = new CatanEdge(new CatanPort(Card.Wood));
	}

	private void shuffleArrays() {
		for (int i = 0; i < tiles.length; i++) {
			int r = (int) (Math.random() * tiles.length);
			CatanTile temp = tiles[i];
			tiles[i] = tiles[r];
			tiles[r] = temp;
		}
		for (int i = 0; i < edges.length; i++) {
			int r = (int) (Math.random() * edges.length);
			CatanEdge temp = edges[i];
			edges[i] = edges[r];
			edges[r] = temp;
		}
	}

	public static CatanBoard generate() {
		CatanBoard board = new CatanBoard();
		board.shuffleArrays();
		int[] rolls = new int[18];
		rolls[0] = 2;
		rolls[1] = 3;
		rolls[2] = 3;
		rolls[3] = 4;
		rolls[4] = 4;
		rolls[5] = 5;
		rolls[6] = 5;
		rolls[7] = 6;
		rolls[8] = 6;
		rolls[9] = 8;
		rolls[10] = 8;
		rolls[11] = 9;
		rolls[12] = 9;
		rolls[13] = 10;
		rolls[14] = 10;
		rolls[15] = 11;
		rolls[16] = 11;
		rolls[17] = 12;

		for (int i = 0; i < rolls.length; i++) {
			int r = (int) (Math.random() * rolls.length);
			int temp = rolls[i];
			rolls[i] = rolls[r];
			rolls[r] = temp;
		}

		CatanTile[] tiles = board.getTiles();
		int tileIndex = 0;
		for (int i = 0; i < rolls.length; i++) {
			if (tiles[tileIndex].getResource() == Card.None) {
				tileIndex++;
			}
			tiles[tileIndex].setRoll(rolls[i]);
			tileIndex++;
		}

		return board;
	}

	@Override
	public String toString() {
		return "CatanBoard{" +
				"tiles=" + Arrays.toString(tiles) +
				", edges=" + Arrays.toString(edges) +
				'}';
	}
}

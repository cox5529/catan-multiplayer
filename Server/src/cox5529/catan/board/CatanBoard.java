package cox5529.catan.board;


import cox5529.catan.Card;

import java.util.Arrays;

public class CatanBoard {

	private CatanTile[][] tiles; // diagonal, column
	private CatanPort[] ports;
	private CatanLink[] links;
	private CatanSpace[] spaces;

	public CatanBoard() {
		fillArrays();
	}

	public CatanTile[][] getTiles() {
		return tiles;
	}

	public CatanPort[] getPorts() {
		return ports;
	}

	public CatanSpace[] getSpaces() {
		return spaces;
	}

	public CatanLink[] getLinks() {
		return links;
	}

	private void fillArrays() {
		tiles = new CatanTile[6][5];
		tiles[1][0] = new CatanTile(Card.None, 4, 1);
		tiles[1][1] = new CatanTile(Card.Wheat, 5, 1);
		tiles[1][2] = new CatanTile(Card.Wheat, 5, 2);
		tiles[2][0] = new CatanTile(Card.Wheat, 4, 0);
		tiles[2][1] = new CatanTile(Card.Wheat, -1, -1);
		tiles[2][2] = new CatanTile(Card.Brick, -1, -1);
		tiles[2][3] = new CatanTile(Card.Brick, 0, 2);
		tiles[3][0] = new CatanTile(Card.Brick, 3, 0);
		tiles[3][1] = new CatanTile(Card.Stone, -1, -1);
		tiles[3][2] = new CatanTile(Card.Stone, -1, -1);
		tiles[3][3] = new CatanTile(Card.Stone, -1, -1);
		tiles[3][4] = new CatanTile(Card.Sheep, 0, 3);
		tiles[4][1] = new CatanTile(Card.Sheep, 3, 5);
		tiles[4][2] = new CatanTile(Card.Sheep, -1, -1);
		tiles[4][3] = new CatanTile(Card.Sheep, -1, -1);
		tiles[4][4] = new CatanTile(Card.Wood, 1, 3);
		tiles[5][2] = new CatanTile(Card.Wood, 2, 5);
		tiles[5][3] = new CatanTile(Card.Wood, 2, 4);
		tiles[5][4] = new CatanTile(Card.Wood, 1, 4);

		ports = new CatanPort[9];
		ports[0] = new CatanPort(Card.All);
		ports[1] = new CatanPort(Card.All);
		ports[2] = new CatanPort(Card.All);
		ports[3] = new CatanPort(Card.All);
		ports[4] = new CatanPort(Card.Wheat);
		ports[5] = new CatanPort(Card.Stone);
		ports[6] = new CatanPort(Card.Sheep);
		ports[7] = new CatanPort(Card.Wood);
		ports[8] = new CatanPort(Card.Brick);
	}

	private void fillSecondaryArrays() {
		spaces = new CatanSpace[54];
		int spaceIndex = 0;
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {
					for (int k = 0; k < 6; k++) {
						int hexCount = getHexCountSpace(i, j, k);
						int[][] hexes = getBorderingHexesSpace(i, j, k);
						if (hexCount > 1) {
							boolean done = false;
							for (int l = 1; l < hexes.length; l++) {
								if (hexes[l][0] < i || (hexes[l][0] == i && hexes[l][1] < j)) {
									done = true;
									break;
								}
							}
							if (!done) {
								CatanSpace space = new CatanSpace();
								for (int[] point : hexes) {
									CatanTile tile = getTile(point[0], point[1]);
									space.addTile(tile);
								}
								space.setDiagonal(i);
								space.setColumn(j);
								space.setPosition(k);
								spaces[spaceIndex] = space;
								spaceIndex++;
							}
						} else {
							CatanSpace space = new CatanSpace();
							space.addTile(tiles[i][j]);
							space.setDiagonal(i);
							space.setColumn(j);
							space.setPosition(k);
							spaces[spaceIndex] = space;
							spaceIndex++;
						}
					}
				}
			}
		}
		System.out.println("Filled spaces");

		links = new CatanLink[76];
		int linkIndex = 0;
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {
					for (int k = 0; k < 6; k++) {
						int hexCount = getHexCountLink(i, j, k);
						int[][] hexes = getBorderingHexesLink(i, j, k);
						if (hexCount > 1) {
							boolean done = false;
							for (int l = 1; l < hexes.length; l++) {
								if (hexes[l][0] < i || (hexes[l][0] == i && hexes[l][1] < j)) {
									done = true;
									break;
								}
							}
							if (!done) {
								CatanLink link = new CatanLink();
								link.setFrontSpace(findSpace(i, j, k == 0 ? 5 : k - 1));
								link.setRearSpace(findSpace(i, j, k));
								link.setDiagonal(i);
								link.setColumn(j);
								link.setPosition(k);
								links[linkIndex] = link;
								linkIndex++;
							}
						} else {
							CatanLink link = new CatanLink();
							link.setFrontSpace(findSpace(i, j, k == 0 ? 5 : k - 1));
							link.setRearSpace(findSpace(i, j, k));
							link.setDiagonal(i);
							link.setColumn(j);
							link.setPosition(k);
							links[linkIndex] = link;
							linkIndex++;
						}
					}
				}
			}
		}
	}

	private CatanSpace findSpace(int diagonal, int column, int position) {
		for (CatanSpace space : spaces) {
			if (space.getDiagonal() == diagonal && space.getColumn() == column && space.getPosition() == position) {
				return space;
			}
		}
		return null;
	}

	private int getHexCountLink(int i, int j, int k) {
		if (getHexCountSpace(i, j, k) == 3)
			return 2;
		if (k > tiles[i][j].getExteriorStart() && k <= tiles[i][j].getExteriorEnd())
			return 1;
		if (k > tiles[i][j].getExteriorStart() || k <= tiles[i][j].getExteriorEnd())
			return 1;
		return 2;
	}

	private int[][] getBorderingHexesLink(int i, int j, int k) {
		int hexCount = getHexCountLink(i, j, k);
		int[][] re = new int[hexCount][2];
		re[0][0] = i;
		re[0][1] = j;
		if (hexCount == 1)
			return re;
		int index = 1;
		if (k == 0) {
			if (getTile(i - 1, j - 1) != null) {
				re[index][0] = i - 1;
				re[index][1] = j - 1;
				index++;
			}
		}
		if (k == 1) {
			if (getTile(i - 1, j) != null) {
				re[index][0] = i - 1;
				re[index][1] = j;
				index++;
			}
		}
		if (k == 2) {
			if (getTile(i, j + 1) != null) {
				re[index][0] = i;
				re[index][1] = j + 1;
				index++;
			}
		}
		if (k == 3) {
			if (getTile(i + 1, j + 1) != null) {
				re[index][0] = i + 1;
				re[index][1] = j + 1;
				index++;
			}
		}
		if (k == 4) {
			if (getTile(i + 1, j) != null) {
				re[index][0] = i + 1;
				re[index][1] = j;
				index++;
			}
		}
		if (k == 5) {
			if (getTile(i, j - 1) != null) {
				re[index][0] = i;
				re[index][1] = j - 1;
			}
		}
		return re;
	}

	private CatanTile getTile(int diag, int col) {
		if (diag < 1 || diag > 5 || col < 0 || col > 4) {
			return null;
		}
		return tiles[diag][col];
	}

	private int[][] getBorderingHexesSpace(int i, int j, int k) {
		int hexCount = getHexCountSpace(i, j, k);
		int[][] re = new int[hexCount][2];
		re[0][0] = i;
		re[0][1] = j;
		if (hexCount == 1)
			return re;
		int index = 1;
		if (k == 0 || k == 1) {
			if (getTile(i - 1, j) != null) {
				re[index][0] = i - 1;
				re[index][1] = j;
				index++;
			}
		}
		if (k == 1 || k == 2) {
			if (getTile(i, j + 1) != null) {
				re[index][0] = i;
				re[index][1] = j + 1;
				index++;
			}
		}
		if (k == 2 || k == 3) {
			if (getTile(i + 1, j + 1) != null) {
				re[index][0] = i + 1;
				re[index][1] = j + 1;
				index++;
			}
		}
		if (k == 3 || k == 4) {
			if (getTile(i + 1, j) != null) {
				re[index][0] = i + 1;
				re[index][1] = j;
				index++;
			}
		}
		if (k == 4 || k == 5) {
			if (getTile(i, j - 1) != null) {
				re[index][0] = i;
				re[index][1] = j - 1;
				index++;
			}
		}
		if (k == 5 || k == 0) {
			if (getTile(i - 1, j - 1) != null) {
				re[index][0] = i - 1;
				re[index][1] = j - 1;
			}
		}
		return re;
	}

	private int getHexCountSpace(int i, int j, int k) {
		if (k == tiles[i][j].getExteriorStart() || k == tiles[i][j].getExteriorEnd()) {
			return 2;
		} else if (tiles[i][j].getExteriorEnd() == -1) {
			return 3;
		} else if (tiles[i][j].getExteriorStart() > tiles[i][j].getExteriorEnd() && (k > tiles[i][j].getExteriorStart() || k < tiles[i][j].getExteriorEnd())) {
			return 1;
		} else if (k > tiles[i][j].getExteriorStart() && k < tiles[i][j].getExteriorEnd()) {
			return 1;
		} else {
			return 3;
		}
	}

	private void shuffleArrays() {
		System.out.println("Shuffling board...");
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] == null) continue;
				int x = (int) (Math.random() * tiles.length);
				int y = (int) (Math.random() * tiles[i].length);
				while (tiles[x][y] == null) {
					x = (int) (Math.random() * tiles.length);
					y = (int) (Math.random() * tiles[i].length);
				}
				CatanTile temp = tiles[i][j];
				tiles[i][j] = tiles[x][y];
				tiles[x][y] = temp;

				tiles[i][j].swapExterior(tiles[x][y]);
			}
		}
		for (int i = 0; i < ports.length; i++) {
			int r = (int) (Math.random() * ports.length);
			CatanPort temp = ports[i];
			ports[i] = ports[r];
			ports[r] = temp;
		}
		System.out.println("Finished shuffling board");
	}

	public static CatanBoard generate() {
		System.out.println("Generating board...");
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
		int rollIndex = 0;
		CatanTile[][] tiles = board.getTiles();
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null && tiles[i][j].getResource() != Card.None) {
					tiles[i][j].setRoll(rolls[rollIndex]);
					rollIndex++;
				}
			}
		}
		System.out.println("Filling secondary arrays");
		board.fillSecondaryArrays();
		System.out.println("Generated board");
		return board;
	}

	@Override
	public String toString() {
		return "CatanBoard{" +
				"tiles=" + Arrays.toString(tiles) +
				", ports=" + Arrays.toString(ports) +
				", links=" + Arrays.toString(links) +
				", spaces=" + Arrays.toString(spaces) +
				'}';
	}
}

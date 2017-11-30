package cox5529.catan.board;


import cox5529.Utility;
import cox5529.catan.Card;

import java.util.Arrays;

public class CatanBoard {

	private CatanTile[][] tiles; // diagonal, column
	private CatanPort[] ports;
	private CatanLink[] links;
	private Robber robber;

	public CatanBoard() {
		fillArrays();
		robber = new Robber(0, 0, null);
	}

	public Robber getRobber() {
		return robber;
	}

	public CatanTile[][] getTiles() {
		return tiles;
	}

	public CatanPort[] getPorts() {
		return ports;
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

		ports = new CatanPort[18];
		ports[1] = new CatanPort(Card.All);
		ports[3] = new CatanPort(Card.All);
		ports[5] = new CatanPort(Card.Brick);
		ports[7] = new CatanPort(Card.Wood);
		ports[9] = new CatanPort(Card.All);
		ports[11] = new CatanPort(Card.Wheat);
		ports[13] = new CatanPort(Card.Stone);
		ports[15] = new CatanPort(Card.All);
		ports[17] = new CatanPort(Card.Sheep);
	}

	private void fillSecondaryArrays() {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {
					for (int k = 0; k < 6; k++) {
						int hexCount = getHexCountSpace(i, j, k);
						int[][] hexes = getBorderingHexesSpace(i, j, k);
						if (hexCount > 1) {
							boolean done = false;
							for (int l = 0; l < hexes.length; l++) {
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
									tile.getSpaces()[point[2]] = space;
								}

							}
						} else {
							CatanSpace space = new CatanSpace();
							space.addTile(tiles[i][j]);
							tiles[i][j].getSpaces()[k] = space;
						}
					}
				}
			}
		}

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
		if (diagonal >= 1 && diagonal <= 5 && column >= 0 && column <= 4 && position >= 0 && position <= 6) {
			return tiles[diagonal][column].getSpaces()[position];
		} else {
			return null;
		}
	}

	private int getHexCountLink(int i, int j, int k) {
		if (getHexCountSpace(i, j, k) == 3) return 2;
		if (k > tiles[i][j].getExteriorStart() && k <= tiles[i][j].getExteriorEnd()) return 1;
		if (k > tiles[i][j].getExteriorStart() || k <= tiles[i][j].getExteriorEnd()) return 1;
		return 2;
	}

	private int[][] getBorderingHexesLink(int i, int j, int k) {
		int hexCount = getHexCountLink(i, j, k);
		int[][] re = new int[hexCount][2];
		re[0][0] = i;
		re[0][1] = j;
		if (hexCount == 1) return re;
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
		int[][] re = new int[hexCount][3];
		re[0][0] = i;
		re[0][1] = j;
		re[0][2] = k;
		if (hexCount == 1) return re;
		int index = 1;
		if (k == 0 || k == 1) {
			if (getTile(i - 1, j) != null) {
				re[index][0] = i - 1;
				re[index][1] = j;
				re[index][2] = 4 - k;
				index++;
			}
		}
		if (k == 1 || k == 2) {
			if (getTile(i, j + 1) != null) {
				re[index][0] = i;
				re[index][1] = j + 1;
				re[index][2] = 6 - k;
				index++;
			}
		}
		if (k == 2 || k == 3) {
			if (getTile(i + 1, j + 1) != null) {
				re[index][0] = i + 1;
				re[index][1] = j + 1;
				if (k == 2) re[index][2] = 0;
				else re[index][2] = 5;
				index++;
			}
		}
		if (k == 3 || k == 4) {
			if (getTile(i + 1, j) != null) {
				re[index][0] = i + 1;
				re[index][1] = j;
				re[index][2] = 4 - k;
				index++;
			}
		}
		if (k == 4 || k == 5) {
			if (getTile(i, j - 1) != null) {
				re[index][0] = i;
				re[index][1] = j - 1;
				re[index][2] = 6 - k;
				index++;
			}
		}
		if (k == 5 || k == 0) {
			if (getTile(i - 1, j - 1) != null) {
				re[index][0] = i - 1;
				re[index][1] = j - 1;
				if (k == 5) re[index][2] = 3;
				else re[index][2] = 2;
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

	private void linkElements() {
		getLink(1, 2, 1).setPort(ports[0]);
		getLink(2, 3, 1).setPort(ports[1]);
		getLink(2, 3, 2).setPort(ports[2]);
		getLink(3, 4, 2).setPort(ports[3]);
		getLink(4, 4, 2).setPort(ports[4]);
		getLink(4, 4, 3).setPort(ports[5]);
		getLink(5, 4, 3).setPort(ports[6]);
		getLink(5, 3, 3).setPort(ports[7]);
		getLink(5, 3, 4).setPort(ports[8]);
		getLink(5, 2, 4).setPort(ports[9]);
		getLink(4, 1, 4).setPort(ports[10]);
		getLink(4, 1, 5).setPort(ports[11]);
		getLink(3, 0, 5).setPort(ports[12]);
		getLink(2, 0, 5).setPort(ports[13]);
		getLink(2, 0, 0).setPort(ports[14]);
		getLink(1, 0, 0).setPort(ports[15]);
		getLink(1, 1, 0).setPort(ports[16]);
		getLink(1, 1, 1).setPort(ports[17]);
	}

	private CatanLink getLink(int diagonal, int column, int position) {
		for (CatanLink link : links) {
			if (link.getDiagonal() == diagonal && link.getColumn() == column && link.getPosition() == position)
				return link;
		}
		return null;
	}

	private void shuffleArrays() {
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
		int edgeLength = 3;
		for (int i = 0; i < ports.length; i += edgeLength) {
			int r = (int) (Math.random() * ports.length / edgeLength) * edgeLength;
			for (int j = 0; j < edgeLength; j++) {
				CatanPort temp = ports[i + j];
				ports[i + j] = ports[r + j];
				ports[r + j] = temp;
			}
		}
	}

	private void placeRobber() {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null && tiles[i][j].getResource() == Card.None) {
					moveRobber(i, j);
					break;
				}
			}
		}
	}

	public void moveRobber(int diagonal, int column) {
		robber.setTile(tiles[diagonal][column], diagonal, column);
	}

	public static CatanBoard generate() {
		Utility.log("Generating board");
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
		board.fillSecondaryArrays();
		board.linkElements();
		board.placeRobber();
		Utility.log("Generated board");
		return board;
	}

}

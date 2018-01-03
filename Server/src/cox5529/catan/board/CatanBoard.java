package cox5529.catan.board;


import cox5529.Utility;
import cox5529.catan.Card;
import cox5529.catan.board.building.CatanBuilding;
import cox5529.catan.board.building.Settlement;

import java.util.ArrayList;
import java.util.Collections;

public class CatanBoard implements Comparable {

	private CatanTile[][] tiles; // diagonal, column
	private CatanPort[] ports;
	private CatanLink[] links;
	private ArrayList<CatanSpace> spaces;
	private Robber robber;
	private double fitness;

	public CatanBoard() {
		fillArrays();
		spaces = new ArrayList<>();
		robber = new Robber(0, 0, null);
	}

	private CatanBoard(CatanTile[][] tiles, CatanPort[] ports) {
		this.tiles = tiles;
		this.ports = ports;
		spaces = new ArrayList<>();
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

	public ArrayList<CatanSpace> getSpaces() {
		return spaces;
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
		spaces.clear();
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
								space.setDiagonal(i);
								space.setColumn(j);
								space.setPosition(k);
								for (int[] point : hexes) {
									CatanTile tile = getTile(point[0], point[1]);
									space.addTile(tile);
									tile.getSpaces()[point[2]] = space;
								}
								spaces.add(space);
							}
						} else {
							CatanSpace space = new CatanSpace();
							space.setDiagonal(i);
							space.setColumn(j);
							space.setPosition(k);
							space.addTile(tiles[i][j]);
							tiles[i][j].getSpaces()[k] = space;
							spaces.add(space);
						}
					}
				}
			}
		}

		links = new CatanLink[72];
		int linkIndex = 0;
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {
					for (int k = 0; k < 6; k++) {
						int hexCount = getHexCountLink(i, j, k);
						if (hexCount > 1) {
							int[][] hexes = getBorderingHexesLink(i, j, k);
							boolean done = false;
							for (int l = 0; l < hexes.length; l++) {
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

	public CatanSpace findSpace(int diagonal, int column, int position) {
		if (diagonal >= 1 && diagonal <= 5 && column >= 0 && column <= 4 && position >= 0 && position < 6) {
			return tiles[diagonal][column].getSpaces()[position];
		} else {
			return null;
		}
	}

	public CatanLink findLink(int diagonal, int column, int position) {
		if (diagonal >= 1 && diagonal <= 5 && column >= 0 && column <= 4 && position >= 0 && position < 6) {
			int start;
			int end = position;
			if (position == 0) {
				start = 5;
			} else {
				start = end - 1;
			}
			CatanSpace startSpace = findSpace(diagonal, column, start);
			CatanSpace endSpace = findSpace(diagonal, column, end);
			for (CatanLink a : startSpace.getLinks()) {
				for (CatanLink b : endSpace.getLinks()) {
					if (a.equals(b)) {
						return a;
					}
				}
			}
		}
		return null;
	}

	public boolean isValidSettlementLocation(int diag, int col, int pos, int team, boolean init) {
		CatanSpace space = findSpace(diag, col, pos);
		if (space != null && space.getBuilding() == null) {
			boolean road = false;
			for (CatanLink link : space.getLinks()) {
				if (link.getFrontSpace() == space && link.getRearSpace().getBuilding() != null) {
					return false;
				} else if (link.getRearSpace() == space && link.getFrontSpace().getBuilding() != null) {
					return false;
				} else if (link.getRoad() == team) {
					road = true;
				}
			}
			return road || init;
		}
		return false;
	}

	public boolean isValidSettlementLocation(CatanSpace space, int team, boolean init) {
		if (space != null && space.getBuilding() == null) {
			boolean road = false;
			for (CatanLink link : space.getLinks()) {
				if (link.getFrontSpace() == space && link.getRearSpace().getBuilding() != null) {
					return false;
				} else if (link.getRearSpace() == space && link.getFrontSpace().getBuilding() != null) {
					return false;
				} else if (link.getRoad() == team) {
					road = true;
				}
			}
			return road || init;
		}
		return false;
	}

	public boolean isValidRoadLocation(int diag, int col, int pos, int team) {
		CatanLink link = findLink(diag, col, pos);
		CatanBuilding front = link.getFrontSpace().getBuilding();
		CatanBuilding rear = link.getRearSpace().getBuilding();
		if (front != null && front.getPlayer().getTeam() == team) {
			return true;
		}
		if (rear != null && rear.getPlayer().getTeam() == team) {
			return true;
		}
		for (CatanLink a : link.getFrontSpace().getLinks()) {
			if (a.getRoad() == team) {
				return true;
			}
		}
		for (CatanLink a : link.getRearSpace().getLinks()) {
			if (a.getRoad() == team) {
				return true;
			}
		}
		return false;
	}

	public boolean isValidRoadLocation(int diag, int col, int pos, int d1, int c1, int p1, int team) {
		CatanLink link = findLink(diag, col, pos);
		CatanBuilding front = link.getFrontSpace().getBuilding();
		CatanBuilding rear = link.getRearSpace().getBuilding();
		if (front != null && front.getPlayer().getTeam() == team) {
			return true;
		}
		if (rear != null && rear.getPlayer().getTeam() == team) {
			return true;
		}
		CatanLink l1 = findLink(d1, c1, p1);
		for (CatanLink a : link.getFrontSpace().getLinks()) {
			if (a.getRoad() == team || a.equals(l1)) {
				return true;
			}
		}
		for (CatanLink a : link.getRearSpace().getLinks()) {
			if (a.getRoad() == team || a.equals(l1)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValidPlacementLocation(int diag, int col, int pos, int spaceDiag, int spaceCol, int spacePos) {
		CatanLink link = findLink(diag, col, pos);
		CatanSpace space = findSpace(spaceDiag, spaceCol, spacePos);
		boolean road = (link.getRearSpace().equals(space) || link.getFrontSpace().equals(space));
		boolean settlement = isValidSettlementLocation(spaceDiag, spaceCol, spacePos, 5, true);
		return road && settlement;
	}

	public boolean isValidCityLocation(int diag, int col, int pos, int team) {
		CatanSpace space = findSpace(diag, col, pos);
		if (space != null) {
			CatanBuilding building = space.getBuilding();
			return building != null && building instanceof Settlement && building.getPlayer().getTeam() == team;
		}
		return false;
	}

	private int getHexCountLink(int i, int j, int k) {
		if (getHexCountSpace(i, j, k) == 3) return 2;
		if (getHexCountSpace(i, j, k == 0 ? 5 : k - 1) == 3) return 2;
		return 1;
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
		findLink(1, 2, 1).setPort(ports[0]);
		findLink(2, 3, 1).setPort(ports[1]);
		findLink(2, 3, 2).setPort(ports[2]);
		findLink(3, 4, 2).setPort(ports[3]);
		findLink(4, 4, 2).setPort(ports[4]);
		findLink(4, 4, 3).setPort(ports[5]);
		findLink(5, 4, 3).setPort(ports[6]);
		findLink(5, 3, 3).setPort(ports[7]);
		findLink(5, 3, 4).setPort(ports[8]);
		findLink(5, 2, 4).setPort(ports[9]);
		findLink(4, 1, 4).setPort(ports[10]);
		findLink(4, 1, 5).setPort(ports[11]);
		findLink(3, 0, 5).setPort(ports[12]);
		findLink(2, 0, 5).setPort(ports[13]);
		findLink(2, 0, 0).setPort(ports[14]);
		findLink(1, 0, 0).setPort(ports[15]);
		findLink(1, 1, 0).setPort(ports[16]);
		findLink(1, 1, 1).setPort(ports[17]);
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

	public static int getRollCount(int roll) {
		switch (roll) {
			case 2:
				return 1;
			case 3:
				return 2;
			case 4:
				return 3;
			case 5:
				return 4;
			case 6:
				return 5;
			case 7:
				return 6;
			case 8:
				return 5;
			case 9:
				return 4;
			case 10:
				return 3;
			case 11:
				return 2;
			case 12:
				return 5;
			default:
				return 0;
		}
	}

	public double getFitness() {
		return fitness;
	}

	private void calculateFitness() {
		double fitness = 0;
		for (CatanSpace space : spaces) {
			fitness += space.calculateFitness();
		}
		this.fitness = fitness;
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
		board.calculateFitness();
		return board;
	}

	private static CatanBoard mutate(CatanBoard a) {
		CatanTile[][] aTiles = a.getTiles();
		CatanTile[][] rTiles = new CatanTile[6][5];
		CatanPort[] aPorts = a.getPorts();
		CatanPort[] rPorts = new CatanPort[18];
		ArrayList<int[]> points = new ArrayList<>();
		for (int i = 0; i < aTiles.length; i++) {
			for (int j = 0; j < aTiles[i].length; j++) {
				if (aTiles[i][j] != null && aTiles[i][j].getResource() != Card.None) {
					points.add(new int[]{i, j});
				}
			}
		}
		ArrayList<Integer> edges = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			edges.add(i);
		}
		for (int i = 0; i < aTiles.length; i++) {
			for (int j = 0; j < aTiles[i].length; j++) {
				if (aTiles[i][j] != null) {
					CatanTile aTile = aTiles[i][j];
					CatanTile tile = new CatanTile(aTile.getResource(), aTile.getExteriorStart(), aTile.getExteriorEnd());
					tile.setRoll(aTile.getRoll());
					rTiles[i][j] = tile;
				}
			}
		}
		for (int i = 0; i < aPorts.length; i++) {
			CatanPort aPort = aPorts[i];
			if (aPort != null) {
				CatanPort port = new CatanPort(aPort.getType());
				rPorts[i] = port;
			}
		}

		// pick 2 resources to swap
		int[] first = points.remove((int) (Math.random() * points.size()));
		int[] second = points.remove((int) (Math.random() * points.size()));
		int i = first[0];
		int j = first[1];
		int x = second[0];
		int y = second[1];
		CatanTile tile1 = rTiles[i][j];
		CatanTile tile2 = rTiles[x][y];
		Card temp = tile1.getResource();
		tile1.setResource(tile2.getResource());
		tile2.setResource(temp);

		// pick 2 numbers to swap
		first = points.remove((int) (Math.random() * points.size()));
		second = points.remove((int) (Math.random() * points.size()));
		i = first[0];
		j = first[1];
		x = second[0];
		y = second[1];
		tile1 = rTiles[i][j];
		tile2 = rTiles[x][y];
		int t = tile1.getRoll();
		tile1.setRoll(tile2.getRoll());
		tile2.setRoll(t);

		// pick 2 edges to swap
		int one = edges.remove((int) (Math.random() * edges.size()));
		int two = edges.remove((int) (Math.random() * edges.size()));
		for (int k = 0; k < 3; k++) {
			CatanPort b = rPorts[one * 3 + k];
			rPorts[one * 3 + k] = rPorts[two * 3 + k];
			rPorts[two * 3 + k] = b;
		}

		CatanBoard board = new CatanBoard(rTiles, rPorts);
		board.fillSecondaryArrays();
		board.linkElements();
		board.placeRobber();
		board.calculateFitness();
		return board;
	}

	private static ArrayList<CatanBoard> doGeneration(ArrayList<CatanBoard> boards) {
		ArrayList<CatanBoard> re = new ArrayList<>();
		for (int i = 0; i < boards.size(); i++) {
			CatanBoard board = boards.get((int) (0.1 * boards.size() * i / boards.size()));
			re.add(mutate(board));
		}
		Collections.sort(re);
		Utility.log("" + re.get(0).getFitness());
		return re;
	}

	public static CatanBoard generateGenetic(int count, int generations) {
		ArrayList<CatanBoard> boards = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			boards.add(CatanBoard.generate());
		}
		Collections.sort(boards);
		for (int i = 0; i < generations; i++) {
			boards = doGeneration(boards);
			Utility.log(i + "");
		}
		CatanBoard board = boards.get(0);
		board.fillSecondaryArrays();
		board.linkElements();
		board.placeRobber();
		return board;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof CatanBoard) {
			CatanBoard other = (CatanBoard) o;
			if (other.getFitness() > getFitness()) return 1;
		}
		return -1;
	}
}

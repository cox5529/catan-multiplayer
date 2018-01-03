package cox5529.catan.board;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cox5529.catan.Card;
import cox5529.catan.board.building.CatanBuilding;

import java.util.ArrayList;

public class CatanSpace {

	@JsonIgnore
	private ArrayList<CatanLink> links;
	@JsonIgnore
	private ArrayList<CatanTile> tiles;

	private CatanBuilding building;
	@JsonIgnore
	private CatanPort port;
	@JsonIgnore
	private int diagonal;
	@JsonIgnore
	private int column;
	@JsonIgnore
	private int position;

	public CatanSpace() {
		links = new ArrayList<>();
		tiles = new ArrayList<>();
	}

	public double calculateFitness() {
		ArrayList<Integer> rolls = new ArrayList<>();
		ArrayList<Card> resources = new ArrayList<>();
		int[] counts = new int[5];
		double fitness = 0;
		int resourcesOn = 0;
		for (CatanTile tile : tiles) {
			int roll = tile.getRoll();
			if ((roll == 6 || roll == 8) && (rolls.contains(6) || rolls.contains(8))) return -100000000;
			if (!rolls.contains(tile.getRoll())) {
				resourcesOn += getRollQuality(roll);
				rolls.add(roll);
			}
			resources.add(tile.getResource());
		}

		// 10   multiple of same resource on same space
		double points = 10;
		for (Card card : resources) {
			if (card.getValue() != -1) counts[card.getValue()]++;
		}
		for (int count : counts) {
			if (count != 0 && count != 1) {
				points /= Math.pow(2, count);
			}
		}
		fitness += points;

		// 10	greater than 3.157 average roll quality per adjacent tile
		double delta = Math.abs(3.157 - resourcesOn / resources.size());
		points = 1.8672 * delta * delta - 8.8672 * delta + 10;
		fitness += points;

		// 30	wheat next to stone
		points = 30;
		if (resources.contains(Card.Wheat) && resources.contains(Card.Stone)) {
			ArrayList<Integer> wheatRolls = new ArrayList<>();
			ArrayList<Integer> stoneRolls = new ArrayList<>();
			int wheatOn = 0;
			int stoneOn = 0;
			for (CatanTile tile : tiles) {
				if (tile.getResource() == Card.Wheat) {
					int roll = tile.getRoll();
					if (!wheatRolls.contains(roll)) wheatOn += getRollQuality(roll);
					wheatRolls.add(roll);
				} else if (tile.getResource() == Card.Stone) {
					int roll = tile.getRoll();
					if (!stoneRolls.contains(roll)) stoneOn += getRollQuality(roll);
					stoneRolls.add(roll);
				}
			}
			points = (0.1536 * stoneOn * stoneOn - 3.0816 * stoneOn + 15.182) / 15.182 * 15.0;
			points += (0.1536 * wheatOn * wheatOn - 3.0816 * wheatOn + 15.182) / 15.182 * 15.0;
		}
		fitness += points;

		// 10	wood next to brick
		points = 10;
		if (resources.contains(Card.Wood) && resources.contains(Card.Brick)) {
			ArrayList<Integer> woodRolls = new ArrayList<>();
			ArrayList<Integer> brickRolls = new ArrayList<>();
			int woodOn = 0;
			int brickOn = 0;
			for (CatanTile tile : tiles) {
				if (tile.getResource() == Card.Wheat) {
					int roll = tile.getRoll();
					if (!woodRolls.contains(roll)) woodOn += getRollQuality(roll);
					woodRolls.add(roll);
				} else if (tile.getResource() == Card.Stone) {
					int roll = tile.getRoll();
					if (!brickRolls.contains(roll)) brickOn += getRollQuality(roll);
					brickRolls.add(roll);
				}
			}
			points = (0.0665 * woodOn * woodOn - 1.1477 * woodOn + 4.9674) / 4.9674 * 5;
			points += (0.0665 * brickOn * brickOn - 1.1477 * brickOn + 4.9674) / 4.9674 * 5;
		}
		fitness += points;

		// 30   resource on its port
		points = 30;
		if (port != null && resources.contains(port.getType())) {
			ArrayList<Integer> portRolls = new ArrayList<>();
			int portOn = 0;
			for (CatanTile tile : tiles) {
				if (tile.getResource() == Card.Wheat) {
					int roll = tile.getRoll();
					if (!portRolls.contains(roll)) portOn += getRollQuality(roll);
					portRolls.add(roll);
				}
			}
			points = (0.2614 * portOn * portOn - 5.8015 * portOn + 30.643) / 30.643 * 30.0;
		}
		fitness += points;
		return fitness;
	}

	private int getRollQuality(int roll) {
		if (roll == 2 || roll == 12) return 1;
		else if (roll == 3 || roll == 11) return 2;
		else if (roll == 4 || roll == 10) return 3;
		else if (roll == 5 || roll == 9) return 4;
		else return 5;
	}

	public int getDiagonal() {
		return diagonal;
	}

	public void setDiagonal(int diagonal) {
		this.diagonal = diagonal;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public CatanPort getPort() {
		return port;
	}

	public void setPort(CatanPort port) {
		this.port = port;
	}

	public void addLink(CatanLink link) {
		links.add(link);
	}

	public void addTile(CatanTile tile) {
		tiles.add(tile);
	}

	public CatanBuilding getBuilding() {
		return building;
	}

	public void setBuilding(CatanBuilding building) {
		this.building = building;
	}

	public ArrayList<CatanLink> getLinks() {
		return links;
	}

	public ArrayList<CatanTile> getTiles() {
		return tiles;
	}
}

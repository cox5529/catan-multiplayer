package cox5529.catan.board;

import cox5529.catan.Card;

import java.util.Arrays;

public class CatanTile {

	private Card resource;
	private int roll;
	private int exteriorStart;
	private int exteriorEnd;
	private CatanSpace[] spaces;

	public CatanTile(Card resource, int exteriorStart, int exteriorEnd) {
		this.resource = resource;
		this.exteriorStart = exteriorStart;
		this.exteriorEnd = exteriorEnd;
		spaces = new CatanSpace[6];
	}

	public int getExteriorStart() {
		return exteriorStart;
	}

	public CatanSpace[] getSpaces() {
		return spaces;
	}

	public int getExteriorEnd() {
		return exteriorEnd;
	}

	public void setExteriorStart(int exteriorStart) {
		this.exteriorStart = exteriorStart;
	}

	public void setExteriorEnd(int exteriorEnd) {
		this.exteriorEnd = exteriorEnd;
	}

	public Card getResource() {
		return resource;
	}

	public void setResource(Card resource) {
		this.resource = resource;
	}

	public int getRoll() {
		return roll;
	}

	public void setRoll(int roll) {
		this.roll = roll;
	}

	public void swapExterior(CatanTile tile) {
		int temp = exteriorStart;
		exteriorStart = tile.getExteriorStart();
		tile.setExteriorStart(temp);

		temp = exteriorEnd;
		exteriorEnd = tile.getExteriorEnd();
		tile.setExteriorEnd(temp);
	}

	@Override
	public String toString() {
		return "CatanTile{" +
				"resource=" + resource +
				", roll=" + roll +
				'}';
	}
}

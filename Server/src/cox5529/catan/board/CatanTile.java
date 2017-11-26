package cox5529.catan.board;

import cox5529.catan.Card;

public class CatanTile {

	private Card resource;
	private int roll;

	public CatanTile(Card resource) {
		this.resource = resource;
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

	@Override
	public String toString() {
		return "CatanTile{" +
				"resource=" + resource +
				", roll=" + roll +
				'}';
	}
}

package cox5529.catan;

public enum Card {
	Wood(0), Wheat(2), Stone(3), Brick(4), Sheep(1), All(-1), None(-1);

	private final int value;

	Card(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

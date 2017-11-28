package cox5529.catan.board.building;

public abstract class CatanBuilding {

	private int team;

	public CatanBuilding(int team) {
		this.team = team;
	}

	public abstract void onRoll(int roll);
}

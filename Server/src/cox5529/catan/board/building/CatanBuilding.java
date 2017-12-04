package cox5529.catan.board.building;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cox5529.catan.board.CatanSpace;
import cox5529.catan.player.Player;

public abstract class CatanBuilding {

	protected Player player;
	@JsonIgnore
	protected CatanSpace space;

	public CatanBuilding(Player player) {
		this.player = player;
	}

	public abstract void onRoll(int roll);

	public Player getPlayer() {
		return player;
	}

	public CatanSpace getSpace() {
		return space;
	}

	public void setSpace(CatanSpace space) {
		this.space = space;
	}
}

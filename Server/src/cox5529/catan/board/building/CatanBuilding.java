package cox5529.catan.board.building;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cox5529.catan.board.CatanSpace;
import cox5529.catan.player.Player;

public abstract class CatanBuilding {

	protected Player player;
	@JsonIgnore
	protected CatanSpace space;
	protected String type;

	public CatanBuilding(Player player, String type) {
		this.player = player;
		this.type = type;
	}

	public abstract void onRoll(int roll);

	public String getType() {
		return type;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

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

package cox5529.catan.devcard;

import cox5529.catan.CatanGame;
import cox5529.catan.player.Player;

public abstract class DevelopmentCard {

	private String name;
	private boolean gainedThisTurn;

	public DevelopmentCard(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isGainedThisTurn() {
		return gainedThisTurn;
	}

	public void setGainedThisTurn(boolean gainedThisTurn) {
		this.gainedThisTurn = gainedThisTurn;
	}

	public boolean play(CatanGame game, Player player, String argument) {
		if(doAction(game, player, argument)) {
			game.broadcastConsoleMessage(player.getName() + " has just played a " + name + " development card!");
			return true;
		}
		return false;
	}



	public abstract boolean doAction(CatanGame game, Player player, String argument);
}

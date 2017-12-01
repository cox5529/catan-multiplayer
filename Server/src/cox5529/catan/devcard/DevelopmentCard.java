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

	public void play(CatanGame game, Player player, String argument) {
		game.broadcastConsoleMessage(player.getName() + " has just played a " + name + " development card!");
		doAction(game, player, argument);
	}



	public abstract void doAction(CatanGame game, Player player, String argument);
}

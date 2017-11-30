package cox5529.catan.devcard;

import cox5529.catan.CatanGame;
import cox5529.catan.player.Player;

public abstract class DevelopmentCard {

	private String name;

	public DevelopmentCard(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void play(CatanGame game, Player player) {
		game.broadcastConsoleMessage(player.getName() + " has just played a " + name + " development card!");
		doAction(game, player);
	}

	public abstract void doAction(CatanGame game, Player player);
}

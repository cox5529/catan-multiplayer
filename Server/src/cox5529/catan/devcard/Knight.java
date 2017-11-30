package cox5529.catan.devcard;

import cox5529.catan.CatanGame;
import cox5529.catan.player.Player;

public class Knight extends DevelopmentCard {

	public Knight() {
		super("Knight");
	}

	@Override
	public void doAction(CatanGame game, Player player) {
		game.moveRobber(player);
	}
}

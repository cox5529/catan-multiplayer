package cox5529.catan.devcard;

import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.player.Player;

import java.util.ArrayList;

public class Monopoly extends DevelopmentCard {

	public Monopoly() {
		super("Monopoly");
	}

	@Override
	public void doAction(CatanGame game, Player player) {
		ArrayList<Player> players = game.getPlayers();
		Card resource = player.getMonopolyResource();
		for (Player other : players) {
			if (other != player) {
				other.give(player, resource, 99999);
			}
		}
	}
}

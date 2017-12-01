package cox5529.catan.devcard;

import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.player.Player;

import java.util.ArrayList;
import java.util.Objects;

public class Monopoly extends DevelopmentCard {

	public Monopoly() {
		super("Monopoly");
	}

	@Override
	public void doAction(CatanGame game, Player player, String argument) {
		Card resource;
		switch (argument) {
			case "wheat":
				resource = Card.Wheat;
				break;
			case "stone":
				resource = Card.Stone;
				break;
			case "wood":
				resource = Card.Wood;
				break;
			case "brick":
				resource = Card.Brick;
				break;
			default:
				resource = Card.Sheep;
				break;
		}
		ArrayList<Player> players = game.getPlayers();
		for (Player other : players) {
			if (other != player) {
				other.give(player, resource, 99999);
			}
		}
	}
}

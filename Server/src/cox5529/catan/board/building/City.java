package cox5529.catan.board.building;

import cox5529.catan.Card;
import cox5529.catan.board.CatanTile;
import cox5529.catan.player.Player;

import java.util.ArrayList;

public class City extends CatanBuilding {

	public City(Player player) {
		super(player, "city");
	}

	@Override
	public void onRoll(int roll) {
		ArrayList<CatanTile> tiles = space.getTiles();
		for(CatanTile tile: tiles) {
			if(tile.getResource() != Card.None && tile.getRoll() == roll && !tile.hasRobber()) {
				player.getHand().addCard(tile.getResource());
				player.getHand().addCard(tile.getResource());
			}
		}
	}
}

package cox5529.catan.board.building;

import cox5529.catan.Card;
import cox5529.catan.board.CatanTile;
import cox5529.catan.player.Player;

import java.util.ArrayList;

public class Settlement extends CatanBuilding {

	public Settlement(Player player) {
		super(player);
	}

	@Override
	public void onRoll(int roll) {
		ArrayList<CatanTile> tiles = space.getTiles();
		for(CatanTile tile: tiles) {
			if(tile.getResource() != Card.None) {
				player.getHand().addCard(tile.getResource());
			}
		}
	}
}

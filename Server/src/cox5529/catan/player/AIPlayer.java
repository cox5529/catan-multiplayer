package cox5529.catan.player;

import cox5529.catan.Card;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;

import java.util.ArrayList;

public class AIPlayer extends Player {

	public AIPlayer(int team) {
		super();
		this.team = team;
		this.name = "AI Player";
	}

	@Override
	public void sendGameState(CatanBoard board, ArrayList<Card> hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {

	}
}

package cox5529.catan.player;

import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;

import java.util.ArrayList;

public class AIPlayer extends Player {

	public AIPlayer(int team, CatanGame game) {
		super();
		this.team = team;
		this.name = "AI Player " + team;
		this.game = game;
	}

	@Override
	public void sendGameState(CatanBoard board, ArrayList<Card> hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {

	}

	@Override
	public int[] moveRobber(CatanBoard board, ArrayList<PlayerData> players) {
		return new int[2];
	}

	@Override
	public void doTurn(CatanBoard board, ArrayList<PlayerData> players) {

	}
}

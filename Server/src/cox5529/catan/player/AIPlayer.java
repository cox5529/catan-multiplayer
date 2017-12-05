package cox5529.catan.player;

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
	public void sendGameState(CatanBoard board, Hand hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {

	}

	@Override
	public int[] moveRobber(CatanBoard board, ArrayList<PlayerData> players) {
		return new int[]{(int) (Math.random() * 2 + 2), (int) (Math.random() * 2 + 1), 0};
	}

	@Override
	public void doTurn(CatanBoard board, ArrayList<PlayerData> players) {

	}

	@Override
	public String getPlacement(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards) {
		int diag = (int) (Math.random() * 5 + 1);
		int col = (int) (Math.random() * 5);
		while (board.getTiles()[diag][col] == null) {
			diag = (int) (Math.random() * 5 + 1);
			col = (int) (Math.random() * 5);
		}
		int pos = (int) (Math.random() * 6);
		return String.format("%d %d %d %d %d %d", diag, col, pos, diag, col, pos);
	}

	@Override
	public int[] sendTradeOffer(CatanBoard board, ArrayList<PlayerData> players, int sourcePlayer, int[] trade) {
		return new int[0];
	}

	@Override
	public int sendTradeResponses(int[][] responses) {
		return -1;
	}

	@Override
	public int[] getDiscard(CatanBoard board, ArrayList<PlayerData> players, int amount) {
		return new int[]{1, 1, 1, 1, 1};
	}
}

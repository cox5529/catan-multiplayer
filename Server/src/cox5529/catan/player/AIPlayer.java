package cox5529.catan.player;

import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.board.CatanSpace;
import cox5529.catan.board.CatanTile;
import cox5529.catan.devcard.DevelopmentCard;

import java.util.ArrayList;
import java.util.Arrays;

public class AIPlayer extends Player {

	private ArrayList<PlayerData> otherPlayers;
	private CatanBoard board;
	private double[] priorities;

	public AIPlayer(int team, CatanGame game) {
		super();
		this.team = team;
		this.name = "AI Player " + team;
		this.game = game;
		this.otherPlayers = new ArrayList<>();
		this.board = game.getBoard();
		priorities = new double[5];
		priorities[0] = Math.random() * 0.5;
		priorities[1] = Math.random() * 0.25;
		priorities[2] = Math.random() * 0.75;
		priorities[3] = Math.random();
		priorities[4] = Math.random() * 0.5;
		normalizePriorities();
	}

	private void normalizePriorities() {
		double total = 0;
		for (int i = 0; i < 5; i++) {
			total += priorities[i];
		}
		for (int i = 0; i < 5; i++) {
			priorities[i] /= total;
		}
	}

	private int[] getPriority() {
		int max[] = new int[5];
		for (int i = 0; i < max.length; i++) {
			max[i] = i;
		}
		for (int i = 1; i < max.length; i++) {
			for (int j = i - 1; j > 0; j--) {
				if (priorities[max[j]] > priorities[max[j - 1]]) {
					int temp = max[j];
					max[j] = max[j - 1];
					max[j - 1] = temp;
				} else break;
			}
		}
		return max;
	}

	private float getSpaceRank(CatanSpace space, int[] ranking) {
		float rank = 0;
		int rollCount = 0;
		ArrayList<Integer> rolls = new ArrayList<>();
		for (CatanTile tile : space.getTiles()) {
			if (tile.getResource() != Card.None) {
				int card = Hand.cardToInt(tile.getResource());
				int r = ranking[card];
				rank += priorities[r] * Math.pow(CatanBoard.getRollCount(tile.getRoll()) / 5.0f, 2);
				if (!rolls.contains(tile.getRoll())) {
					rollCount += tile.getRoll();
					rolls.add(tile.getRoll());
				}
			}
		}
		return rank * rollCount / 15.0f;
	}

	@Override
	public void sendGameState(CatanBoard board, Hand hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {
		this.otherPlayers = players;
	}

	@Override
	public int[] moveRobber(CatanBoard board, ArrayList<PlayerData> players) {
		return new int[]{(int) (Math.random() * 2 + 2), (int) (Math.random() * 2 + 1), (int) (Math.random() * game.getPlayers().size())};
	}

	@Override
	public boolean doTurn(CatanBoard board, ArrayList<PlayerData> players) {
		return true;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public String getPlacement(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards) {
		int[] ranking = getPriority();
		ArrayList<CatanSpace> spaces = board.getSpaces();
		CatanSpace bestSpace = null;
		float bestRank = 0f;
		for (CatanSpace space : spaces) {
			if (board.isValidSettlementLocation(space, team, true)) {
				float rank = getSpaceRank(space, ranking);
				if (bestSpace == null) {
					bestSpace = space;
					bestRank = rank;
				} else if (rank > bestRank) {
					bestRank = rank;
					bestSpace = space;
				}
			}
		}
		System.out.println(team + " " + bestRank);
		System.out.println(Arrays.toString(priorities));
		for (CatanTile tile : bestSpace.getTiles()) {
			Card card = tile.getResource();
			if (card != Card.None) {
				int cardInt = Hand.cardToInt(card);
				priorities[cardInt] /= CatanBoard.getRollCount(tile.getRoll());
			}
		}
		normalizePriorities();
		int diag = bestSpace.getDiagonal();
		int col = bestSpace.getColumn();
		int pos = bestSpace.getPosition();
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

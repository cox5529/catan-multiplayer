package cox5529.catan.player;


import cox5529.catan.Card;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;

import java.util.ArrayList;
import java.util.Collection;

public abstract class Player {

	protected int team;
	protected ArrayList<Card> hand;
	protected ArrayList<DevelopmentCard> devCards;
	protected ArrayList<DevelopmentCard> playedDevCards;

	public Player() {
		hand = new ArrayList<>();
		devCards = new ArrayList<>();
	}

	public abstract void sendGameState(CatanBoard board, ArrayList<Card> hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players);

	public int getTeam() {
		return team;
	}

	public ArrayList<Card> getHand() {
		return hand;
	}

	public ArrayList<DevelopmentCard> getDevCards() {
		return devCards;
	}

	public ArrayList<DevelopmentCard> getPlayedDevCards() {
		return playedDevCards;
	}

	public void setTeam(int team) {
		this.team = team;
	}
}

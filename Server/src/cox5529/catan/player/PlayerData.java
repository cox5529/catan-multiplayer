package cox5529.catan.player;

import cox5529.catan.devcard.DevelopmentCard;

import java.util.ArrayList;

public class PlayerData {

	private int cards;
	private int devCards;
	private int team;
	private ArrayList<DevelopmentCard> playedDevCards;

	public PlayerData() {
		playedDevCards = new ArrayList<>();
	}

	public int getCards() {
		return cards;
	}

	public void setCards(int cards) {
		this.cards = cards;
	}

	public int getDevCards() {
		return devCards;
	}

	public void setDevCards(int devCards) {
		this.devCards = devCards;
	}

	public int getTeam() {
		return team;
	}

	public void setTeam(int team) {
		this.team = team;
	}

	public ArrayList<DevelopmentCard> getPlayedDevCards() {
		return playedDevCards;
	}

	public void setPlayedDevCards(ArrayList<DevelopmentCard> playedDevCards) {
		this.playedDevCards = playedDevCards;
	}
}

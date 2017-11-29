package cox5529.catan.player;


import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;
import org.java_websocket.WebSocket;

import java.util.ArrayList;

public abstract class Player {

	protected int team;
	protected String name;
	protected CatanGame game;
	protected ArrayList<Card> hand;
	protected ArrayList<DevelopmentCard> devCards;
	protected ArrayList<DevelopmentCard> playedDevCards;

	public Player() {
		hand = new ArrayList<>();
		devCards = new ArrayList<>();
		playedDevCards = new ArrayList<>();
		name = "";
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
		if (name.equals("")) name = "TEAM " + team;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CatanGame getGame() {
		return game;
	}

	public void setGame(CatanGame game) {
		this.game = game;
	}

	public AIPlayer toAIPlayer() {//TODO implement
		return null;
	}

	public RemotePlayer toRemotePlayer(WebSocket conn) {
		RemotePlayer player = new RemotePlayer(conn);
		player.setTeam(team);
		player.setGame(game);
		for (Card card : hand)
			player.getHand().add(card);
		for (DevelopmentCard card : playedDevCards)
			player.getPlayedDevCards().add(card);
		for (DevelopmentCard card : devCards)
			player.getDevCards().add(card);
		return player;
	}

	@Override
	public String toString() {
		if (game != null) {
			return String.format("%s %d[%d]", name, game.getId(), team);
		} else {
			return name + " (Idle)";
		}
	}
}

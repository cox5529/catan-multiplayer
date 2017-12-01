package cox5529.catan.player;


import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;
import cox5529.catan.devcard.Knight;
import cox5529.catan.devcard.Monopoly;
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

	public abstract int[] moveRobber(CatanBoard board, ArrayList<PlayerData> players);

	public abstract void doTurn(CatanBoard board, ArrayList<PlayerData> players);

	public void give(Player player, Card resource, int amount) {
		int count = 0;
		for (int i = 0; i < hand.size(); i++) {
			if (hand.get(i) == resource) {
				count++;
				player.getHand().add(hand.get(i));
				hand.remove(i);
				i--;
				if (count == amount) break;
			}
		}
		game.broadcastConsoleMessage(name + " has given " + player.getName() + " " + count + " " + resource + " card" + (amount == 1 ? "." : "s."));
	}

	public void playDevelopmentCard(DevelopmentCard card, String argument) {
		boolean valid = false;
		for (DevelopmentCard c : devCards) {
			if (c.equals(card)) {
				valid = true;
				devCards.remove(c);
				playedDevCards.add(c);
				break;
			}
		}
		if (valid) {
			card.play(game, this, argument);
			game.broadcastGameState();
		} else if (this instanceof RemotePlayer) {
			((RemotePlayer) this).sendConsoleMessage("You do not have a " + card.getName() + " card to play.");
		}
	}


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

	public RemotePlayer toRemotePlayer(RemotePlayer player) {
		player.setTeam(team);
		player.setGame(game);
		player.getHand().clear();
		player.getDevCards().clear();
		player.getPlayedDevCards().clear();
		for (Card card : hand)
			player.getHand().add(card);
		for (DevelopmentCard card : playedDevCards)
			player.getPlayedDevCards().add(card);
		for (DevelopmentCard card : devCards)
			player.getDevCards().add(card);
		player.getDevCards().add(new Knight());
		player.getDevCards().add(new Monopoly());
		player.getHand().add(Card.Brick);
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

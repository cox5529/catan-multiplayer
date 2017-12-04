package cox5529.catan.player;


import com.fasterxml.jackson.annotation.JsonIgnore;
import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.board.CatanLink;
import cox5529.catan.board.CatanSpace;
import cox5529.catan.board.building.CatanBuilding;
import cox5529.catan.board.building.Settlement;
import cox5529.catan.devcard.DevelopmentCard;
import cox5529.catan.devcard.Knight;
import cox5529.catan.devcard.Monopoly;
import org.java_websocket.WebSocket;

import java.lang.reflect.Array;
import java.util.ArrayList;

public abstract class Player {

	protected static final String SETTLEMENT = "SETTLEMENT";
	protected static final String CITY = "CITY";
	protected static final String DEV_CARD = "DEV CARD";
	protected static final String ROAD = "ROAD";

	protected int team;
	protected String name;
	@JsonIgnore
	protected CatanGame game;
	protected Hand hand;
	protected ArrayList<DevelopmentCard> devCards;
	protected ArrayList<DevelopmentCard> playedDevCards;
	@JsonIgnore
	protected ArrayList<CatanBuilding> buildings;

	public Player() {
		hand = new Hand();
		devCards = new ArrayList<>();
		playedDevCards = new ArrayList<>();
		buildings = new ArrayList<>();
		name = "";
	}

	public abstract void sendGameState(CatanBoard board, Hand hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players);

	public abstract int[] moveRobber(CatanBoard board, ArrayList<PlayerData> players);

	public abstract void doTurn(CatanBoard board, ArrayList<PlayerData> players);

	public abstract String getPlacement(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards);

	public void place(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards) {
		String[] data = getPlacement(board, players, giveCards).split(" ");
		int spaceDiag = Integer.parseInt(data[0]);
		int spaceCol = Integer.parseInt(data[1]);
		int spaceId = Integer.parseInt(data[2]);
		int linkDiag = Integer.parseInt(data[3]);
		int linkCol = Integer.parseInt(data[4]);
		int linkId = Integer.parseInt(data[5]);

		CatanSpace space = board.findSpace(spaceDiag, spaceCol, spaceId);
		CatanLink link = board.findLink(linkDiag, linkCol, linkId);
		if (board.isValidPlacementLocation(linkDiag, linkCol, linkId, spaceDiag, spaceCol, spaceId)) {
			Settlement settlement = new Settlement(this);
			space.setBuilding(settlement);
			settlement.setSpace(space);
			link.setRoad(team);
		} else {
			place(board, players, giveCards);
		}
	}

	public final void onTurn(CatanBoard board, ArrayList<PlayerData> players) {
		for (DevelopmentCard developmentCard : devCards) {
			developmentCard.setGainedThisTurn(false);
		}
		doTurn(board, players);
	}

	public final int buy(String object) {
		if (object.equals(DEV_CARD)) {
			if (hand.getCount(Card.Sheep) >= 1 && hand.getCount(Card.Stone) >= 1 && hand.getCount(Card.Wheat) >= 1) {
				DevelopmentCard card = game.drawDevCard(this);
				if (card != null) {
					hand.removeCard(Card.Sheep);
					hand.removeCard(Card.Stone);
					hand.removeCard(Card.Wheat);
					devCards.add(card);
					return 0;
				} else {
					return 2;
				}
			} else return 1;
		} else if (object.startsWith(SETTLEMENT)) {
			if (hand.getCount(Card.Sheep) >= 1 && hand.getCount(Card.Brick) >= 1 && hand.getCount(Card.Wheat) >= 1 && hand.getCount(Card.Wood) >= 1) {
				String[] data = object.split(" ");
				int diag = Integer.parseInt(data[1]);
				int col = Integer.parseInt(data[2]);
				int spaceId = Integer.parseInt(data[3]);
				CatanSpace space = game.getBoard().findSpace(diag, col, spaceId);
				if (game.getBoard().isValidSettlementLocation(diag, col, spaceId, team, false)) {
					Settlement settlement = new Settlement(this);
					space.setBuilding(settlement);
					settlement.setSpace(space);
					buildings.add(settlement);
					hand.removeCard(Card.Sheep);
					hand.removeCard(Card.Wood);
					hand.removeCard(Card.Wheat);
					hand.removeCard(Card.Brick);
					return 0;
				} else {
					return 1;
				}
			} else {
				return 2;
			}
		} else if (object.startsWith(ROAD)) {
			if (hand.getCount(Card.Brick) >= 1 && hand.getCount(Card.Wood) >= 1) {
				String[] data = object.split(" ");
				int diag = Integer.parseInt(data[1]);
				int col = Integer.parseInt(data[2]);
				int linkId = Integer.parseInt(data[3]);
				CatanLink link = game.getBoard().findLink(diag, col, linkId);
				if (game.getBoard().isValidRoadLocation(diag, col, linkId, team)) {
					link.setRoad(team);
					hand.removeCard(Card.Brick);
					hand.removeCard(Card.Wood);
					return 0;
				}
				return 1;
			} else {
				return 2;
			}
		}
		return -1;
	}

	public final void give(Player player, Card resource, int amount) {
		int count = 0;
		while (hand.getCount(resource) > 0 && count < amount) {
			hand.removeCard(resource);
			player.getHand().addCard(resource);
			count++;
		}
		game.broadcastConsoleMessage(name + " has given " + player.getName() + " " + count + " " + resource + " card" + (amount == 1 ? "." : "s."));
	}

	public final void playDevelopmentCard(DevelopmentCard card, String argument) {
		boolean valid = false;
		for (DevelopmentCard c : devCards) {
			if (c.equals(card) && !c.isGainedThisTurn()) {
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

	public ArrayList<CatanBuilding> getBuildings() {
		return buildings;
	}

	public int getTeam() {
		return team;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
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
		player.getDevCards().clear();
		player.getPlayedDevCards().clear();
		hand.addCard(Card.Sheep);
		hand.addCard(Card.Stone);
		hand.addCard(Card.Wheat);
		hand.addCard(Card.Wood);
		hand.addCard(Card.Brick);
		player.setHand(hand.copy());
		for (DevelopmentCard card : playedDevCards)
			player.getPlayedDevCards().add(card);
		for (DevelopmentCard card : devCards)
			player.getDevCards().add(card);
		player.getDevCards().add(new Knight());
		player.getDevCards().add(new Monopoly());
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

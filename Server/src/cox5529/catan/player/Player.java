package cox5529.catan.player;


import com.fasterxml.jackson.annotation.JsonIgnore;
import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.board.*;
import cox5529.catan.board.building.CatanBuilding;
import cox5529.catan.board.building.City;
import cox5529.catan.board.building.Settlement;
import cox5529.catan.devcard.DevelopmentCard;
import cox5529.catan.devcard.Knight;
import cox5529.catan.devcard.RoadBuilding;
import cox5529.catan.devcard.VictoryPoint;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Player {

	public static final int TURN_SETTLEMENT = 40;
	public static final int TURN_CITY = 41;
	public static final int TURN_ROAD = 42;
	public static final int TURN_DEV_CARD = 43;
	public static final int TURN_KNIGHT = 44;
	public static final int TURN_MONOPOLY = 45;
	public static final int TURN_YOP = 46;
	public static final int TURN_RB = 47;

	protected int team;
	protected String name;
	@JsonIgnore
	protected CatanGame game;
	protected Hand hand;
	protected ArrayList<DevelopmentCard> devCards;
	protected ArrayList<DevelopmentCard> playedDevCards;
	@JsonIgnore
	protected ArrayList<CatanBuilding> buildings;
	@JsonIgnore
	protected ArrayList<CatanLink> roads;

	public Player() {
		hand = new Hand();
		devCards = new ArrayList<>();
		playedDevCards = new ArrayList<>();
		buildings = new ArrayList<>();
		roads = new ArrayList<>();
		name = "";
	}

	public abstract void sendGameState(CatanBoard board, Hand hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players);

	public abstract int[] moveRobber(CatanBoard board, ArrayList<PlayerData> players);

	public abstract void doTurn(CatanBoard board, ArrayList<PlayerData> players);

	public abstract String getPlacement(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards);

	public abstract int[] sendTradeOffer(CatanBoard board, ArrayList<PlayerData> players, int sourcePlayer, int[] trade);

	public abstract int sendTradeResponses(int[][] responses);

	public abstract int[] getDiscard(CatanBoard board, ArrayList<PlayerData> players, int amount);

	public final void onSeven(CatanBoard board, ArrayList<PlayerData> players) {
		if (hand.getSize() > 7) {
			int amt = hand.getSize() / 2;
			int a = amt;
			while (amt > 0) {
				int[] discard = getDiscard(board, players, amt);
				for (int i = 0; i < 5; i++) {
					if (hand.getCount(i) >= discard[i]) {
						hand.removeCard(i, discard[i]);
						amt -= discard[i];
					}
				}
			}
			game.broadcastConsoleMessage("The robber has stolen " + a + " cards from " + name + ".");
		}
	}

	public void place(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards) {
		String[] data = getPlacement(board, players, giveCards).split(" ");
		int spaceDiag = Integer.parseInt(data[0]);
		int spaceCol = Integer.parseInt(data[1]);
		int spaceId = Integer.parseInt(data[2]);
		int linkDiag = Integer.parseInt(data[3]);
		int linkCol = Integer.parseInt(data[4]);
		int linkId = Integer.parseInt(data[5]);

		if (board.isValidPlacementLocation(linkDiag, linkCol, linkId, spaceDiag, spaceCol, spaceId)) {
			CatanLink link = board.findLink(linkDiag, linkCol, linkId);
			link.setRoad(team);
			roads.add(link);
			buildSettlement(spaceDiag, spaceCol, spaceId);
			CatanSpace space = board.findSpace(spaceDiag, spaceCol, spaceId);
			for (CatanTile tile : space.getTiles()) {
				if (tile.getResource() != Card.None) {
					hand.addCard(tile.getResource());
				}
			}
		} else {
			place(board, players, giveCards);
		}
	}

	private void buildSettlement(int diag, int col, int spaceId) {
		CatanSpace space = game.getBoard().findSpace(diag, col, spaceId);
		Settlement settlement = new Settlement(this);
		buildings.add(settlement);
		space.setBuilding(settlement);
		settlement.setSpace(space);
	}

	public final void onTurn(CatanBoard board, ArrayList<PlayerData> players) {
		for (DevelopmentCard developmentCard : devCards) {
			developmentCard.setGainedThisTurn(false);
		}
		doTurn(board, players);
	}

	public final int playerTrade(String data) {
		String[] split = data.split(" ");
		int[] amounts = new int[10];
		for (int i = 0; i < split.length; i++) {
			amounts[i] = Integer.parseInt(split[i]);
		}
		int[][] responses = game.doTrade(this, amounts);
		int tradeId = sendTradeResponses(responses);
		if (tradeId != -1) {
			int playerId = tradeId;
			if (tradeId >= team) playerId++;
			Player player = game.getPlayers().get(playerId);
			int[] trade = responses[tradeId];
			if (trade.length == 10) {
				// make sure both players have resources
				for (int i = 0; i < 5; i++) {
					if (hand.getCount(i) < amounts[i]) {
						return -1;
					}
				}
				for (int i = 5; i < 10; i++) {
					if (player.getHand().getCount(i - 5) < amounts[i]) {
						return -1;
					}
				}
				// trade resources
				for (int i = 0; i < 5; i++) {
					hand.removeCard(i, amounts[i]);
					player.getHand().addCard(i, amounts[i]);
				}
				for (int i = 5; i < 10; i++) {
					player.getHand().removeCard(i - 5, amounts[i]);
					hand.addCard(i - 5, amounts[i]);
				}
				game.broadcastGameState();
				return tradeId;
			}
			return -1;
		}
		return -2;
	}

	public final int bankTrade(String data) {
		String[] split = data.split(" ");
		int[] amounts = new int[10];
		for (int i = 0; i < split.length; i++) {
			amounts[i] = Integer.parseInt(split[i]);
		}
		int[] rates = new int[5];
		for (int i = 0; i < rates.length; i++) {
			rates[i] = 4;
		}
		for (CatanBuilding building : buildings) {
			CatanPort port = building.getSpace().getPort();
			if (port != null) {
				Card type = port.getType();
				if (type == Card.All) {
					for (int i = 0; i < rates.length; i++) {
						if (rates[i] == 4) rates[i] = 3;
					}
				} else if (type == Card.Wood) {
					rates[0] = 2;
				} else if (type == Card.Sheep) {
					rates[1] = 2;
				} else if (type == Card.Wheat) {
					rates[2] = 2;
				} else if (type == Card.Stone) {
					rates[3] = 2;
				} else if (type == Card.Brick) {
					rates[4] = 2;
				}
			}
		}
		for (int i = 0; i < 5; i++) {
			if (amounts[i] != 0 && amounts[i + 5] != 0) {
				if (amounts[i] > amounts[i + 5]) amounts[i] -= amounts[i + 5];
				else amounts[i + 5] -= amounts[i];
			}
			if (amounts[i] % rates[i] != 0) {
				return 1;
			} else if (amounts[i] > hand.getCount(i)) {
				return 2;
			}
		}
		int resourcesGained = 0;
		for (int i = 0; i < 5; i++) {
			resourcesGained += amounts[i] / rates[i];
		}
		int requestedCount = 0;
		for (int i = 5; i < 10; i++) {
			requestedCount += amounts[i];
		}
		if (resourcesGained != requestedCount) return 1;
		for (int i = 0; i < 5; i++) {
			hand.removeCard(i, amounts[i]);
		}
		for (int i = 5; i < 10; i++) {
			hand.addCard(i - 5, amounts[i]);
		}
		return 0;
	}

	public final int buy(int type, String object) {
		if (type == TURN_DEV_CARD) {
			if (hand.getCount(Card.Sheep) >= 1 && hand.getCount(Card.Stone) >= 1 && hand.getCount(Card.Wheat) >= 1) {
				DevelopmentCard card = game.drawDevCard(this);
				if (card != null) {
					hand.removeCard(Card.Sheep);
					hand.removeCard(Card.Stone);
					hand.removeCard(Card.Wheat);
					devCards.add(card);
					game.broadcastGameState();
					return 0;
				} else {
					return 2;
				}
			} else return 1;
		} else if (type == TURN_SETTLEMENT) {
			if (hand.getCount(Card.Sheep) >= 1 && hand.getCount(Card.Brick) >= 1 && hand.getCount(Card.Wheat) >= 1 && hand.getCount(Card.Wood) >= 1) {
				String[] data = object.split(" ");
				int diag = Integer.parseInt(data[0]);
				int col = Integer.parseInt(data[1]);
				int spaceId = Integer.parseInt(data[2]);
				if (game.getBoard().isValidSettlementLocation(diag, col, spaceId, team, false)) {
					buildSettlement(diag, col, spaceId);
					hand.removeCard(Card.Sheep);
					hand.removeCard(Card.Wood);
					hand.removeCard(Card.Wheat);
					hand.removeCard(Card.Brick);
					game.broadcastGameState();
					return 0;
				} else {
					return 1;
				}
			} else {
				return 2;
			}
		} else if (type == TURN_ROAD) {
			if (hand.getCount(Card.Brick) >= 1 && hand.getCount(Card.Wood) >= 1) {
				String[] data = object.split(" ");
				int diag = Integer.parseInt(data[0]);
				int col = Integer.parseInt(data[1]);
				int linkId = Integer.parseInt(data[2]);
				if (game.getBoard().isValidRoadLocation(diag, col, linkId, team)) {
					CatanLink link = game.getBoard().findLink(diag, col, linkId);
					link.setRoad(team);
					roads.add(link);
					hand.removeCard(Card.Brick);
					hand.removeCard(Card.Wood);
					game.broadcastGameState();
					return 0;
				}
				return 1;
			} else {
				return 2;
			}
		} else if (type == TURN_CITY) {
			if (hand.getCount(Card.Wheat) >= 2 && hand.getCount(Card.Stone) >= 3) {
				String[] data = object.split(" ");
				int diag = Integer.parseInt(data[0]);
				int col = Integer.parseInt(data[1]);
				int spaceId = Integer.parseInt(data[2]);
				if (game.getBoard().isValidCityLocation(diag, col, spaceId, team)) {
					CatanSpace space = game.getBoard().findSpace(diag, col, spaceId);
					Settlement settlement = (Settlement) space.getBuilding();
					buildings.remove(settlement);
					City city = new City(this);
					space.setBuilding(city);
					city.setSpace(space);
					buildings.add(city);
					hand.removeCard(Card.Wheat);
					hand.removeCard(Card.Wheat);
					hand.removeCard(Card.Stone);
					hand.removeCard(Card.Stone);
					hand.removeCard(Card.Stone);
					game.broadcastGameState();
					return 0;
				} else {
					return 1;
				}
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
				break;
			}
		}
		if (valid) {
			if (!card.play(game, this, argument)) {
				if (this instanceof RemotePlayer) {
					((RemotePlayer) this).sendConsoleMessage("You cannot play a " + card.getName() + " with those arguments.");
				}
			} else {
				devCards.remove(card);
				playedDevCards.add(card);
				game.broadcastGameState();
				for (DevelopmentCard dc : devCards) dc.setGainedThisTurn(true);
			}
		} else if (this instanceof RemotePlayer) {
			((RemotePlayer) this).sendConsoleMessage("You do not have a " + card.getName() + " card to play.");
		}
	}

	public final int calculateRoadLength() {
		int longest = -1;
		for (CatanLink link : roads) {
			int size = getLongestRoadFromSegment(link, null);
			if (size > longest) longest = size;
		}
		return longest;
	}

	private int getLongestRoadFromSegment(CatanLink link, ArrayList<CatanLink> road) {
		if (road == null) road = new ArrayList<>();
		if (!road.contains(link)) {
			road.add(link);
			CatanSpace front = link.getFrontSpace();
			CatanSpace rear = link.getRearSpace();
			ArrayList<Integer> possible = new ArrayList<>();
			if (front.getBuilding() == null || front.getBuilding().getPlayer().getTeam() == team) {
				for (CatanLink l : front.getLinks()) {
					if (l != link && link.getRoad() == team) {
						ArrayList<CatanLink> r = new ArrayList<>();
						r.addAll(road);
						possible.add(getLongestRoadFromSegment(l, r));
					}
				}
			} else if (rear.getBuilding() == null || rear.getBuilding().getPlayer().getTeam() == team) {
				for (CatanLink l : rear.getLinks()) {
					if (l != link && link.getRoad() == team) {
						ArrayList<CatanLink> r = new ArrayList<>();
						r.addAll(road);
						possible.add(getLongestRoadFromSegment(l, r));
					}
				}
			}
			Collections.sort(possible);
			if (possible.size() == 0) return road.size();
			return possible.get(possible.size() - 1);
		} else return road.size();
	}

	public final int getArmySize() {
		int count = 0;
		for (DevelopmentCard card : playedDevCards) {
			if (card instanceof Knight) count++;
		}
		return count;
	}

	public final int countVictoryPoints() {
		int vp = 0;
		for (DevelopmentCard card : devCards) {
			if (card instanceof VictoryPoint) vp++;
		}
		for (CatanBuilding building : buildings) {
			if (building instanceof Settlement) vp++;
			else if (building instanceof City) vp += 2;
		}
		return vp;
	}

	public ArrayList<CatanBuilding> getBuildings() {
		return buildings;
	}

	public ArrayList<CatanLink> getRoads() {
		return roads;
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
		player.setHand(hand.copy());
		for (DevelopmentCard card : playedDevCards)
			player.getPlayedDevCards().add(card);
		for (DevelopmentCard card : devCards)
			player.getDevCards().add(card);
		for (CatanBuilding building : buildings) {
			player.getBuildings().add(building);
			building.setPlayer(player);
		}
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

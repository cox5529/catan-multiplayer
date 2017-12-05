package cox5529.catan.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cox5529.Utility;
import cox5529.catan.CatanServer;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.*;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class RemotePlayer extends Player {

	public static final int ROBBER = 1;
	public static final int INFORMATION = 2;
	public static final int TRADE = 3;
	public static final int TURN = 4;
	public static final int GAME = 5;
	public static final int RESPONSE = 6;
	public static final int PLACE = 7;

	public static final int ROBBER_DISCARD = 10;
	public static final int ROBBER_PLACE = 11;

	public static final int INFORMATION_CONSOLE = 20;
	public static final int INFORMATION_TEAM = 21;
	public static final int INFORMATION_USERNAME = 22;

	public static final int TRADE_BANK = 30;
	public static final int TRADE_PLAYERS = 31;

	public static final int GAME_START = 50;
	public static final int GAME_JOIN = 51;

	public static final int PLACE_SETTLEMENT = 70;
	public static final int PLACE_ROAD = 71;

	public static final int GAMESTATE_BOARD = 80;
	public static final int GAMESTATE_PLAYERS = 81;
	public static final int GAMESTATE_HAND = 82;
	public static final int GAMESTATE_DEV_CARDS = 83;

	@JsonIgnore
	private WebSocket connection;
	private final Queue<String> response;

	public RemotePlayer(WebSocket connection) {
		super();
		this.connection = connection;
		response = new LinkedList<>();
	}

	public WebSocket getConnection() {
		return connection;
	}

	public void onMessage(CatanServer server, String message) {
		if (message.startsWith("" + GAME_START)) {
			String key = message.substring(2);
			server.startGame(key, this);
		} else if (message.startsWith("" + GAME_JOIN)) {
			String key = message.substring(2);
			server.getGame(key).addPlayer(this);
		} else if (message.startsWith("" + INFORMATION_USERNAME)) {
			this.name = message.substring(2);
		} else if (message.startsWith(RESPONSE + "")) {
			synchronized (response) {
				this.response.add(message.substring(1));
			}
		}
	}

	@Override
	public void sendGameState(CatanBoard board, Hand hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			send(GAMESTATE_BOARD, mapper.writeValueAsString(board));
			send(GAMESTATE_PLAYERS, mapper.writeValueAsString(players));
			send(GAMESTATE_HAND, hand.toJSON());
			send(GAMESTATE_DEV_CARDS, mapper.writeValueAsString(devCards));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int[] moveRobber(CatanBoard board, ArrayList<PlayerData> players) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			send(ROBBER, mapper.writeValueAsString(board));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		waitForResponse();
		String response = this.response.poll();
		String[] coords = response.split(" ");
		int[] re = new int[3];
		re[0] = Integer.parseInt(coords[0]);
		re[1] = Integer.parseInt(coords[1]);
		re[2] = Integer.parseInt(coords[2]);
		return re;
	}

	private void send(int protocol, String message) {
		connection.send(protocol + message);
		Utility.log("Server to " + toString() + ":\t(" + protocol + ") " + message);
	}

	public void sendConsoleMessage(String message) {
		send(INFORMATION_CONSOLE, message);
	}

	private void waitForResponse() {
		int size = 0;
		synchronized (response) {
			size = response.size();
		}
		while (size == 0) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
			}
			synchronized (response) {
				size = response.size();
			}
		}
	}

	@Override
	public void doTurn(CatanBoard board, ArrayList<PlayerData> players) {
		while (true) {
			sendGameState(board, hand, devCards, game.buildPlayerData());
			send(TURN, "");
			waitForResponse();
			String response = this.response.poll();
			if (response.equals("")) {
				break;
			}
			int protocol = Integer.parseInt("" + response.charAt(0));
			response = response.substring(1);
			char secondary = response.charAt(0);
			if (secondary >= '0' && secondary <= '9') {
				protocol = protocol * 10 + Integer.parseInt(secondary + "");
				response = response.substring(1);
			}
			if (protocol == TURN_DEV_CARD) {
				int res = buy(TURN_DEV_CARD, "");
				if (res == 1) {
					sendConsoleMessage("You do not have the resources to purchase a development card.");
				} else if (res == 2) {
					sendConsoleMessage("There are no development cards left to purchase.");
				}
			} else if (protocol == TURN_KNIGHT) {
				boolean valid = false;
				DevelopmentCard c = null;
				for (DevelopmentCard card : devCards) {
					if (card instanceof Knight && !card.isGainedThisTurn()) {
						valid = true;
						c = card;
						break;
					}
				}
				if (valid) {
					playDevelopmentCard(c, null);
				} else {
					sendConsoleMessage("You do not have a knight card to play.");
				}
			} else if (protocol == TURN_MONOPOLY) {
				boolean valid = false;
				DevelopmentCard c = null;
				for (DevelopmentCard card : devCards) {
					if (card instanceof Monopoly && !card.isGainedThisTurn()) {
						valid = true;
						c = card;
						break;
					}
				}
				if (valid) {
					playDevelopmentCard(c, response);
				} else {
					sendConsoleMessage("You do not have a monopoly card to play.");
				}
			} else if (protocol == TURN_SETTLEMENT) {
				int res = buy(protocol, response);
				if (res == 1) {
					sendConsoleMessage("You cannot place a settlement there.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to build a settlement.");
				} else {
					game.broadcastConsoleMessage(name + " has just built a new settlement.");
				}
			} else if (protocol == TURN_ROAD) {
				int res = buy(protocol, response);
				if (res == 1) {
					sendConsoleMessage("You cannot place a road there.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to build a road.");
				} else {
					game.broadcastConsoleMessage(name + " has just built a new road.");
				}
			} else if (protocol == TURN_CITY) {
				int res = buy(protocol, response);
				if (res == 1) {
					sendConsoleMessage("You cannot place a city there.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to build a city.");
				} else {
					game.broadcastConsoleMessage(name + " has just built a new city.");
				}
			} else if (protocol == TRADE_BANK) {
				int res = bankTrade(response);
				if (res == 1) {
					sendConsoleMessage("You have submitted an invalid trade.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to make that trade.");
				} else {
					String[] split = response.split(" ");
					int[] amounts = new int[10];
					for (int i = 0; i < split.length; i++) {
						amounts[i] = Integer.parseInt(split[i]);
					}
					String trade = "";
					boolean added = false;
					for (int i = 0; i < 5; i++) {
						if (amounts[i] > 0) {
							if (added) trade += " + ";
							trade += amounts[i] + " x " + Hand.intToCard(i);
							added = true;
						}
					}
					trade += " for ";
					added = false;
					for (int i = 5; i < 10; i++) {
						if (amounts[i] > 0) {
							if (added) trade += " + ";
							trade += amounts[i] + " x " + Hand.intToCard(i - 5);
							added = true;
						}
					}
					game.broadcastConsoleMessage(name + " has just traded with the bank: " + trade + ".");
				}
			} else if (protocol == TRADE_PLAYERS) {
				int res = playerTrade(response);
				if (res == -1) {
					sendConsoleMessage("You have submitted an invalid trade.");
				} else if (res == -2) {
					game.broadcastConsoleMessage(name + " has cancelled his trade.");
				} else {
					String[] split = response.split(" ");
					int[] amounts = new int[10];
					for (int i = 0; i < split.length; i++) {
						amounts[i] = Integer.parseInt(split[i]);
					}
					String trade = "";
					boolean added = false;
					for (int i = 0; i < 5; i++) {
						if (amounts[i] > 0) {
							if (added) trade += " + ";
							trade += amounts[i] + " x " + Hand.intToCard(i);
							added = true;
						}
					}
					trade += " for ";
					added = false;
					for (int i = 5; i < 10; i++) {
						if (amounts[i] > 0) {
							if (added) trade += " + ";
							trade += amounts[i] + " x " + Hand.intToCard(i - 5);
							added = true;
						}
					}
					game.broadcastConsoleMessage(name + " has just traded with " + game.getPlayers().get(res).getName() + ": " + trade + ".");
				}
			} else if(protocol == TURN_RB) {
				boolean valid = false;
				DevelopmentCard c = null;
				for (DevelopmentCard card : devCards) {
					if (card instanceof RoadBuilding && !card.isGainedThisTurn()) {
						valid = true;
						c = card;
						break;
					}
				}
				if (valid) {
					playDevelopmentCard(c, response);
				} else {
					sendConsoleMessage("You do not have a Road Building card to play.");
				}
			} else if(protocol == TURN_YOP) {
				boolean valid = false;
				DevelopmentCard c = null;
				for (DevelopmentCard card : devCards) {
					if (card instanceof YearOfPlenty && !card.isGainedThisTurn()) {
						valid = true;
						c = card;
						break;
					}
				}
				if (valid) {
					playDevelopmentCard(c, response);
				} else {
					sendConsoleMessage("You do not have a Year of Plenty card to play.");
				}
			} else {
				Utility.log("Unknown message type: " + response);
			}
		}
	}

	@Override
	public int[] sendTradeOffer(CatanBoard board, ArrayList<PlayerData> players, int sourcePlayer, int[] trade) {
		String json = "";
		json += "{";
		json += "\"source\":" + sourcePlayer + ",";
		json += "\"trade\":" + Arrays.toString(trade);
		json += "}";
		send(TRADE, json);
		waitForResponse();
		String response = this.response.poll().substring(2);
		if (response.equals("reject")) {
			return new int[0];
		}
		String[] data = response.split(" ");
		int[] amounts = new int[10];
		for (int i = 0; i < data.length; i++) {
			amounts[i] = Integer.parseInt(data[i]);
		}
		return amounts;
	}

	@Override
	public int sendTradeResponses(int[][] responses) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			send(RESPONSE * 10 + TRADE, mapper.writeValueAsString(responses));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		waitForResponse();
		String response = this.response.poll();
		String prefix = TRADE + "";
		if (response.startsWith(prefix)) {
			return Integer.parseInt(response.substring(prefix.length() + 1));
		}
		return -1;
	}

	@Override
	public int[] getDiscard(CatanBoard board, ArrayList<PlayerData> players, int amount) {
		send(ROBBER_DISCARD, amount + "");
		waitForResponse();
		String response = this.response.poll();
		if (response.startsWith(ROBBER_DISCARD + "")) {
			String[] data = response.substring(3).split(" ");
			int[] re = new int[5];
			for (int i = 0; i < 5; i++) {
				re[i] = Integer.parseInt(data[i]);
			}
			sendGameState(board, hand, devCards, players);
			return re;
		}
		return new int[5];
	}

	@Override
	public void setTeam(int team) {
		this.team = team;
		send(INFORMATION_TEAM, team + "");
	}

	@Override
	public String getPlacement(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards) {
		send(PLACE, "");
		String prefix = PLACE_SETTLEMENT + "";
		String re;
		String response;
		do {
			waitForResponse();
			response = this.response.poll();
		} while (!response.startsWith(prefix));
		re = response.substring(prefix.length());
		prefix = PLACE_ROAD + "";
		do {
			waitForResponse();
			response = this.response.poll();
		} while (!response.startsWith(prefix));
		re += " " + response.substring(prefix.length());
		return re;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}

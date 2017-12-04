package cox5529.catan.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cox5529.Utility;
import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.CatanServer;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;
import cox5529.catan.devcard.Knight;
import cox5529.catan.devcard.Monopoly;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class RemotePlayer extends Player {

	public static final String GAMESTATE = "STATE";
	public static final String GAMESTATE_BOARD = "BOARD";
	public static final String GAMESTATE_PLAYERS = "PLAYERS";
	public static final String GAMESTATE_HAND = "HAND";
	public static final String START_GAME = "GAME START";
	public static final String JOIN_GAME = "GAME JOIN";
	public static final String USERNAME = "USERNAME";
	public static final String CONSOLE = "CONSOLE";
	public static final String ROBBER = "ROBBER";
	public static final String RESPONSE = "RESPONSE";
	public static final String TURN = "TURN";
	public static final String GAMESTATE_DEV_CARDS = "DEV CARDS";
	public static final String KNIGHT = "KNIGHT";
	public static final String MONOPOLY = "MONOPOLY";
	public static final String YOP = "YOP";
	public static final String RB = "RB";
	public static final String PLACE = "PLACE";
	public static final String TEAM = "TEAM";
	public static final String TRADE = "TRADE";
	public static final String BANK = "BANK";


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
		if (message.startsWith(START_GAME)) {
			String key = message.substring(START_GAME.length() + 1);
			server.startGame(key, this);
		} else if (message.startsWith(JOIN_GAME)) {
			String key = message.substring(JOIN_GAME.length() + 1);
			server.getGame(key).addPlayer(this);
		} else if (message.startsWith(USERNAME)) {
			this.name = message.substring(USERNAME.length() + 1);
		} else if (message.startsWith(RESPONSE)) {
			synchronized (response) {
				this.response.add(message.substring(RESPONSE.length() + 1));
			}
		}
	}

	@Override
	public void sendGameState(CatanBoard board, Hand hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			send(GAMESTATE + " " + GAMESTATE_BOARD, mapper.writeValueAsString(board));
			send(GAMESTATE + " " + GAMESTATE_PLAYERS, mapper.writeValueAsString(players));
			send(GAMESTATE + " " + GAMESTATE_HAND, hand.toJSON());
			send(GAMESTATE + " " + GAMESTATE_DEV_CARDS, mapper.writeValueAsString(devCards));
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
		int[] re = new int[2];
		re[0] = Integer.parseInt(coords[0]);
		re[1] = Integer.parseInt(coords[1]);
		return re;
	}

	private void send(String protocol, String message) {
		connection.send(protocol);
		connection.send(message);
		Utility.log("Server to " + toString() + ":\t(" + protocol + ") " + message);
	}

	public void sendConsoleMessage(String message) {
		send(CONSOLE, message);
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
			if (response.equals(TURN)) {
				break;
			} else if (response.equals(DEV_CARD)) {
				int res = buy(DEV_CARD);
				if (res == 1) {
					sendConsoleMessage("You do not have the resources to purchase a development card.");
				} else if (res == 2) {
					sendConsoleMessage("There are no development cards left to purchase.");
				}
			} else if (response.equals(KNIGHT)) {
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
			} else if (response.startsWith(MONOPOLY)) {
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
					playDevelopmentCard(c, response.substring(MONOPOLY.length() + 1));
				} else {
					sendConsoleMessage("You do not have a monopoly card to play.");
				}
			} else if (response.startsWith(SETTLEMENT)) {
				int res = buy(response);
				if (res == 1) {
					sendConsoleMessage("You cannot place a settlement there.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to build a settlement.");
				} else {
					game.broadcastConsoleMessage(name + " has just built a new settlement.");
				}
			} else if (response.startsWith(ROAD)) {
				int res = buy(response);
				if (res == 1) {
					sendConsoleMessage("You cannot place a road there.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to build a road.");
				} else {
					game.broadcastConsoleMessage(name + " has just built a new road.");
				}
			} else if (response.startsWith(CITY)) {
				int res = buy(response);
				if (res == 1) {
					sendConsoleMessage("You cannot place a city there.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to build a city.");
				} else {
					game.broadcastConsoleMessage(name + " has just built a new city.");
				}
			} else if (response.startsWith(TRADE + " " + BANK)) {
				String data = response.substring((TRADE + " " + BANK).length() + 1);
				int res = bankTrade(data);
				if (res == 1) {
					sendConsoleMessage("You have submitted an invalid trade.");
				} else if (res == 2) {
					sendConsoleMessage("You do not have the resources to make that trade.");
				} else {
					String[] split = data.split(" ");
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
			} else if (response.startsWith(TRADE + " " + GAMESTATE_PLAYERS)) {
				String data = response.substring((TRADE + " " + GAMESTATE_PLAYERS).length() + 1);
				int res = playerTrade(data);
				if (res == -1) {
					sendConsoleMessage("You have submitted an invalid trade.");
				} else if (res == -2) {
					game.broadcastConsoleMessage(name + " has cancelled his trade.");
				} else {
					String[] split = data.split(" ");
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
		String response = this.response.poll().substring(TRADE.length() + 1);
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
			send(RESPONSE + " " + TRADE, mapper.writeValueAsString(responses));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		waitForResponse();
		String response = this.response.poll();
		String prefix = TRADE;
		if (response.startsWith(prefix)) {
			return Integer.parseInt(response.substring(prefix.length() + 1));
		}
		return -1;
	}

	@Override
	public void setTeam(int team) {
		this.team = team;
		send(TEAM, team + "");
	}

	@Override
	public String getPlacement(CatanBoard board, ArrayList<PlayerData> players, boolean giveCards) {
		send(PLACE, "");
		String prefix = PLACE + " " + SETTLEMENT;
		String re;
		String response;
		do {
			waitForResponse();
			response = this.response.poll();
		} while (!response.startsWith(prefix));
		re = response.substring(prefix.length() + 1);
		prefix = PLACE + " " + ROAD;
		do {
			waitForResponse();
			response = this.response.poll();
		} while (!response.startsWith(prefix));
		re += " " + response.substring(prefix.length() + 1);
		return re;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}

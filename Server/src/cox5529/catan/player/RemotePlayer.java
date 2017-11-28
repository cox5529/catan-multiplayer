package cox5529.catan.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cox5529.Utility;
import cox5529.catan.Card;
import cox5529.catan.CatanGame;
import cox5529.catan.CatanServer;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class RemotePlayer extends Player {

	public static final String GAMESTATE = "STATE";
	public static final String GAMESTATE_BOARD = "BOARD";
	public static final String GAMESTATE_PLAYERS = "PLAYERS";
	public static final String START_GAME = "GAME START";
	public static final String JOIN_GAME = "GAME JOIN";
	public static final String USERNAME = "USERNAME";
	public static final String CONSOLE = "CONSOLE";

	private WebSocket connection;

	public RemotePlayer(WebSocket connection) {
		this.connection = connection;
	}

	public WebSocket getConnection() {
		return connection;
	}

	public void onMessage(CatanServer server, String message) {
		if (message.startsWith(START_GAME)) {
			String key = message.substring(START_GAME.length() + 1);
			CatanGame game = new CatanGame();
			server.addGame(key, game);
			game.addPlayer(this);
		} else if (message.startsWith(JOIN_GAME)) {
			String key = message.substring(JOIN_GAME.length() + 1);
			server.getGame(key).addPlayer(this);
		} else if (message.startsWith(USERNAME)) {
			this.name = message.substring(USERNAME.length() + 1);
		}
	}

	@Override
	public void sendGameState(CatanBoard board, ArrayList<Card> hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			send(GAMESTATE + " " + GAMESTATE_BOARD, mapper.writeValueAsString(board));
			send(GAMESTATE + " " + GAMESTATE_PLAYERS, mapper.writeValueAsString(players));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void send(String protocol, String message) {
		connection.send(protocol);
		connection.send(message);
		Utility.log("Server to " + toString() + ":\t(" + protocol + ") " + message);
	}

	public void sendConsoleMessage(String message) {
		send(CONSOLE, message);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}

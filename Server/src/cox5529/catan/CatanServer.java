package cox5529.catan;

import cox5529.Utility;
import cox5529.catan.player.RemotePlayer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class CatanServer extends WebSocketServer {

	private int nextGameID;

	private HashMap<WebSocket, RemotePlayer> players;
	private HashMap<String, CatanGame> games;
	private ArrayList<Thread> threads;

	public CatanServer(int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
		players = new HashMap<>();
		games = new HashMap<>();
		threads = new ArrayList<>();
		nextGameID = 0;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		Utility.log("Player connected");
		players.put(conn, new RemotePlayer(conn));
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		RemotePlayer player = players.get(conn);
		player.getGame().removePlayer(player, "Disconnected.");
		players.remove(player);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		if (players.containsKey(conn)) {
			RemotePlayer player = players.get(conn);
			Utility.log(player + " says:\t" + message);
			player.onMessage(this, message);
		} else {
			Utility.log("Message was not passed on to player");
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {

	}

	@Override
	public void onStart() {

	}

	public void startGame(String key, RemotePlayer creator) {
		CatanGame game = new CatanGame();
		game.setId(nextGameID);
		nextGameID++;
		games.put(key, game);
		game.addPlayer(creator);
		Thread t = new Thread(game);
		threads.add(t);
		t.start();
	}

	public CatanGame getGame(String name) {
		return games.get(name);
	}
}

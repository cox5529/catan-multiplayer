package cox5529.catan.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cox5529.catan.Card;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.devcard.DevelopmentCard;
import org.java_websocket.WebSocket;

import java.util.ArrayList;

public class RemotePlayer extends Player {

	public static final String GAMESTATE = "STATE";
	public static final String GAMESTATE_BOARD = "BOARD";

	private WebSocket connection;

	public RemotePlayer(WebSocket connection) {
		this.connection = connection;
	}

	@Override
	public void sendGameState(CatanBoard board, ArrayList<Card> hand, ArrayList<DevelopmentCard> devCards, ArrayList<PlayerData> players) {
		System.out.println("Sending game state");
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(board);
			connection.send(GAMESTATE + " " + GAMESTATE_BOARD);
			connection.send(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}

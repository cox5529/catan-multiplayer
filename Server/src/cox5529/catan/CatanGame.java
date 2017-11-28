package cox5529.catan;


import cox5529.catan.board.CatanBoard;
import cox5529.catan.player.Player;
import cox5529.catan.player.PlayerData;
import cox5529.catan.player.RemotePlayer;

import java.util.ArrayList;

public class CatanGame {

	private CatanBoard board;
	private int id;
	private ArrayList<Player> players;

	public CatanGame() {
		board = CatanBoard.generate();
		players = new ArrayList<>();
	}

	public void addPlayer(Player player) {
		player.setGame(this);
		players.add(player);
		player.setTeam(players.size() - 1);
		for (Player p : players)
			p.sendGameState(board, p.getHand(), p.getDevCards(), buildPlayerData());
		broadcastConsoleMessage(player.getName() + " has joined the game");
	}

	private ArrayList<PlayerData> buildPlayerData() {
		ArrayList<PlayerData> data = new ArrayList<>();
		for (Player player : players) {
			PlayerData pdata = new PlayerData();
			pdata.setTeam(pdata.getTeam());
			pdata.setCards(player.getHand().size());
			pdata.setDevCards(player.getDevCards().size());
			pdata.setPlayedDevCards(player.getPlayedDevCards());
			data.add(pdata);
		}
		return data;
	}

	public void broadcastConsoleMessage(String message) {
		for (Player player : players) {
			if (player instanceof RemotePlayer) ((RemotePlayer) player).sendConsoleMessage(message);
		}
	}

	public CatanBoard getBoard() {
		return board;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

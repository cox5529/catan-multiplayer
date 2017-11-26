package cox5529.catan;


import cox5529.catan.board.CatanBoard;
import cox5529.catan.player.Player;
import cox5529.catan.player.PlayerData;

import java.util.ArrayList;

public class CatanGame {

	private CatanBoard board;
	private ArrayList<Player> players;

	public CatanGame() {
		board = CatanBoard.generate();
		players = new ArrayList<>();
	}

	public void addPlayer(Player player) {
		players.add(player);
		player.setTeam(players.size() - 1);
		player.sendGameState(board, player.getHand(), player.getDevCards(), buildPlayerData());
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

	public CatanBoard getBoard() {
		return board;
	}
}

package cox5529.catan;


import cox5529.catan.board.CatanBoard;
import cox5529.catan.board.Robber;
import cox5529.catan.player.AIPlayer;
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
		for (int i = 0; i < 4; i++) {
			players.add(new AIPlayer(i, this));
		}
	}

	public void addPlayer(RemotePlayer player) {
		player.setGame(this);
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) instanceof AIPlayer) {
				RemotePlayer remotePlayer = players.get(i).toRemotePlayer(player.getConnection());
				remotePlayer.setName(player.getName());
				players.set(i, remotePlayer);
				break;
			}
		}
		broadcastGameState();
		broadcastConsoleMessage(player.getName() + " has joined the game");
	}

	public void removePlayer(Player player, String reason) {
		players.remove(player);
		broadcastGameState();
		broadcastConsoleMessage(player.getName() + " has left the game. Reason: " + reason);
	}

	private void broadcastGameState() {
		for (Player p : players)
			p.sendGameState(board, p.getHand(), p.getDevCards(), buildPlayerData());
	}

	private ArrayList<PlayerData> buildPlayerData() {
		ArrayList<PlayerData> data = new ArrayList<>();
		for (Player player : players) {
			PlayerData pdata = new PlayerData();
			pdata.setTeam(player.getTeam());
			pdata.setCards(player.getHand().size());
			pdata.setDevCards(player.getDevCards().size());
			pdata.setPlayedDevCards(player.getPlayedDevCards());
			pdata.setName(player.getName());
			data.add(pdata);
		}
		return data;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void moveRobber(Player player) {
		int[] robberPos = player.moveRobber(board, buildPlayerData());
		Robber robber = board.getRobber();
		while(robberPos[0] == robber.getDiagonal() && robberPos[1] == robber.getColumn()) {
			robberPos = player.moveRobber(board, buildPlayerData());
		}
		board.moveRobber(robberPos[0], robberPos[1]);
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

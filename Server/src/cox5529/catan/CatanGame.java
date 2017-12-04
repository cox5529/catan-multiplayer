package cox5529.catan;


import cox5529.catan.board.CatanBoard;
import cox5529.catan.board.CatanSpace;
import cox5529.catan.board.Robber;
import cox5529.catan.board.building.CatanBuilding;
import cox5529.catan.devcard.*;
import cox5529.catan.player.AIPlayer;
import cox5529.catan.player.Player;
import cox5529.catan.player.PlayerData;
import cox5529.catan.player.RemotePlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CatanGame implements Runnable {

	private CatanBoard board;
	private int id;
	private ArrayList<Player> players;
	private ArrayList<DevelopmentCard> devCardDeck;

	public CatanGame() {
		board = CatanBoard.generate();
		players = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			players.add(new AIPlayer(i, this));
		}
		devCardDeck = new ArrayList<>();
		for (int i = 0; i < 14; i++) {
			devCardDeck.add(new Knight());
		}
		for (int i = 0; i < 5; i++) {
			devCardDeck.add(new VictoryPoint());
		}
		for (int i = 0; i < 2; i++) {
			devCardDeck.add(new Monopoly());
		}
		for (int i = 0; i < 2; i++) {
			devCardDeck.add(new YearOfPlenty());
		}
		for (int i = 0; i < 2; i++) {
			devCardDeck.add(new RoadBuilding());
		}
		Collections.shuffle(devCardDeck);
	}

	public void addPlayer(RemotePlayer player) {
		player.setGame(this);
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) instanceof AIPlayer) {
				RemotePlayer remotePlayer = players.get(i).toRemotePlayer(player);
				remotePlayer.setName(player.getName());
				players.set(i, remotePlayer);
				break;
			}
		}
		broadcastGameState();
		broadcastConsoleMessage(player.getName() + " has joined the game");
	}

	public DevelopmentCard drawDevCard(Player player) {
		if (devCardDeck.size() == 0) {
			return null;
		}
		DevelopmentCard card = devCardDeck.remove(0);
		card.setGainedThisTurn(true);
		broadcastConsoleMessage(player.getName() + " has just bought a development card!");
		return card;
	}

	public void removePlayer(Player player, String reason) {
		players.remove(player);
		broadcastGameState();
		broadcastConsoleMessage(player.getName() + " has left the game. Reason: " + reason);
	}

	public void broadcastGameState() {
		for (Player p : players)
			p.sendGameState(board, p.getHand(), p.getDevCards(), buildPlayerData());
	}

	public ArrayList<PlayerData> buildPlayerData() {
		ArrayList<PlayerData> data = new ArrayList<>();
		for (Player player : players) {
			PlayerData pdata = new PlayerData();
			pdata.setTeam(player.getTeam());
			pdata.setCards(player.getHand().getSize());
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
		while (robberPos[0] == robber.getDiagonal() && robberPos[1] == robber.getColumn()) {
			robberPos = player.moveRobber(board, buildPlayerData());
		}
		board.moveRobber(robberPos[0], robberPos[1]);
		broadcastGameState();
		broadcastConsoleMessage(player.getName() + " has moved the robber!");
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

	private int getDiceRoll() {
		int a = (int) (Math.random() * 6 + 1);
		int b = (int) (Math.random() * 6 + 1);
		return a + b;
	}

	private void doTurn(Player player) {
		int roll = getDiceRoll();
		broadcastGameState();
		broadcastConsoleMessage("It is now " + player.getName() + "'s turn. " + player.getName() + " rolled a " + roll + ".");
		if(roll != 7) {
			for (CatanSpace space : board.getSpaces()) {
				CatanBuilding building = space.getBuilding();
				if (building != null) {
					building.onRoll(roll);
				}
			}
		} else {
			player.moveRobber(board, buildPlayerData());
		}
		player.onTurn(board, buildPlayerData());
	}

	@Override
	public void run() {
		int first = (int) (Math.random() * 4);
		int cur = first;
		do {
			Player player = players.get(cur);
			broadcastConsoleMessage("It is now " + player.getName() + "'s turn to place.");
			broadcastGameState();
			player.place(board, buildPlayerData(), true);
			cur++;
			if (cur == 4) cur = 0;
		} while (cur != first);
		do {
			cur--;
			if (cur == -1) cur = 3;
			Player player = players.get(cur);
			broadcastConsoleMessage("It is now " + player.getName() + "'s turn to place.");
			broadcastGameState();
			player.place(board, buildPlayerData(), true);
		} while (cur != first);
		while (true) {
			for (Player player : players) {
				doTurn(player);
			}
		}
	}
}

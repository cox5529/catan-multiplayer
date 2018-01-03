package cox5529.catan;


import cox5529.Main;
import cox5529.catan.board.CatanBoard;
import cox5529.catan.board.CatanSpace;
import cox5529.catan.board.CatanTile;
import cox5529.catan.board.Robber;
import cox5529.catan.board.building.CatanBuilding;
import cox5529.catan.devcard.*;
import cox5529.catan.player.AIPlayer;
import cox5529.catan.player.Player;
import cox5529.catan.player.PlayerData;
import cox5529.catan.player.RemotePlayer;

import java.util.ArrayList;
import java.util.Collections;

public class CatanGame implements Runnable {

    private CatanBoard board;
    private int id;
    private String winner;
    private String name;
    private final ArrayList<Player> players;
    private ArrayList<DevelopmentCard> devCardDeck;

    private Player longestRoad;
    private Player largestArmy;

    private boolean kill;

    public CatanGame(int playerCount, String name) {
        board = CatanBoard.generateGenetic(100, 100);
        players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
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
        winner = "In progress";
        this.name = name;
    }

    public void addPlayer(RemotePlayer player) {
        player.setGame(this);
        synchronized (players) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i) instanceof AIPlayer) {
                    RemotePlayer remotePlayer = players.get(i).toRemotePlayer(player);
                    remotePlayer.setName(player.getName());
                    players.set(i, remotePlayer);
                    break;
                }
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
        int index = players.indexOf(player);
        players.remove(player);
        AIPlayer ai = player.toAIPlayer();
        players.add(index, ai);
        broadcastGameState();
        broadcastConsoleMessage(player.getName() + " has left the game. Reason: " + reason);
        boolean allAI = true;
        for (Player p : players) {
            allAI &= (p instanceof AIPlayer);
        }
        if (allAI) {
            kill = true;
        }
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
            String name = player.getName();
            if (largestArmy == player) {
                name += " <i class=\"material-icons\">group</i>";
            }
            if (longestRoad == player) {
                name += " <i class=\"material-icons\">map</i>";
            }
            pdata.setName(name);
            data.add(pdata);
        }
        return data;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean moveRobber(Player player) {
        int[] robberPos = player.moveRobber(board, buildPlayerData());
        Robber robber = board.getRobber();
        while (robberPos.length != 0 && robberPos[0] == robber.getDiagonal() && robberPos[1] == robber.getColumn()) {
            boolean validSteal = false;
            CatanTile tile = board.getTiles()[robberPos[0]][robberPos[1]];
            for (CatanSpace space : tile.getSpaces()) {
                if (space.getBuilding() != null && space.getBuilding().getPlayer().getTeam() == robberPos[2]) {
                    validSteal = true;
                    break;
                }
            }
            if (robberPos[2] == -1 || validSteal) {
                break;
            }
            robberPos = player.moveRobber(board, buildPlayerData());
        }
        if (robberPos.length == 0) {
            return false;
        }
        board.moveRobber(robberPos[0], robberPos[1]);
        broadcastGameState();
        broadcastConsoleMessage(player.getName() + " has moved the robber!");
        if (robberPos[2] != -1) {
            Player stolen = players.get(robberPos[2]);
            Card c = stolen.getHand().removeRandomCard();
            player.getHand().addCard(c);
            broadcastConsoleMessage(player.getName() + " has stolen a " + c + " from " + stolen.getName() + ".");
        }
        broadcastGameState();
        return true;
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

    public int[][] doTrade(Player source, int[] trade) {
        int[][] re = new int[players.size() - 1][];
        int idx = 0;
        int sourceId = players.indexOf(source);
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i != sourceId) {
                int[] response = player.sendTradeOffer(board, buildPlayerData(), source.getTeam(), trade);
                while (response.length == 1) {
                    player = players.get(i);
                    response = player.sendTradeOffer(board, buildPlayerData(), source.getTeam(), trade);
                }
                re[idx] = response;
                idx++;
            }
        }
        return re;
    }

    private void doTurn(Player player) {
        int playerId = players.indexOf(player);
        int roll = getDiceRoll();
        broadcastGameState();
        broadcastConsoleMessage("It is now " + player.getName() + "'s turn. " + player.getName() + " rolled a " + roll + ".");
        if (roll != 7) {
            for (CatanSpace space : board.getSpaces()) {
                CatanBuilding building = space.getBuilding();
                if (building != null) {
                    building.onRoll(roll);
                }
            }
        } else {
            Thread[] t = new Thread[players.size()];
            for (Player robbed : players) {
                ArrayList<PlayerData> data = buildPlayerData();
                Runnable r = () -> {
                    int playerId1 = players.indexOf(robbed);
                    Player p = robbed;
                    while (!p.onSeven(board, data)) {
                        p = players.get(playerId1);
                    }
                };
                t[robbed.getTeam()] = new Thread(r);
                t[robbed.getTeam()].start();
            }
            while (true) {
                boolean cont = true;
                for (int i = 0; i < t.length; i++) {
                    if (t[i].isAlive()) cont = false;
                }
                if (cont) break;
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
            }
            while (!moveRobber(player)) {
                player = players.get(playerId);
            }
        }
        while (!player.onTurn(board, buildPlayerData())) {
            player = players.get(playerId);
        }
    }

    public int countPoints(Player player) {
        int vp = player.countVictoryPoints();
        if (player == longestRoad) vp += 2;
        if (player == largestArmy) vp += 2;
        return vp;
    }

    public void broadcastLobby() {
        String message = "[";
        for (Player player : players) {
            if (!message.equals("[")) message += ",";
            message += player.toLobbyObject();
        }
        message += "]";
        for (Player player : players) {
            if (player instanceof RemotePlayer) {
                ((RemotePlayer) player).sendLobby(message);
            }
        }
    }

    public String toJSON() {
        String re = "{";
        re += "\"name\":\"" + name + "\"";
        re += ",\"participants\":[";
        for (int i = 0; i < players.size(); i++) {
            re += "\"" + players.get(i).getName() + "\"";
            if (i != players.size() - 1) re += ",";
        }
        re += "]";
        re += ",\"winner\":\"" + winner + "\"";
        re += ",\"id\":" + id;
        return re + "}";
    }

    private void lobby() {
        boolean ready = false;
        while (!ready) {
            broadcastLobby();
            ready = true;
            synchronized (players) {
                for (Player player : players) {
                    ready &= player.isReady();
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void run() {
        //lobby state
        lobby();
        //game state
        int first = (int) (Math.random() * players.size());
        int cur = first;
        do {
            Player player = players.get(cur);
            broadcastConsoleMessage("It is now " + player.getName() + "'s turn to place.");
            broadcastGameState();
            while (!player.place(board, buildPlayerData(), true)) {
                player = players.get(cur);
            }
            cur++;
            if (cur == players.size()) cur = 0;
        } while (cur != first);
        do {
            cur--;
            if (cur == -1) cur = players.size() - 1;
            Player player = players.get(cur);
            broadcastConsoleMessage("It is now " + player.getName() + "'s turn to place.");
            broadcastGameState();
            while (!player.place(board, buildPlayerData(), false)) {
                player = players.get(cur);
            }
        } while (cur != first);
        boolean game = true;
        while (game && !kill) {
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                doTurn(player);
                int roadLen = player.calculateRoadLength();
                if (roadLen >= 5 && longestRoad != player) {
                    if (longestRoad == null) {
                        longestRoad = player;
                        broadcastConsoleMessage(player.getName() + " now possesses the longest road!");
                    } else if (roadLen > longestRoad.calculateRoadLength()) {
                        longestRoad = player;
                        broadcastConsoleMessage(player.getName() + " now possesses the longest road!");
                    }
                }
                int armySize = player.getArmySize();
                if (armySize >= 3 && largestArmy != player) {
                    if (largestArmy == null) {
                        largestArmy = player;
                        broadcastConsoleMessage(player.getName() + " now possesses the largest army!");
                    } else if (armySize > largestArmy.getArmySize()) {
                        broadcastConsoleMessage(player.getName() + " now possesses the largest army!");
                        largestArmy = player;
                    }
                }
                int vp = countPoints(player);
                if (vp >= 10) {
                    winner = player.getName();
                    broadcastConsoleMessage(player.getName() + " has won the game with " + vp + " victory points!");
                    broadcastGameState();
                    game = false;
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
            }
        }
        if (kill) winner = "No players";
    }
}

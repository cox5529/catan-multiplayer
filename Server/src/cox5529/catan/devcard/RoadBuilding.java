package cox5529.catan.devcard;

import cox5529.catan.CatanGame;
import cox5529.catan.board.CatanLink;
import cox5529.catan.player.Player;

public class RoadBuilding extends DevelopmentCard {

	public RoadBuilding() {
		super("Road Building");
	}

	@Override
	public boolean doAction(CatanGame game, Player player, String argument) {
		String[] dataString = argument.split(" ");
		int[] data = new int[dataString.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = Integer.parseInt(dataString[i]);
		}
		if (game.getBoard().isValidRoadLocation(data[0], data[1], data[2], player.getTeam()) && game.getBoard().isValidRoadLocation(data[3], data[4], data[5], data[0], data[1], data[2], player.getTeam())) {
			CatanLink link = game.getBoard().findLink(data[0], data[1], data[2]);
			link.setRoad(player.getTeam());
			link = game.getBoard().findLink(data[3], data[4], data[5]);
			link.setRoad(player.getTeam());
			return true;
		}
		return false;
	}
}

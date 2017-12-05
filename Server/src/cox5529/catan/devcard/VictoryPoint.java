package cox5529.catan.devcard;

import cox5529.catan.CatanGame;
import cox5529.catan.player.Player;

public class VictoryPoint extends DevelopmentCard {

	public VictoryPoint(){
		super("Victory Point");
	}

	@Override
	public boolean doAction(CatanGame game, Player player, String argument) {
		return true;
	}
}

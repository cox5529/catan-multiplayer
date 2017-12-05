package cox5529.catan.devcard;

import cox5529.catan.CatanGame;
import cox5529.catan.player.Hand;
import cox5529.catan.player.Player;

public class YearOfPlenty extends DevelopmentCard {

	public YearOfPlenty() {
		super("Year of Plenty");
	}

	@Override
	public boolean doAction(CatanGame game, Player player, String argument) {
		String[] stringData = argument.split(" ");
		int[] data = new int[5];
		int total = 0;
		for (int i = 0; i < 5; i++) {
			data[i] = Integer.parseInt(stringData[i]);
			total += data[i];
		}
		if (total != 2) return false;
		Hand hand = player.getHand();
		for (int i = 0; i < 5; i++) {
			hand.addCard(i, data[i]);
		}
		return true;
	}
}

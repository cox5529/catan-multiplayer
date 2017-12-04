package cox5529.catan.player;

import cox5529.catan.Card;

import java.util.HashMap;

public class Hand {

	private HashMap<Card, Integer> hand;

	public Hand() {
		clear();
	}

	public int getSize() {
		return hand.get(Card.Brick) + hand.get(Card.Wheat) + hand.get(Card.Wood) + hand.get(Card.Stone) + hand.get(Card.Sheep);
	}

	public int getCount(Card card) {
		return hand.get(card);
	}

	public int getCount(int card) {
		return getCount(intToCard(card));
	}

	public void addCard(Card card) {
		hand.put(card, hand.get(card) + 1);
	}

	public void addCard(int cardId, int amount) {
		Card card = intToCard(cardId);

		for (int i = 0; i < amount; i++) {
			addCard(card);
		}
	}


	public boolean removeCard(Card card) {
		if (hand.get(card) > 0) {
			hand.put(card, hand.get(card) - 1);
			return true;
		}
		return false;
	}

	public boolean removeCard(int cardId, int amount) {
		Card card = intToCard(cardId);

		if (getCount(card) < amount) return false;
		for (int i = 0; i < amount; i++) {
			removeCard(card);
		}
		return true;
	}

	public void clear() {
		hand = new HashMap<>();
		hand.put(Card.Brick, 0);
		hand.put(Card.Stone, 0);
		hand.put(Card.Sheep, 0);
		hand.put(Card.Wheat, 0);
		hand.put(Card.Wood, 0);
	}

	public static Card intToCard(int input) {
		if (input == 0) return Card.Wood;
		if (input == 1) return Card.Sheep;
		if (input == 2) return Card.Wheat;
		if (input == 3) return Card.Stone;
		if (input == 4) return Card.Brick;
		return Card.None;
	}

	public String toJSON() {
		return String.format("{\"sheep\": %d, \"stone\": %d, \"wheat\": %d, \"wood\": %d, \"brick\": %d}", hand.get(Card.Sheep), hand.get(Card.Stone), hand.get(Card.Wheat), hand.get(Card.Wood), hand.get(Card.Brick));
	}

	public Hand copy() {
		Hand hand = new Hand();
		int res = this.hand.get(Card.Brick);
		for (int i = 0; i < res; i++) {
			hand.addCard(Card.Brick);
		}
		res = this.hand.get(Card.Sheep);
		for (int i = 0; i < res; i++) {
			hand.addCard(Card.Sheep);
		}
		res = this.hand.get(Card.Stone);
		for (int i = 0; i < res; i++) {
			hand.addCard(Card.Stone);
		}
		res = this.hand.get(Card.Wood);
		for (int i = 0; i < res; i++) {
			hand.addCard(Card.Wood);
		}
		res = this.hand.get(Card.Wheat);
		for (int i = 0; i < res; i++) {
			hand.addCard(Card.Wheat);
		}
		return hand;
	}
}

package cox5529.catan.board;

import cox5529.catan.Card;

public class CatanPort {

	private Card type;

	public CatanPort(Card type) {
		this.type = type;
	}

	public Card getType() {
		return type;
	}

	public void setType(Card type) {
		this.type = type;
	}
}

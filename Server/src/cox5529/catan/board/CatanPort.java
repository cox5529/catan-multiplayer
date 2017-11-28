package cox5529.catan.board;

import cox5529.catan.Card;

public class CatanPort {

	private Card type;
	private CatanLink link;

	public CatanPort(Card type) {
		this.type = type;
	}

	public CatanLink getLink() {
		return link;
	}

	public void setLink(CatanLink link) {
		this.link = link;
	}

	public Card getType() {
		return type;
	}

	public void setType(Card type) {
		this.type = type;
	}
}

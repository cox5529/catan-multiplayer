package cox5529.catan.board;

public class CatanLink {

	private CatanSpace frontSpace;
	private CatanSpace rearSpace;

	private int road;

	private int diagonal;
	private int column;
	private int position;

	public CatanLink() {
		road = -1;
	}

	public int getDiagonal() {
		return diagonal;
	}

	public void setDiagonal(int diagonal) {
		this.diagonal = diagonal;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getRoad() {
		return road;
	}

	public void setRoad(int road) {
		this.road = road;
	}

	public CatanSpace getFrontSpace() {
		return frontSpace;
	}

	public void setFrontSpace(CatanSpace frontSpace) {
		this.frontSpace = frontSpace;
	}

	public CatanSpace getRearSpace() {
		return rearSpace;
	}

	public void setRearSpace(CatanSpace rearSpace) {
		this.rearSpace = rearSpace;
	}
}

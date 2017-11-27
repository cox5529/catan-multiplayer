package cox5529.catan.board;

import java.util.ArrayList;

public class CatanSpace {

	private ArrayList<CatanLink> links;
	private ArrayList<CatanTile> tiles;

	private CatanBuilding building;

	private int diagonal;
	private int column;
	private int position;

	public CatanSpace(){
		links = new ArrayList<>();
		tiles = new ArrayList<>();
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

	public void addLink(CatanLink link){
		links.add(link);
	}

	public void addTile(CatanTile tile){
		tiles.add(tile);
	}

	public CatanBuilding getBuilding() {
		return building;
	}

	public void setBuilding(CatanBuilding building) {
		this.building = building;
	}

	public ArrayList<CatanLink> getLinks() {
		return links;
	}

	public ArrayList<CatanTile> getTiles() {
		return tiles;
	}
}

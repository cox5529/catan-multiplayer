package cox5529.catan.board;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cox5529.catan.board.building.CatanBuilding;

import java.util.ArrayList;

public class CatanSpace {

	@JsonIgnore
	private ArrayList<CatanLink> links;
	@JsonIgnore
	private ArrayList<CatanTile> tiles;

	private CatanBuilding building;
	@JsonIgnore
	private CatanPort port;
	@JsonIgnore
	private int diagonal;
	@JsonIgnore
	private int column;
	@JsonIgnore
	private int position;

	public CatanSpace() {
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

	public CatanPort getPort() {
		return port;
	}

	public void setPort(CatanPort port) {
		this.port = port;
	}

	public void addLink(CatanLink link) {
		links.add(link);
	}

	public void addTile(CatanTile tile) {
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

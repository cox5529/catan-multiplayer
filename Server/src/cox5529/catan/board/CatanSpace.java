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

	public CatanSpace() {
		links = new ArrayList<>();
		tiles = new ArrayList<>();
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

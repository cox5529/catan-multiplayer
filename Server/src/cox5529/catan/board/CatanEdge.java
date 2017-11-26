package cox5529.catan.board;

public class CatanEdge {

	private CatanPort[] ports;

	public CatanEdge(CatanPort... ports) {
		this.ports = ports;
	}

	public CatanPort[] getPorts() {
		CatanPort[] ports = new CatanPort[this.ports.length];
		System.arraycopy(this.ports, 0, ports, 0, this.ports.length);
		return ports;
	}
}

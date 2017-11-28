package cox5529;


import cox5529.catan.CatanServer;
import cox5529.catan.board.CatanBoard;

import java.net.UnknownHostException;

public class Main {

	public static void main(String[] args) throws UnknownHostException {
		CatanServer server = new CatanServer(1185);
		server.start();
		//CatanBoard.generate();
	}
}

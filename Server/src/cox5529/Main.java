package cox5529;


import cox5529.catan.CatanGame;
import cox5529.catan.CatanServer;

import java.net.UnknownHostException;

public class Main {

	public static void main(String[] args) throws UnknownHostException {
		CatanServer server = new CatanServer(1185);
		server.start();
	}
}

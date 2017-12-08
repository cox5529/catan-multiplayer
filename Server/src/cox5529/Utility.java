package cox5529;

import java.util.Date;

public class Utility {

	public static void log(String message) {
		if (message.length() < 100) System.out.println(new Date() + ":\t" + message);
		else System.out.println(new Date() + ":\t" + message.substring(0, 100));
	}
}

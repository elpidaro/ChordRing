import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChordRing {
	
	public static int calculate_sha1(String input, int size) throws NoSuchAlgorithmException{
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	    byte[] result = mDigest.digest(input.getBytes());
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < result.length; i++) {
	    	sb.append(String.format("%02x", result[i]));
	    }
	    String asString = sb.toString(); // hexadecimal representation of hash
	    //System.out.println("Hex String of hash = " + asString);
	    BigInteger value = new BigInteger(asString, 16);
	    value = value.mod(BigInteger.valueOf(size));  
	    return value.intValue();

	}
	
	public static void main(String[] args) throws IOException {
		while(true){
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Type your command: ");
			String command = input.readLine();
			System.out.println("Command read: " + command);

		}
		/*Node n = new Node("1", 1000);
		Socket socket = null;
		String message_to_send = null;
		System.out.println(n.getId());
		System.out.println("Starting thread..." + n.getName());
		n.start();
		try {
			socket = new Socket("localhost", 49153);
		} 
		catch (UnknownHostException e) {
		     System.out.println("Unknown host");
		     System.exit(1);
		}
		catch (IOException e) {
			System.out.println("Cannot use this port");
		    System.exit(1);
		}
		System.out.println("Client: Connected");
		OutputStream os = null;
		try {
			os = socket.getOutputStream();
		} catch (IOException e) {
			System.out.println("Couldn't get output stream");
			System.exit(1);
		}
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);
        try {
        	message_to_send = "Hi,server,Do you want a cup of coffe?\n";
			bw.write(message_to_send);
            bw.flush();
		} catch (IOException e) {
			System.out.println("Couldn't write to BufferWriter");
			System.exit(1);
		}
        System.out.println("Client said:" + message_to_send); 

	*/
		
		
	}
}

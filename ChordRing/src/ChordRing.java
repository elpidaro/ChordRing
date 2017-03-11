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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/*
 * COMMAND EXAMPLES:
 * INSERT,eimaste omadara,100
 * INSERT,Love is in the air,97
 * QUERY,eimaste omadara
 * DEPART,708
 * DELETE,Love is in the air
 * DELETE,akuro_pou_den_uparxei
 * QUERY,*
 */

public class ChordRing {
	
	public static int calculate_sha1(String input, int size) throws NoSuchAlgorithmException{
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	    byte[] result = mDigest.digest(input.getBytes());
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < result.length; i++) {
	    	sb.append(String.format("%02x", result[i]));
	    }
	    String asString = sb.toString(); // hexadecimal representation of hash
	    BigInteger value = new BigInteger(asString, 16);
	    value = value.mod(BigInteger.valueOf(size));  
	    return value.intValue();

	}
	
	public static void main_forward_to(String message, String hostname, int port){
		Socket socket = null;
		try {
			socket = new Socket(hostname, port);
		} 
		catch (UnknownHostException e) {
		     System.out.println("Unknown host");
		     System.exit(1);
		}
		catch (IOException e) {
			System.out.println("Cannot use this port");
		    System.exit(1);
		}
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
        	bw.write(message);
            bw.flush();
		} catch (IOException e) {
			System.out.println("Couldn't write to BufferWriter");
			System.exit(1);
		}		
	}
	
	public static void main(String[] args) throws IOException {
		
		
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		//System.out.println("Please enter the <number of desired nodes>: ");
		int number_of_nodes = 5;//Integer.parseInt(input.readLine());
		//System.out.println("Please enter the <log of ring size>: ");
		int M = 10; //Integer.parseInt(input.readLine());
		double ring = Math.pow(2,M);
		int ring_size = (int) Math.round(ring);
		int globalc; // global node counter
		List<Node> nodelist = new ArrayList<Node>();
		
		System.out.printf("Initial number of nodes: %d and ring size: %d\n",number_of_nodes,ring_size);
		
		// create initial ring
		for (globalc=1; globalc<=number_of_nodes; globalc++){
			Node n = new Node("localhost", Integer.toString(globalc), ring_size);
			nodelist.add(n);
		}
		
		fix_nodes(nodelist);
		for (Node n: nodelist){
			System.out.println(n.getmyId());
		}
		for (Node n: nodelist){
			n.start();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			System.out.println("Main couldn't sleep!");
		}
		while(true){
			System.out.println("Type your command: ");
			String command = input.readLine();			
			String option = command.split(",")[0]; //INSERT,QUERY,DELETE,JOIN,DEPART
			if (option.equals("INSERT") || option.equals("DELETE") || option.equals("QUERY")) {
				int len = nodelist.size();
				int randomNum = ThreadLocalRandom.current().nextInt(1, len);
				Node init = nodelist.get(randomNum);
				main_forward_to(command + "-" + init.getMyname()+"-"+ init.getmyPort()+"\n", init.getMyname(), init.getmyPort());
				System.out.println("Main says: I forwarded the command to Node with ID: " + init.getmyId());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					System.out.println("Main couldn't sleep!");
				}
			
			}
			else if (option.equals("DEPART")){
				Node node_to_delete = null;
				int idToDelete = Integer.parseInt(command.split(",")[1]);
				// Check if node with this ID exists
				for (Node n: nodelist){
					if (n.getmyId() == idToDelete){
						node_to_delete = n;
						// send the depart massage
						main_forward_to("DEPART\n", node_to_delete.getMyname(), node_to_delete.getmyPort());
						try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							System.out.println("Main couldn't sleep!");
						}
						break;
					}
				}
				if (node_to_delete == null){
					System.out.println("No node with such ID");
				}
				else {
					// fix nodes
					nodelist.remove(node_to_delete);
					fix_nodes(nodelist);
				}
			}
			else if (option == "JOIN"){
				// do stuff
			}
			
		}
		
		
		
	}

	private static void fix_nodes(List<Node> nodelist) {
		int len = nodelist.size();
		Collections.sort(nodelist);
		for (int i=0; i<len; i++){
			
			if (i == 0){
				// first in ring
				nodelist.get(i).successor = nodelist.get(i+1);
				nodelist.get(i).predecessor = nodelist.get(len - 1);
			}
			else if (i == len - 1){
				// last in ring
				nodelist.get(i).successor = nodelist.get(0);
				nodelist.get(i).predecessor = nodelist.get(i-1);
			}
			else {
				nodelist.get(i).successor = nodelist.get(i+1);
				nodelist.get(i).predecessor = nodelist.get(i-1);
			}
		}
		
	}
}

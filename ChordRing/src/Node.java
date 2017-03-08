import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

//needed for catch(IOException e)
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
//needed for socket setup
import java.net.Socket;
import java.net.SocketOption;

public class Node extends Thread {
	private int myid = 0, successor, predecessor;
	private String myname; // "1", "2", ...
	int ring_size;
	Map<Integer, Integer> files = new HashMap<>();
	

	public Node(String name, int size) {
		myname = name;
		ring_size = size;
	}

	private boolean iAmResponsibleForId (int id){
		if ((id > predecessor && id <= myid) || (myid < predecessor && ((id < myid) || (id > predecessor)))){
			return true;
		}
		else return false;
	}
	
	public long getId() {
		if (myid != 0) return myid;
		else {
			try {
				myid = ChordRing.calculate_sha1(myname, ring_size);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return myid;
		}
	}

	public int getSuccessor() {
		return successor;
	}

	public void setSuccessor(int successor) {
		this.successor = successor;
	}

	public int getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(int predecessor) {
		this.predecessor = predecessor;
	}

	public int getRing_size() {
		return ring_size;
	}

	public void setRing_size(int ring_size) {
		this.ring_size = ring_size;
	}
	
	public String getMyname() {
		return myname;
	}

	public void setMyname(String myname) {
		this.myname = myname;
	}
	
	int query(Command c) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(c.key, ring_size);
		int value = -1; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			value = -2; // i am responsible, song doesn't exist 
			if (files.containsKey(song_id)){
				value = files.get(song_id); // song exists in my list --> value >= 0 returned
			}
		}
		return value;
	}
	
	int insert(Command c) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(c.key, ring_size);
		int answer = 0; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			if (files.containsKey(song_id)){
				// update
				files.replace(song_id, c.value);
			}
			else{
				// insert
				files.put(song_id, c.value);
			}
			answer = 1; // I did the insert
		}
		return answer;
	}
	
	int delete (Command c) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(c.key, ring_size);
		int answer = 0; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			if (files.containsKey(song_id)){
				// delete
				files.remove(song_id);
			}
			answer = 1; // I did the deletion
		}
		return answer;
	}
	
	int set_the_socket_and_listen_it () {
		/* This function is executed by each thread in Chord Ring. 
		 * It setups a socket for each thread (node) and then wait (remains open
		 * and listens for incomming connections) until a depart query 
		 * for this node arrives. 
		 */
		
		// The port in which the connection is set up. 
		// A valid port value is between 0 and 65535
		int port = 64000;
		// The name of this node
		String hostname = myname ;
		try{
			/* Creates a Server Socket with the computer name (hostname) 
			 * and port number (port). 
			 * Each node has a server socket in order to send and receive 
			 * queries.
			 */
			
			ServerSocket serverSocket = new ServerSocket(port);
			
			//Creates a socket address from a hostname and a port number.

			InetSocketAddress myAddress = new InetSocketAddress(hostname , port);
			
		  } catch (IOException e) {
		    System.out.println("Could not listen on port " + port);
		    System.exit(-1);
		  }
		
			/* Reading the message from the client
			 * Method accept() returns when a client has 
			 * connected to server.
			 */
			Socket channel = serverSocket.accept();
        	InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
     
            //If you read from the input stream, you'll hear what the client has to say.
            String number = br.readLine();
            System.out.println("Message received from client is "+number);
		
            
	}

	

	
}

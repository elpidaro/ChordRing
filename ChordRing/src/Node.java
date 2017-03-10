import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

//needed for catch(IOException e)
import java.io.*;
import java.net.ServerSocket;
//needed for socket setup
import java.net.Socket;
import java.net.UnknownHostException;

public class Node extends Thread implements Comparable<Node> {
	public static final int PORT_BASE = 49152;
	private int myid = 0;
	Node successor, predecessor;
	private int myport = 0; // takes values PORT_BASE+1, PORT_BASE+2, ...
	String successorName;
	private String myname; // "localhost" here
	private String seira;
	int ring_size;
	private int arrived = 0;
	private boolean IamInit = false;
	Map<String, Integer> files = new HashMap<>();
	

	public Node(String name, String seiratou, int size) {
		myname = name;
		ring_size = size;
		seira = seiratou;
		myport = PORT_BASE + Integer.parseInt(seiratou);
		try {
			myid = ChordRing.calculate_sha1(seira, ring_size);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private boolean iAmResponsibleForId (int song_id){
		if ((song_id > predecessor.getId() && song_id <= myid) || (myid < predecessor.getId() && ((song_id < myid) || (song_id > predecessor.getId())))){
			return true;
		}
		else return false;
	}
	
	public int getmyId() {
			return myid;
	}
	

	public int getmyPort(){
		return myport;
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
	
	int query(String key) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(key, ring_size);
		int value = -1; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			value = -2; // i am responsible, song doesn't exist 
			if (files.containsKey(song_id)){
				value = files.get(song_id); // song exists in my list --> value >= 0 returned
			}
		}
		return value;
	}
	
	int insert(String key, int value) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(key, ring_size);
		int answer = 0; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			if (files.containsKey(key)){
				// update
				files.replace(key, value);
			}
			else{
				// insert
				files.put(key, value);
			}
			answer = 1; // I did the insert
		}
		return answer;
	}
	
	int delete (String key) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(key, ring_size);
		int answer = 0; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			if (files.containsKey(song_id)){
				// delete
				files.remove(song_id);
				answer = 2; //I had the song and i deleted
			}
			else{
				answer = 1; // The song does not exist
		
			}
		}
		return answer;
	}
	
	public void run () {
		/* This function is executed by each thread in Chord Ring. 
		 * It setups a socket for each thread (node) and then waits (remains open
		 * and listens for incoming connections) until a depart query 
		 * for this node arrives. 
		 */
		
		// The port in which the connection is set up. 
		// A valid port value is between 0 and 65535
		// The name of this node
		ServerSocket serverSocket = null;
		InputStream is = null;
		InputStreamReader isr;
		BufferedReader br = null;
		String message_to_handle = null;
		Socket channel = null;
		/* Creates a Server Socket with the computer name (hostname) 
		 * and port number (port). 
		 * Each node has a server socket in order to send and receive 
		 * queries.
		 */
		
		try {
			serverSocket = new ServerSocket(myport);
		} catch (IOException e) {
            System.err.println("Could not listen on defined port");
            System.exit(1);
        }
			
		while (true){
			// block until a request has arrived
			/* Reading the message from the client
			 * Method accept() returns when a client has 
			 * connected to server.
			 */
			try {
				channel = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed");
	            System.exit(1);
			}
			System.out.println("Server:Connected");
			try {
				is = channel.getInputStream();
			} catch (IOException e) {
				System.err.println("Getting input stream failed");
	            System.exit(1);
			}
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			
			// If you read from the input stream, you'll hear what the client has to say.
			try {
				message_to_handle = br.readLine();
			} catch (IOException e) {
				System.err.println("ReadLine failed");
	            System.exit(1);
			}
	        System.out.println("Message received from client is " + message_to_handle);
	        
	        
	        // decide if I am the initial node who received the query
	        
	        String[] message = message_to_handle.split(" ");
	        // to song mporei na exei kena! Na to doume kai stin praksi
	        if (Integer.parseInt(message[1]) == myid){ // message[1] is the initial host's ID
	        	// I am the initial node
	        	IamInit = true;
	        }
	        String theQuery = message[0]; // keeps what the user entered
	        String []splittedMessage = theQuery.split(",");
	        
	        
	        // decide what to do according to the type of query
	        
	        switch (splittedMessage[0]) {
	        case "INSERT":
	        	if (message.length != 3){
	        		System.err.println("Wrong number of parameters");
	        	}
	        	else {
	        		int insertresult = -1;
					try {
						insertresult = insert(splittedMessage[1], Integer.parseInt(splittedMessage[2]));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						System.exit(1);
					} 
	        		if (insertresult == 1){
	        			// if I did the insert
	        			String myAnswer = "Inserted pair ("+ splittedMessage[1] + "," + splittedMessage[2] + ") at NodeID" + myid;
	        			if (IamInit){
	        				System.out.println(myAnswer);
	        			}
	        			else {
	        				forward_to(myAnswer, message[1], PORT_BASE + ring_size); //message[2] is the initial host's name
	        			}
	        		}
	        		else {
	        			// I didn't do the insert
	        			forward_to(message_to_handle, successorName, successor.getmyPort());
	        			if (IamInit){
	        				 start_listening_for_answers();
	        			}
	        		}
	        	}
	        case "QUERY":
	        	if (message.length != 2){
	        		System.err.println("Wrong number of parameters");
	        	}
	        	else {
	        		//do staff
	        		//new!!!
	        		
	        		try{
	        			int queryresult;  
	        			if (splittedMessage[1] != "*"){
	        				queryresult = query(splittedMessage[1]);
	        				if (queryresult == -2){
	        					// I don't have this but i am rensponsible for this song
	        					String answer = splittedMessage[1]+": Not found";
	        					forward_to(answer, message[1], PORT_BASE + ring_size);
	        					//System.out.println(splittedMessage[1]+": Not found");
	        				}
	        				if (queryresult == -1){
	        					//I am not the responsible node to talk about it :/
	        					//ask the next one :(
	        					forward_to(message_to_handle, successorName, successor.getmyPort());
	        				}
	        				if (queryresult > 0){
	        					//file exists in my list
	        					String answer = "Greetings from Node :" + myid + "I've got this song with value"+queryresult;
	    	        			forward_to(answer, message[1], PORT_BASE + ring_size);
	        					//System.out.println("Greetings from Node :" + myname + "I've got this song with value"+queryresult);
	        				}
	        				if (IamInit){
		        				 start_listening_for_answers();
		        			}
	        				
	        			}
	        			else{
	        				
	        				if (IamInit){
	        					arrived ++;
	        					if (arrived == 2){
		        					//stop
		        					// 
		        					arrived=0;
		        					System.out.println("I am initial node.. finished");
		        				}
	        				}
	        				else{
	        					//I am asking for all songs in all nodes
		        				//print my list first print<java>.files
	        				
	        					for (String key : files.keySet()) {
	        					    System.out.println(key + " " + files.get(key));
	        					}
	        					
	        					//forward the message to the next node
	        					forward_to(message_to_handle, successorName, successor.getmyPort());
	        				}
	        				
	        				
	        				
	        				
	        			}
	        		} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						System.exit(1);
	        		}
	        	}
	        case "DELETE":
	        	if (message.length != 2){
	        		System.err.println("Wrong number of parameters");
	        	}
	        	else {
	        		//do staff
	        		//new!!!
	        		try{
	        			int deleteresult;
	        			System.out.println("Try to delete song"+ splittedMessage[1]);
	        			deleteresult = delete(splittedMessage[1]);
	        			if (deleteresult == 0){
	        				forward_to(message_to_handle, successorName, successor.getmyPort());
	        			}
	        			else{
	        				if (deleteresult ==2){
	        					String answer = "Deleted song"+ splittedMessage[1];
	        					forward_to(answer, message[1], PORT_BASE + ring_size);
	        				}
	        				else{
	        					String answer = "Song Doesn't exist :"+ splittedMessage[1];
	        					forward_to(answer, message[1], PORT_BASE + ring_size);
	        				}
	        				
	        				//System.out.println("Deleted song"+ splittedMessage[1]);
	        			} 
	        			if (IamInit){
	        				 start_listening_for_answers();
	        			}
	        		} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
							System.exit(1);
	        			}
	        		
	        		
	        	}
	        	
	        case "JOIN":
	        	if (message.length != 2){
	        		System.err.println("Wrong number of parameters");
	        	}
	        	else {
	        		//do staff
	        		Node node = new Node("localhost", message[1] ,ring_size);
	        		
	        		
	        	
	        	
	        	}


	        
	        }
		}
		
    	
		
		//Creates a socket address from a hostname and a port number.

		//myAddress = new InetSocketAddress(hostname , port);
		
	}

	// used to sort nodes after every join or depart in main 
	@Override
	public int compareTo(Node nd) {
		int compareId = (int) nd.getId();
		//ascending order
		return (int) (this.getId() - compareId);
		
	}
	
	public void forward_to(String message, String hostname, int port){
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

	public void start_listening_for_answers(){
		ServerSocket serverSocket = null;
		InputStream is = null;
		InputStreamReader isr;
		BufferedReader br = null;
		String answer = null;
		Socket channel = null;
		try {
			serverSocket = new ServerSocket(PORT_BASE + ring_size);
		} catch (IOException e) {
            System.err.println("Could not listen on defined port");
            System.exit(1);
        }
		
		try {
			channel = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed");
            System.exit(1);
		}
		System.out.println("Server:Connected");
		try {
			is = channel.getInputStream();
		} catch (IOException e) {
			System.err.println("Getting input stream failed");
            System.exit(1);
		}
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		
		// If you read from the input stream, you'll hear what the client has to say.
		try {
			answer = br.readLine();
		} catch (IOException e) {
			System.err.println("ReadLine failed");
            System.exit(1);
		}
		System.out.println(answer);
	}

	

	
}

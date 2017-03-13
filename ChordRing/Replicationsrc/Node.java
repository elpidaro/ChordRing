import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private String myname; // "localhost" here
	private String seira;
	private int global_rep;
	int ring_size;
	private int arrived = 0;
	private int have_arrived = 0;
	private boolean IamInit = false;
	Map<String, Integer> files = new HashMap<>();
	

	public Node(String name, String seiratou, int size, int replicasNumbers) {
		myname = name;
		ring_size = size;
		seira = seiratou;
		myport = PORT_BASE + Integer.parseInt(seiratou);
		try {
			myid = ChordRing.calculate_sha1(seira, ring_size);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		global_rep = replicasNumbers;
	}
	
	public String getSeira(){
		return seira;
	}

	private boolean iAmResponsibleForId (int song_id){
		if (song_id > predecessor.getmyId() && song_id <= myid) {
			return true;
		}
		else if (myid < predecessor.getmyId() && (song_id <= myid || song_id > predecessor.getmyId())){
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getmyId() {
			return myid;
	}
	
	public int calculate_replicas_number(String user_input){
		//String []splittedInput = user_input.split("-");
		//int len = splittedInput.length;
		int replicas = Integer.parseInt(user_input);
		return replicas;
		
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
			System.out.println("Node "+ myid + ": I am responsible for : "+ key);
			value = -2; // i am responsible, song doesn't exist 
		}
			if (files.containsKey(key)){
				value = files.get(key); // song exists in my list --> value >= 0 returned
			}
		return value;
	}
	
	int insert(String key, int value,int counter) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(key, ring_size);
		int answer = 0; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			System.out.println("Node "+ myid + ": I am responsible for :" + key);
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
		else if( counter < global_rep){
			System.out.println("Node "+ myid + ": I am replicating :" + key);
			if (files.containsKey(key)){
				// update
				files.replace(key, value);
			}
			else{
				// insert
				files.put(key, value);
			}
			answer=1;
		}
		return answer;
	}
	
	int delete (String key) throws NoSuchAlgorithmException{
		int song_id = ChordRing.calculate_sha1(key, ring_size);
		int answer = 0; // i am not responsible for song
		if (iAmResponsibleForId(song_id)){
			System.out.println("Node "+ myid + ": I am responsible for :" + key);
			if (files.containsKey(key)){
				// delete
				files.remove(key);
				answer = 2; //I had the song and i deleted it
			}
			else{
				answer = 1; // The song does not exist
		
			}
		}
		return answer;
	}
	
	public void run () {
		System.out.println("Node-Thread with id " + myid + " started!\n");
		/* This function is executed by each thread in Chord Ring. 
		 * It setups a socket for each thread (node) and then waits (remains open
		 * and listens for incoming connections) until a depart query 
		 * for this node arrives. 
		 */
		
		// The port in which the connection is set up. 
		// A valid port value is between 0 and 65535
		ServerSocket serverSocket = null;
		InputStream is = null;
		InputStreamReader isr;
		BufferedReader br = null;
		String message_to_handle = null;
		Socket channel = null;
		int replica_counter = 0;
		/* Creates a Server Socket with the computer name (hostname) 
		 * and port number (port). 
		 * Each node has a server socket in order to send and receive 
		 * queries.
		 */
		
			
		while (true){
			try {
				serverSocket = new ServerSocket(myport);
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
			try {
				is = channel.getInputStream();
			} catch (IOException e) {
				System.err.println("Getting input stream failed");
	            System.exit(1);
			}
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			
			
			/* Reading the message from the client
			 * Method accept() returns when a client has 
			 * connected to server.
			 */
			

			// If you read from the input stream, you'll hear what the client has to say.
			try {
				// block until a request has arrived
				message_to_handle = br.readLine();
			} catch (IOException e) {
				System.err.println("ReadLine failed");
	            System.exit(1);
			}	        
		//	System.out.println("Node "+ myid +": Got message: "+ message_to_handle);

	        // decide if I am the initial node who received the query
	        
	        String[] message = message_to_handle.split("-");
	        String theQuery = message[0]; // keeps what the user entered

	        if (!(theQuery.equals("ANSWER"))){
	        replica_counter=calculate_replicas_number(message[1]);
			System.out.println("Node "+ myid +":"+" Replica_counter is now "+replica_counter);
	        }

	     // Check if it is an answer
	        if (theQuery.equals("ANSWER")){
	        	System.out.println("Node "+myid+": "+message[1]);
	        	try {
	    			serverSocket.close();
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	        	continue;
	        }
	        
	        if (message.length == 4){
	        	if (Integer.parseInt(message[3]) == myport){ // message[2] is the initial host's port
	        		// I am the initial node
	        		IamInit = true;
	        	}
	        }
	        String []splittedMessage = theQuery.split(",");
	        // decide what to do according to the type of query
	        if (splittedMessage[0].equals("INSERT")) {
	        	if (splittedMessage.length != 3){
	        		System.err.println("Wrong number of parameters");
	        	}
	        	else {
	        		int insertresult = -1;
					try {
						insertresult = insert(splittedMessage[1], Integer.parseInt(splittedMessage[2]),replica_counter);
						
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						System.exit(1);
					} 
	        		if (insertresult == 1){
	        			// if I did the insert
	        			if (replica_counter>1){
	        				replica_counter--;
	        				forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
	        				try {
	        		            // thread to sleep for 1000 milliseconds
	        		            Thread.sleep(1000);
	        		         } catch (Exception e) {
	        		            System.out.println(e);
	        		           }
	        			}
	        			String myAnswer = "node "+ myid +" Inserted pair ("+ splittedMessage[1] + "," + splittedMessage[2]+")";
	        			if (IamInit){
	        				System.out.println("Node "+ myid + ": " + myAnswer);
	        			}
	        			else {
	        				System.err.println(myAnswer);
	        				forward_to("ANSWER-"+myAnswer+"\n",replica_counter, message[2], Integer.parseInt(message[3])); //message[2] is the initial host's name
	        				try {
	        		            // thread to sleep for 1000 milliseconds
	        		            Thread.sleep(1000);
	        		         } catch (Exception e) {
	        		            System.out.println(e);
	        		         }
	        			}
	        		}
	        		else {
	        			// I didn't do the insert
	        			forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
        				System.out.println("Node "+myid+": I forwarded the query to "+successor.getMyname()+":"+successor.getmyPort());	    
        				try {
        		            // thread to sleep for 1000 milliseconds
        		            Thread.sleep(1000);
        		         } catch (Exception e) {
        		            System.out.println(e);
        		         }
	        		}
	        	}
	        }
	        else if (splittedMessage[0].equals("QUERY")) {

	        	if (splittedMessage.length != 2){
	        		System.err.println("Wrong number of parameters");
	        	}
	        	else {
	        		
        			int queryresult = -6; 
        			
        			if (!(splittedMessage[1].equals("*"))){
        				try {
							queryresult = query(splittedMessage[1]);
	        			}
        				catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.exit(1);
						}
        	//			if(queryresult>0){
//DEBUGGING     			System.out.println(queryresult);
        		//		}
        				if (queryresult == -1){
        					//I am not the responsible node to talk about it :/
        					//ask the next one :(
        					forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
            				System.out.println("Node "+myid+": I forwarded the query to "+successor.getMyname()+":"+successor.getmyPort());
        				}
        				if (queryresult == -2){
        					// I don't have this but i am responsible for this song
        					String answer = "responsible node "+myid+" didn't find "+splittedMessage[1];
        					if (IamInit) System.out.println("Node "+myid+": " +answer);
        					else {
            					forward_to("ANSWER-" +answer+"\n",replica_counter, message[2], Integer.parseInt(message[3]));
            					try {
            			            // thread to sleep for 1000 milliseconds
            			            Thread.sleep(1000);
            			         } catch (Exception e) {
            			            System.out.println(e);
            			         }
        					}
        					System.out.println(splittedMessage[1]+": Not found");
        				}
        				if (queryresult > 0){
        					//file exists in my list
        					String answer = "node " + myid + " said 'I've got this song', value = "+queryresult;
        					replica_counter--;
        					if(!(replica_counter==0)){
        					forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
        					}
        					if (IamInit) System.out.println("Node "+myid+": " +answer);
        					else{
        						forward_to("ANSWER-"+answer+"\n",replica_counter, message[2], Integer.parseInt(message[3]));
        						try {
        				            // thread to sleep for 1000 milliseconds
        				            Thread.sleep(1000);
        				         } catch (Exception e) {
        				            System.out.println(e);
        				         }
        					}
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
        					else{
        						// I am init and I print my list
        						for (String key : files.keySet()) {
            					    System.out.println(key + " " + files.get(key));
        						}
            					forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
            					try {
            			            // thread to sleep for 1000 milliseconds
            			            Thread.sleep(1000);
            			         } catch (Exception e) {
            			            System.out.println(e);
            			         }
        					}
        				}
        				else{
        					// every node answers with its list
        				
        					for (String key : files.keySet()) {
        						forward_to("ANSWER-"+key + " " + files.get(key)+"\n",replica_counter, message[2], Integer.parseInt(message[3]));
        						try {
        				            // thread to sleep for 1000 milliseconds
        				            Thread.sleep(1000);
        				         } catch (Exception e) {
        				            System.out.println(e);
        				         }
        					}
        					
        					//forward the message to the next node
        					forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
        				}
        			}
	        	}
	        }
	        else if (splittedMessage[0].equals("DELETE")) {

	        	if (splittedMessage.length != 2){
	        		System.err.println("Wrong number of parameters");
	        	}
	        	else {
        			int deleteresult = -6;
        			try {
						deleteresult = delete(splittedMessage[1]);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			if (deleteresult == 0){
        				
        				// I am not responsible
        				forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
        				try {
        		            // thread to sleep for 1000 milliseconds
        		            Thread.sleep(1000);
        		         } catch (Exception e) {
        		            System.out.println(e);
        		         }
        			}
        			else if (deleteresult == 2){
        				
        				// I deleted it
        				String answer = "Deleted song"+ splittedMessage[1];
    					if (IamInit) System.out.println("Node "+myid+": " +answer);
    					else{
    						forward_to("ANSWER-"+answer+"\n",replica_counter, message[2], Integer.parseInt(message[3]));
    						try {
    				            // thread to sleep for 1000 milliseconds
    				            Thread.sleep(1000);
    				         } catch (Exception e) {
    				            System.out.println(e);
    				         }
    					}
        			}
        			else if (deleteresult == 1){
        				
        				// Song doesn't exist
        				String answer = "Song Doesn't exist :"+ splittedMessage[1];
    					if (IamInit) System.out.println("Node "+myid+": " +answer);
    					else {
    						forward_to("ANSWER-"+answer+"\n",replica_counter, message[1], Integer.parseInt(message[2]));
    						try {
    				            // thread to sleep for 1000 milliseconds
    				            Thread.sleep(1000);
    				         } catch (Exception e) {
    				            System.out.println(e);
    				         }
    					}
        			}		
	        	}		
	        }
	        else if (splittedMessage[0].equals("DEPART")) {
	        	System.out.println("Node "+myid+": in depart");
	        	for (String key : files.keySet()) {
					forward_to("GET_MY_STUFF-"+key + "," + files.get(key)+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
					try {
						sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	        	try {
					serverSocket.close();
				} catch (IOException e) {
					System.out.println("Socket couldn't close during depart!");
				}
	        	System.out.println("Node "+ myid+": closed socket");
	        	break;
	        }
	        else if (splittedMessage[0].equals("GET_MY_STUFF")) {
	        	String key = message[1].split(",")[0];
	        	int value = Integer.parseInt(message[1].split(",")[1]);
	        	files.put(key, value);
	        }
	        else if (message[0].equals("JOIN")) {
	        	int hisSuccessorID = Integer.parseInt(message[1]);
	        	List<String> to_remove = new ArrayList<String>();
	        	if (myid == hisSuccessorID ){
	        		for (String key : files.keySet()) {
	        			System.out.println(key);
	        			try {
							if (ChordRing.calculate_sha1(key, ring_size) <= predecessor.getmyId()){
								forward_to("GET_MY_STUFF-"+key + "," + files.get(key)+"\n",replica_counter, predecessor.getMyname(), predecessor.getmyPort());
								to_remove.add(key);
								try {
						            // thread to sleep for 1000 milliseconds
						            Thread.sleep(1000);
						         } catch (Exception e) {
						            System.out.println(e);
						         }
							}
						} catch (NoSuchAlgorithmException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	        		for (String s : to_remove){
	        			files.remove(s);
	        		}
	        	}
	        	else {
    				forward_to(message_to_handle+"\n",replica_counter, successor.getMyname(), successor.getmyPort());
    				try {
    		            // thread to sleep for 1000 milliseconds
    		            Thread.sleep(1000);
    		         } catch (Exception e) {
    		            System.out.println(e);
    		         }
	        	}
	        }
		
	        try {
	        	serverSocket.close();
	        } catch (IOException e) {
	        	// TODO Auto-generated catch block
	        	e.printStackTrace();
	        }
		}
	}

	// used to sort nodes after every join or depart in main 
	@Override
	public int compareTo(Node nd) {
		int compareId = (int) nd.getmyId();
		//ascending order
		return (int) (this.getmyId() - compareId);
		
	}
	
	public void forward_to(String message,int replicas, String hostname, int port){
		String message_final = null;
		String []message_with_replicas = message.split("-");
		if(!(message_with_replicas[0].equals("ANSWER"))){
			message_final = message_with_replicas[0]+"-"+replicas+"-"+message_with_replicas[2]+"-"+message_with_replicas[3];
		}
		else{
			message_final = message;
		}
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
			bw.write(message_final);
            bw.flush();
		} catch (IOException e) {
			System.out.println("Node "+myid+": Couldn't write to BufferWriter");
			e.printStackTrace();
			System.exit(1);
		}
	}
}

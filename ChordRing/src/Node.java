import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Node extends Thread {
	private int id = 0, successor, predecessor;
	private String myname; // "1", "2", ...
	int ring_size;
	
	

	public Node(String name, int size) {
		myname = name;
		ring_size = size;
	}


	public long getId() {
		if (id != 0) return id;
		else {
			try {
				this.calculateID();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return id;
		}
	}

	public void setId(int id) {
		this.id = id;
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
	
	
	private void calculateID() throws NoSuchAlgorithmException{
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	    byte[] result = mDigest.digest(myname.getBytes());
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < result.length; i++) {
	    	sb.append(String.format("%02x", result[i]));
	    }
	    String asString = sb.toString(); // hexadecimal representation of hash
	    System.out.println("Hex String of hash = " + asString);
	    BigInteger value = new BigInteger(asString, 16);
	    value = value.mod(BigInteger.valueOf(ring_size));  
	    this.id = value.intValue();

	}
	
}

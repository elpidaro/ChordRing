import java.math.BigInteger;
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
				id = ChordRing.calculate_sha1(myname, ring_size);
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
	
	
}

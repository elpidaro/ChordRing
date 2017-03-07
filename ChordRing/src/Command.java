import java.io.Serializable;

public class Command implements Serializable{
	private static final long serialVersionUID = 1L; // used for compatibility in communication between sockets 
	String key; 
	int value;
	String initial_node_name;
	int type; // 1:QUERY, 2:INSERT, 3:DELETE, 4:JOIN, 5:DEPART
}



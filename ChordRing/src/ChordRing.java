import java.math.BigInteger;
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
	    System.out.println("Hex String of hash = " + asString);
	    BigInteger value = new BigInteger(asString, 16);
	    value = value.mod(BigInteger.valueOf(size));  
	    return value.intValue();

	}
	
	public static void main(String[] args) {
		Node n = new Node("1", 1000);
		System.out.println(n.getId());
	}

}

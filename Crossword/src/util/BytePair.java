package util;

//2 bytes together (not using Pair because it would require to allocate useless references)
public class BytePair {
		
	public BytePair() {
		this.b1=0;
		this.b2=0;
	}
	
	public BytePair(byte b1, byte b2) {
		this.b1=b1;
		this.b2=b2;
	}
	
	
	public byte b1;
	public byte b2;
	
}

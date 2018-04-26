package util;


//a tuple of 2 with public elements
public class Pair<T1,T2> {
	
	public Pair() {
		this.val1=null;
		this.val2=null;
	}
	
	public Pair(T1 val1, T2 val2) {
		this.val1=val1;
		this.val2=val2;
	}
	
	public T1 val1;
	public T2 val2;
}

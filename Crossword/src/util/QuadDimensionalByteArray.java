package util;


/*
 * Using this class instead of a normal four dimensional array, makes it much more efficient, because
 * 
 */
public class QuadDimensionalByteArray {
	
	public QuadDimensionalByteArray(int size1, int size2, int size3, int size4) {
		
		//here we save the sizes
		this.size1=size1;
		this.size2=size2;
		this.size3=size3;
		this.size4=size4;
		
		//we calculate this numbers taking into account that modifying the last parameter by 1, only varies the access also by 1 (faster by cache)
		prod4=1;
		prod3=prod4*size4;
		prod2=prod3*size3;
		prod1=prod2*size2;
		
		//we allocate the memory
		pral = new byte[size1*size2*size3*size4];
		
	}
	
	//size getter
	public int getSize(int num) {
		int ret=0;
		
		switch(num) {
		case 0:
			ret=size1;
			break;
		case 1:
			ret=size2;
			break;
		case 2:
			ret=size3;
			break;
		case 3:
			ret=size4;
			break;
		}
		
		return ret;
	}

	
	//sizes of the different dimensions
	private final int size1;
	private final int size2;
	private final int size3;
	private final int size4;
	
	
	//this numbers will be multiplied for each of the indexes to get the single-dimension array position
	private final int prod1;
	private final int prod2;
	private final int prod3;
	private final int prod4;
	
	//the actual allocated array
	private final byte[] pral;
	
	
	//we return the one dimensional array
	public byte[] get() {
		return pral;
	}
	
	
	public int calcIndex(int ind1, int ind2, int ind3, int ind4) {
		//we calculate the access
		return (ind1*prod1+ind2*prod2+ind3*prod3+ind4*prod4);
	}
	
	
}

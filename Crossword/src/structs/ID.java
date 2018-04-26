package structs;

public class ID {

	
	public final byte number;					//number identificator of the word [1,2,3,4...]
	public final char direction;				//direction of the word, it's either H or V.
	
	public ID(byte number, char direction) {
		this.number=number;
		this.direction=direction;
	}
	
}

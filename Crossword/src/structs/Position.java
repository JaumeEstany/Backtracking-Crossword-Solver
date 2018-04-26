package structs;


//saves a Word reference and a byte
public class Position {
	
	public Position() {
		letterIndex=-1;
		word=null;
	}
	
	public Position(byte letterIndex, Word word) {
		this.letterIndex=letterIndex;
		this.word=word;
	}
	
	private byte letterIndex;
	private Word word;
	
	
	
	public byte getLetterIndex() {
		return letterIndex;
	}
	public void setLetterIndex(byte letterIndex) {
		this.letterIndex = letterIndex;
	}
	public Word getWord() {
		return word;
	}
	public void setWord(Word word) {
		this.word = word;
	}
	
}

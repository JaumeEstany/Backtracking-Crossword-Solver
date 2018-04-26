package structs;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import util.BytePair;

public class Word {
	
	public final ID identificator;			//contains the identification info of a word
	
	public final byte length;				//length of the word
	
	public final LinkedHashMap<Byte, Position> dependences;
	//dictionary of dependences of this word.
	//it has a Hashtable entry for every position that is shared with another position of another word
	

	private String value;			//it's the actual value of the word. if null, no value has been assigned yet
	
	//stores the current domain of this word
	private LinkedList<String> domain;
	
	
	//stack of "extracts" of the domain
	//when a condition is applied to the domain, the removed nodes are stored in a linked list and pushed into this stack
	//when we backtrack thus remove the condition, we pop the list and merge it with the domain
	private Stack< LinkedList <String> > removedDomains;
	
	//this contains the same information as the dependences map, just that this one is indexed by another Word reference
	//and gives you both letter indexes
	//useful to check if a word has a dependence with this one in constant time O(1)
	private Map<Word, BytePair> inversedDependenceMap;
	
	//basic constructor
	public Word(byte number, char direction, byte length){
		this.identificator = new ID(number, direction);
		this.length=length;
		this.dependences= new LinkedHashMap<>();
		this.value=null;
		
		//forward checking case
		this.domain=null;
		this.removedDomains=null;
		this.inversedDependenceMap = null;
		
	}
	
	public void initBeforeForwardChecking(List<String> initialDomain) {
		
		//init all the variable domains with the words of their length (the parameter has to be that)
		initDomain(initialDomain);
		//and finally init the inversedDependenceMap (auxiliar only): has to be done after adding the dependences
		initInversedDependenceMap();
		
	}
	
	//initDomain is only called in the forwardChecking case
	//it initialises the domain of this word
	private void initDomain(List<String> initDomain) {
		
		domain = new LinkedList<String>();
		removedDomains = new Stack< LinkedList <String> >(); 
		for(String str : initDomain) {
			domain.add(str);
		}
	}
	
	//we init the inversed dependence map (see declaration to know what it holds)
	private void initInversedDependenceMap() {
		
		this.inversedDependenceMap = new HashMap<Word, BytePair>(dependences.size());
		
		for(Map.Entry<Byte, Position> entry : this.dependences.entrySet()) {
			inversedDependenceMap.put(	entry.getValue().getWord(),
										new BytePair(entry.getKey(), entry.getValue().getLetterIndex()) );
		}
		
	}
	
	//update domain in the case of letter filter
	//params: this word is being forced to have C value in it's POS position, so we have to remove every word that doesn't fit that 
	public void updateDomain(final String assignedValue, final byte otherPosition, final byte thisPosition) {
		
		//we create an empty list: extractedValues which will be the one whick contains the extracted values from the domain this one time
		LinkedList<String> extractedValues = new LinkedList<>();
		//iter on domain
		Iterator<String> iter = domain.iterator();
		
		final boolean notDependant = (otherPosition<0);			//we calculate this once and save it here
		
		//only if this word is expecting the same word length as the assigned one
		if(assignedValue.length()==length) {
			
			//if the word has no dependence with the assigned-value word, we only check for exact same word to be in the domain (no word repetition)
			if(notDependant) {			//only check to eliminate the exact same string

				//for every word
				while(iter.hasNext()) {
					final String currString = iter.next();
					
					//remove it if the word coincides, then break,
					//because there's no need to keep the search
					if(currString==assignedValue) {
						iter.remove();
						extractedValues.add(currString);
						break;
					}
					
				}
				
			}
			else {							//check to eliminate the string and the unvalid words because of letter dependences
				
				//get it closer
				final char c = assignedValue.charAt(otherPosition);
				
				//for every word in domain
				while(iter.hasNext()) {
					final String currString = iter.next();
					
					//remove it if it's the same or is invalid because of dependence
					if(c!= currString.charAt(thisPosition) || currString==assignedValue) {
						iter.remove();
						extractedValues.add(currString);
					}
					
				}
			}
		}
		else if(!notDependant) {

			//get it closer
			final char c = assignedValue.charAt(otherPosition);
			
			//for every word in domain
			while(iter.hasNext()) {
				final String currString = iter.next();
				
				//remove it if it doesn't fit the dependences 
				if(c!= currString.charAt(thisPosition)) {
					iter.remove();
					extractedValues.add(currString);
				}
					
			}
				
		}
			
			
		
		

		//push the extracted values into the stack
		removedDomains.push(extractedValues);
		
	}
	
	//undo the domain filter
	public void reverseDomain() {
		
		//we pop the list off the stack
		LinkedList<String> extracted = removedDomains.pop();
		
		//we concatenate it the the domain O(1)
		while(!extracted.isEmpty()) {
			domain.add( extracted.removeFirst() );
		}
		
	}
	
	
	public LinkedList<String> getDomain() {
		return domain;
	}
	
	public void setValue(String newVal) {
		value = newVal;
	}
	
	public String getValue() {
		return value;
	}
	
	//checks wheter this word (String) could fit in this space. Checks length and the dependences it has with other words
	public boolean isValidValueLengthCheck(String newValue) {
		
		//if the lengths are different, false
		if(newValue.length()!=this.length) {
			return false;
		}
		
		//for every entry in the dependences dictionary
		for(Map.Entry<Byte, Position> entry : this.dependences.entrySet()) {
			
			//calculate the char that will be in this place of this word
			final char collisionChar = newValue.charAt(entry.getKey());
			
			//get the Word this one collides with
			final Word collisionWord = entry.getValue().getWord();
			
			//get the position of the collision relative to the other Word's start
			final byte collisionPosition = entry.getValue().getLetterIndex();
			
			//if the char doesn't fit, false
			if(!collisionWord.charFits(collisionChar, collisionPosition)) {
				return false;
			}
		}
		
		return true;
	}
	
	//checks wheter this word (String) could fit in this space. Checks only the dependences it has with other words.
	//Giving this function a String with a different length than the Word length, can cause exceptions or sensless results
	public boolean isValidValue(String newValue) {
		
		//for every entry in the dependences dictionary
		for(Map.Entry<Byte, Position> entry : this.dependences.entrySet()) {
			
			//calculate the char that will be in this place of this word
			final char collisionChar = newValue.charAt(entry.getKey());
			
			//get the Word this one collides with
			final Word collisionWord = entry.getValue().getWord();
			
			//get the position of the collision relative to the other Word's start
			final byte collisionPosition = entry.getValue().getLetterIndex();
			
			//if the char doesn't fit, false
			if(!collisionWord.charFits(collisionChar, collisionPosition)) {
				return false;
			}
		}
		
		return true;
	}
	
	//recieves the character that should go in this word's position pos
	public boolean charFits(char c, byte pos) {
		
		//is true when this value is null (can be any value) or when the char is the same we already have, as they has to coincide
		return ((this.value==null) || (this.value.charAt(pos)==c));
	}
	
	
	//equals overriden to only compare references -> we will never have a same-value different-reference instance 
	public boolean equals(Word word) {
		return (word==this);
	}

	
	public Map<Word, BytePair> getInversedDependenceMap() {
		return inversedDependenceMap;
	}
	
	
	
}

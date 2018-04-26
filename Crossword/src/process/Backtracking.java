package process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import structs.Word;
import util.BytePair;


public class Backtracking {
	
	public static boolean backtrackingFind(ArrayList<Word> wordList, int supposedNextIndex, ArrayList<String> dictionary) {
		
		//if we've completed the run-through, return true, as this is the final solution
		if(supposedNextIndex>=wordList.size()) {
			return true;
		}
		
		//the target word we wanna fill in this call
		final Word targetWord = wordList.get(supposedNextIndex);
		
		//for every entry in the dictionary
		for(int i=0;i<dictionary.size();i++) {
			final String entry = dictionary.get(i);								//take the reference "closer"
			if(entry!=null && targetWord.isValidValueLengthCheck(entry)) {					//if the entry is not null (already used) and is fits here, we propague the backtracking
				
				//we set the value and erase it of the possible domain
				targetWord.setValue(entry);
				dictionary.set(i, null);
				
				//we get the below result
				boolean resultFromBelow = backtrackingFind(wordList, supposedNextIndex+1, dictionary);
				
				//if true, we succeded, return true
				if(resultFromBelow) {
					return true;
				}
				//if false, we have to go for another word
				else {
					//we restore the value in the domain, as it's not used anymore for the solution
					targetWord.setValue(null);
					dictionary.set(i, entry);
				}
				
			}
		}
		
		return false;
	}

	private static Word chooseWord(Set<Word> wordSet) {
		
		//starting values MAX_INT and null
		int minDomain=Integer.MAX_VALUE;
		Word ret=null;
		
		//for every word in the not assigned values: get the most restricted variable
		for(Word w : wordSet) {
			if(w.getDomain().size()<minDomain) {
				ret=w;
				minDomain=w.getDomain().size();
			}
		}
		
		return ret;
	}
	
	public static boolean backtrackingFindWithForwardChecking(ArrayList<Word> wordList, Set<Word> wordSet, Map<Byte, ArrayList<String> > dictionary) {
		
		//if the set of not assigned variables is empty, return true
		if(wordSet.isEmpty()) {
			return true;
		}
		
		//get the next word to be assigned
		final Word currWord = chooseWord(wordSet);
		
		//we remove the word from the non used words set
		wordSet.remove(currWord);
		
		
		//we get the inversed dependence map closer 
		final Map<Word, BytePair> inversedDependenceMap = currWord.getInversedDependenceMap();
		
		//for every word in the domain
		LinkedList<String> domain = currWord.getDomain();
		for(String assignedValue : domain) {
			//we set the value
			currWord.setValue( assignedValue );
				
			//we eliminate from every's word domain the just used word
			for(Word w : wordSet) {
				
				//get the dependence between currWord and W (null if no dependence)
				BytePair indexes = inversedDependenceMap.get(w);
				
				//if it's null, it's not dependant, so we pass it -1 -1
				if(indexes==null) {
					//we pass -1 -1 so it only removes the used word
					w.updateDomain(assignedValue, (byte)-1, (byte)-1);
				}
				else {
					//if not null, we pass the 2 indexes so it removes the used word and the invalid ones
					w.updateDomain(assignedValue, indexes.b1, indexes.b2);
				}
			}
				
			//we get the below result
			boolean resultFromBelow = backtrackingFindWithForwardChecking(wordList, wordSet, dictionary);
				
			//if true, we succeded, return true
			if(resultFromBelow) {
				return true;
			}
			//if false, we have to go for another word
			else {
					
				//for every word we undo the word repetition search
				for(Word w : wordSet) {
					w.reverseDomain();
				}

				//we restore the value in the domain, as it's not used anymore for the solution
				currWord.setValue(null);
					
			}
					
		}
		
		//we add this word to the not assigned variables set
		wordSet.add(currWord);
			
		return false;
	}
	
	//here there's a thread pool that we will be using to speed up our code
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	public static boolean backtrackingFindWithForwardCheckingAndMultithread(ArrayList<Word> wordList, Set<Word> wordSet, Map<Byte, ArrayList<String> > dictionary) {
		
		//if the set of not assigned variables is empty, return true
		if(wordSet.isEmpty()) {
			Backtracking.executor.shutdown();
			return true;
		}
		
		//get the next word to be assigned
		final Word currWord = chooseWord(wordSet);
		
		//we remove the word from the non used words set
		wordSet.remove(currWord);
		
		//we get the inversed dependence map closer 
		final Map<Word, BytePair> inversedDependenceMap = currWord.getInversedDependenceMap();
		
		
		//for every word in the domain
		LinkedList<String> domain = currWord.getDomain();
		for(String assignedValue : domain) {
			//we set the value
			currWord.setValue( assignedValue );
			
			//list of callable objects
			List<Callable<?> > runnableList = new ArrayList<Callable<?> >(wordList.size());
			
			//we eliminate from every's word domain the just used word
			//multithread case: we create a list of callable objects, each with it's work, and we run them later concurrently
			for(Word w : wordSet) {
				
				//we get the two dependence indexes (first and second word) of this pair
				//null if no dependence
				BytePair indexes = inversedDependenceMap.get(w);
				
				//the actual add of the callable; inside it uses W and indexes
				runnableList.add(new Callable<Object>() {

					@Override
					public Object call() {
						//if it's null, it's not dependant, so we pass it -1 -1
						if(indexes==null) {
							//we pass -1 -1 so it only removes the used word
							w.updateDomain(assignedValue, (byte)-1, (byte)-1);
						}
						else {
							//if not null, we pass the 2 indexes so it removes the used word and the invalid ones
							w.updateDomain(assignedValue, indexes.b1, indexes.b2);
						}
						return null;
					}
				});
				
			}
			
			//we run these concurrently
			try {
				executor.invokeAll(runnableList);
			} catch (InterruptedException e) {
				System.out.println("Executor threw exception.");
			}
				
			//we get the below result
			boolean resultFromBelow = backtrackingFindWithForwardCheckingAndMultithread(wordList, wordSet, dictionary);
				
			//if true, we succeded, return true
			if(resultFromBelow) {
				return true;
			}
			//if false, we have to go for another word
			else {
					
				//for every word we undo the word repetition search
				for(Word w : wordSet) {
					w.reverseDomain();
				}

				//we restore the value in the domain, as it's not used anymore for the solution
				currWord.setValue(null);
					
			}
					
		}
		
		//we add this word to the not assigned variables set
		wordSet.add(currWord);
			
		return false;
	}

	
}

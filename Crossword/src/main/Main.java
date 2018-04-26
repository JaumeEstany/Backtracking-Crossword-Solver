package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import input.FileInput;
import process.Backtracking;
import process.UsableFormat;
import structs.Word;
import util.Pair;

public class Main {

	private static final String CROSSWORD_A_PATH  = "res/crossword_A.txt";
	private static final String CROSSWORD_CB_PATH = "res/crossword_CB.txt"; 
	
	private static final String DICTIONARY_A_PATH  = "res/diccionari_A.txt";
	private static final String DICTIONARY_CB_PATH = "res/diccionari_CB.txt";
	
	public static long startingTime = System.currentTimeMillis(); 
	
	public static void printElapsedTime() {
		System.out.println( ((double)(System.currentTimeMillis()-startingTime))/1000+"s" );
	}
	
	private static void printWordList(final ArrayList<Word> wordList) {
		
		for(Word w : wordList) {
			System.out.println(""+w.identificator.number+w.identificator.direction+":   "+w.getValue());
		}
		
	}
	
	//==========================================================================================
	
	private static void solveCrossword_normal(String crossWordPath, String dictionaryPath) {
		
		Pair<byte[][], Byte> readInfo = FileInput.readTableFromFile(crossWordPath);
		ArrayList<Word> wordList = UsableFormat.transformFromMatrixToUsable(readInfo.val1, readInfo.val2);
		readInfo=null;
		
		System.out.print("Crossword read:");
	    printElapsedTime();
		
		ArrayList<String> dictionary = FileInput.readWordFile(dictionaryPath);
		
		System.out.print("Words read:");
	    printElapsedTime();
		
	    System.out.print("Backtracking starts:");
	    printElapsedTime();
	    
		Backtracking.backtrackingFind(wordList, 0, dictionary);
		
		System.out.print("Backtracking ends:");
	    printElapsedTime();
	    System.out.println("\n\n");
		
		printWordList(wordList);
		
	}
	
	
	private static void solveCrossword_optimization(final boolean multithread, String crossWordPath, String dictionaryPath) {
		
		//read the file into a matrix and get the number of words
		Pair<byte[][], Byte> readInfo = FileInput.readTableFromFile(crossWordPath);
		//transform it to an arrayList of words
		ArrayList<Word> wordList = UsableFormat.transformFromMatrixToUsable(readInfo.val1, readInfo.val2);
		readInfo=null;
		
	    System.out.print("Crossword read:");
	    printElapsedTime();
		
	    //read the dictionary file and classify words by their words
		Map<Byte, ArrayList<String> > dictionary = FileInput.readAndGroupWordFile(dictionaryPath);
		
	    System.out.print("Words read:");
	    printElapsedTime();
		
		//==================
	    //init the wordSet of not assigned variables with every word in the list
		Set<Word> wordSet = new HashSet<>(wordList.size());
		
		for(Word word : wordList) {
			wordSet.add(word);
			//init the word with the necessary starting info
			word.initBeforeForwardChecking(dictionary.get(word.length));
		}
		
		
		//==================
	    System.out.print("Backtracking starts:");
	    printElapsedTime();
		
	    
	    //run one version or another of the backtracking
	    if(multithread) {
	    	Backtracking.backtrackingFindWithForwardCheckingAndMultithread(wordList, wordSet, dictionary);
	    }
	    else {
	    	Backtracking.backtrackingFindWithForwardChecking(wordList, wordSet, dictionary);
	    }
	    
		
	    System.out.print("Backtracking ends:");
	    printElapsedTime();
	    System.out.println("\n\n");
		
		//System.out.println(result);
	    
	    //print results
		printWordList(wordList);
	}
	
	//==========================================================================================
	
	private static void crosswordA_normal() {
		solveCrossword_normal(CROSSWORD_A_PATH, DICTIONARY_A_PATH);
	}

	private static void crosswordCB_normal() {
		solveCrossword_normal(CROSSWORD_CB_PATH, DICTIONARY_CB_PATH);
	}

	private static void crosswordA_optimization(final boolean multithread) {
		solveCrossword_optimization(multithread, CROSSWORD_A_PATH, DICTIONARY_A_PATH);
	}
	
	private static void crosswordCB_optimization(final boolean multithread) {
		solveCrossword_optimization(multithread, CROSSWORD_CB_PATH, DICTIONARY_CB_PATH);
	}
		
	
	//==========================================================================================
	
	
	public static void main(String[] args) {
		
		boolean crosswordDifficulty = true;					//true goes for A, false goes for B
		String optChosen = "optimization+multithread";
		
		if(args.length>=1 && (args[0].equals("CB"))) {
			crosswordDifficulty=false;
		}
		
		if(args.length>=2) {
			optChosen = args[1];
		}
		
		System.out.println();
		//=======================
		
		switch(optChosen) {
			
			case "normal":
				
				System.out.println("Running in normal mode:");
				
				if(crosswordDifficulty) {
					System.out.println("Crossword A:\n\n");
					crosswordA_normal();
				}
				else {
					System.out.println("Crossword CB:\n\n");
					crosswordCB_normal();
				}
				
				break;
			case "optimization":
				
				System.out.println("Running in optimization mode:");
				
				if(crosswordDifficulty) {
					System.out.println("Crossword A:\n\n");
					crosswordA_optimization(false);
				}
				else {
					System.out.println("Crossword CB:\n\n");
					crosswordCB_optimization(false);
				}
				
				break;
			case "optimization+multithread":
				
				System.out.println("Running in optimization+multithread mode:");
				
				if(crosswordDifficulty) {
					System.out.println("Crossword A:\n\n");
					crosswordA_optimization(true);
				}
				else {
					System.out.println("Crossword CB:\n\n");
					crosswordCB_optimization(true);
				}
				
				break;
			default:
				System.out.println("ERROR: The mode parameter is not a valid value.");
				System.out.println("It is \"" + optChosen + "\". This has to be [\"normal\", \"optimization\", \"optimization+multithread\"]");
				break;
			
		}
		
	}
	
}



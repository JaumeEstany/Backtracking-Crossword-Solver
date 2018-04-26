package process;

import java.util.ArrayList;
import structs.*;
import util.QuadDimensionalByteArray;

public class UsableFormat {
	
	/*
	 * wrapps the matrix access to return -1 (wall) if the given indexes are out of range
	 */
	private static byte accessMatrix(byte[][] matrix, int i, int j){
		
		if(i<0 || j<0 || i>=matrix.length || j>=matrix[0].length){
			return -1;
		}
		
		return (matrix[i][j]);
		
	}
	
	
	/*
	 * this function recieves the crossword matrix and returns an ArrayList of words.
	 */
	public static ArrayList<Word> transformFromMatrixToUsable(final byte[][] matrix, final byte numWords){
		
		ArrayList<Word> wordList = new ArrayList<>(numWords*2);		//we give it an initial capacity of twice the number of words, because every number
																	//can mean 2 written words													
		
		final QuadDimensionalByteArray dependenceMatrix = new QuadDimensionalByteArray(matrix.length, matrix[0].length, 2, 2);
		/*
 			we allocate a quadrimensional array. Basically, it will store 4 bytes for every cell in the original matrix.
 			[ rows ][ columns ][ {0,1}->{horizontal,vertical} ][ {0,1}->{index of the word inside the ArrayList, index of the letter of said word} ]
 			
 			The point is to have a matrix of size [rows][columns] that can hold 2 word indexes and 2 letter indexes, for horizontal and vertical words
 			that might cross in one same cell.
 			
 			We would then mark every cell a word is present on, and after all this, we save collisions in a more usable way.
 			
 			Using an array of 4 dimensions saves up a lot of space, because it only allocates 4 bytes for each cell of the original matrix.
 			If we used classes, this should be a matrix of java references to object (4 bytes per cell) plus the actual allocated memory for our information,
 			so it saves pretty much half of the memory used.
 			
 		*/
		
		//the two calls to do the process
		calculateDirectionsAndLengths(wordList, matrix, dependenceMatrix);
		getDependencesFromMatrix(wordList, dependenceMatrix);

		
		return wordList;
	}
	
	private static void calculateDirectionsAndLengths(ArrayList<Word> wordList, byte[][] matrix, final QuadDimensionalByteArray dependenceMatrix) {
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				
				if (matrix[i][j] > 0) {		//we will only do this when there's a number of word -> not 0 or -1(wall)
					
					//we get if the current word goes horizontally and/or vertically
					final boolean H = ((accessMatrix(matrix, i, j-1) == -1) && (accessMatrix(matrix, i, j+1) != -1));
					final boolean V = ((accessMatrix(matrix, i-1, j) == -1) && (accessMatrix(matrix, i+1, j) != -1));
					
					//we get the number of the word
					final byte ID = matrix[i][j];
					
					
					//		THESE 2 IFs ARE NOT EXCLUSIVE TO EACH OTHER, CAUTION!
					
					if (H) {		//if it goes horizontal
						
						//we will go horizontally from the initial position to the right until we hit a wall or end of map
						//for every cell we go over, we will mark it as this word occupies that space and the letter number that would correspond there 
						byte it=0;
						while(accessMatrix(matrix, i, it+j)!=-1) {
							/*
							we put here the index of the ArrayList that will be occupied by this current word
							the word will be added last, so the current size of the list (before adding) is the position that this word will be at when added
							also, we are summing one here and substracting one when getting the value, this is because we want to differ the first word (index=0)
							from the default value java gives to the array, 0 also.
							then, we always sum 1 before and substract 1 after, to be able to detect when one of the two values wasn't used
							*/
							dependenceMatrix.get()[dependenceMatrix.calcIndex(i, it+j, 0, 0)] = (byte)(wordList.size()+1);
							//we put the letter number that corresponds here
							dependenceMatrix.get()[dependenceMatrix.calcIndex(i, it+j, 0, 1)] = it;
							
							it++;		//increment
						}
						
						//we add the list 
						wordList.add(new Word(ID, 'H', it));
					}
						
					if (V) {		//if it goes vertical
									//for explanation see code above that works exactly the same, except this one runs vertically (down)
						byte it=0;
						while(accessMatrix(matrix, it+i, j)!=-1) {
							
							dependenceMatrix.get()[dependenceMatrix.calcIndex(it+i, j, 1, 0)] = (byte)(wordList.size()+1);
							dependenceMatrix.get()[dependenceMatrix.calcIndex(it+i, j, 1, 1)] =it;
							it++;
						}
						
						wordList.add(new Word(ID, 'V', it));
					}
						
						
				}
			}
		}
		
	}
	
	private static void getDependencesFromMatrix(ArrayList<Word> wordList, final QuadDimensionalByteArray dependenceMatrix){
		
		
		for (int i = 0; i < dependenceMatrix.getSize(0); i++) {
			for (int j = 0; j < dependenceMatrix.getSize(1); j++) {
				
				//we get the indexes directly (without the substract)
				final int indexH = dependenceMatrix.get()[dependenceMatrix.calcIndex(i, j, 0, 0)];
				final int indexV = dependenceMatrix.get()[dependenceMatrix.calcIndex(i, j, 1, 0)];
				
				if(indexH*indexV!=0) {		//if none is 0
					
					//we get the two indexes but decrease them once, because we summed 1 before (explained in function above)
					//with the indexes, we get the word reference
					final Word wH = wordList.get(indexH-1);
					final Word wV = wordList.get(indexV-1);
					
					//we get the letters indexes that correspond
					final byte posH = dependenceMatrix.get()[dependenceMatrix.calcIndex(i, j, 0, 1)];
					final byte posV = dependenceMatrix.get()[dependenceMatrix.calcIndex(i, j, 1, 1)];
					
					//we put the values in both dictionaries
					//indexed by the own position, gives us a position object
					//that contains the target word and the position of the letter
					wH.dependences.put(posH, new Position(posV, wV));
					wV.dependences.put(posV, new Position(posH, wH));
					
				}
				
			}
		}
		
	}
	
}

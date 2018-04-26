package input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import util.Pair;

public class FileInput {
	
	private static final int HASHTABLE_INIT_CAPACITY = 20;
	private static final int TABLE_ARRAYLIST_INIT_CAPACITY = 20;
	
	/*
	 * called when a FileNotFoundException is found
	 */
	private static void toDoWhenFileNotFound(){
		System.out.println("FILE NOT FOUND");
	}
	
	/*
	 * called when a IOException is found
	 */
	private static void toDoWhenIOException(){
		System.out.println("IOEXCEPTION SOMEHOW");
	}
	
	/*
	 * called to transform the Strings of each cell gotten from the file
	 */
	private static byte fromStringToByte(String str) {
		
		byte ret = -1;
		
		try {
			ret = (byte)Integer.parseInt(str);
			
		} catch (NumberFormatException e) {}
		
		return ret;
	}
	
	
	/*
	 * Reads the specified path and returns the list of words
	 */
	public static ArrayList<String> readWordFile(String path){
		
		ArrayList<String> ret = new ArrayList<>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			for(String line; (line = br.readLine()) != null; ) {
				ret.add(line);
			}
		}
		catch(FileNotFoundException e){
			toDoWhenFileNotFound();
		}
		catch(IOException e){
			toDoWhenIOException();
		}
		
		return ret;
	}

	/*
	 * Reads the specified path and groups and returns the words grouped by word length. It returns a Map< Integer, ArrayList<String> > 
	 */
	public static Map<Byte, ArrayList<String> > readAndGroupWordFile(String path){
		
		Map<Byte, ArrayList<String> > ret = new HashMap<>(HASHTABLE_INIT_CAPACITY);
		
		//init the reader
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {

			//for every line in the file
			for(String line; (line = br.readLine()) != null; ) {
				//get the length
				final byte LEN = (byte)line.length();
				
				//append it to the list of Strings of that length
				//if the list doesnt exist, create it
				
				if(ret.get(LEN) == null){
					ret.put(LEN, new ArrayList<String>());
				}
				
				ret.get(LEN).add(line);
			}
			
		}
		catch(FileNotFoundException e){
			toDoWhenFileNotFound();
		}
		catch(IOException e){
			toDoWhenIOException();
		}
		
		return ret;
	}
	
	
	public static Pair<byte[][], Byte> readTableFromFile(String path){
		
		//we init our auxList variable which will be used to append all the rows until we've got all of them
		ArrayList < byte[] > auxList = new ArrayList<>(TABLE_ARRAYLIST_INIT_CAPACITY);
		
		//this value will be updated with the max value found (number of words)
		byte max=0;
		
		//the try with parameters to safely init and ensure closing of the buffer
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			
			//for every line in the file
			for(String line; (line = br.readLine()) != null; ) {
				final String[] auxStringArray = line.split("\\s+");							//we divide it by any kind of whitespace (spaces or tabs, in this case)
				final byte[] actualAddition = new byte[auxStringArray.length];				//array that will be added to auxList in this iteration
				
				for(int i=0;i<auxStringArray.length;i++){									//for every element we splitted
					final byte val = (byte)fromStringToByte(auxStringArray[i]);				//transform the value to byte
					max = (val>max)?(val):(max);											//update max
					actualAddition[i] = val;												//move the byte value to its position
				}
				
				auxList.add(actualAddition);		//we add to the auxList the split by whitespace
													//basically every character of the line separately
			}
		}
			
		
		catch(FileNotFoundException e){
			toDoWhenFileNotFound();
		}
		catch(IOException e){
			toDoWhenIOException();
		};
		
		byte[][] ret = new byte[auxList.size()][];
		
		for(int i=0;i<ret.length;i++) {
			ret[i]=auxList.get(i);
		}
		
		//we return a pair constructed with the matrix and the number of words we found
		return (new Pair<byte[][], Byte>(ret, max));
	}
	
	
}

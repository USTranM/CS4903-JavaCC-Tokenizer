/******************************
 * Name: 		Martin Tran
 * Username:	uatext
 * Problem Set:	PS3
 * Due Date:	7-30-19
 ******************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UAQuery {
	
	protected static HashMap<String, Integer> startMap;
	protected static HashMap<String, Integer> termIdMap;
	
	public static void main (String[] args) throws FileNotFoundException, IOException {
		UAQuery query = new UAQuery();
		query.buildGlobalHashMap("C:/Users/Owner/Documents/raf/dict.txt");
		
		ArrayList<String> list = new ArrayList<String>();
		
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) 
				break;
			args[i] = args[i].replaceAll("\\p{Punct}", " ").replaceAll("  ", " ").trim().toLowerCase();
			list.add(args[i]);
			
		}
		Object[] arr = list.toArray();
		String[] topTen = runQuery(arr);
		for (int i = 0; i < topTen.length; i++) {
			if (topTen[i] == null) {
				System.out.println("End of query. If no results are shown, then there are no documents related to query search.");
				break;
			}
			System.out.println(topTen[i]);
		}
	}
	
	/*
	 * Recreates a HashMap<String, Integer> for easy access to 
	 * both the start values and the termIds.
	 * @param outputFile This output file is basically the dictionary table.
	 */
	
	
	public void buildGlobalHashMap(String outputFile) throws IOException {
		startMap = new HashMap<String, Integer>();
		termIdMap = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(outputFile));
		String input = reader.readLine();
		int count = 0;
		while(input != null) {
			
			if (input.substring(0, 8).equals("000blank")) {
				count++;
				input = reader.readLine();
			} else {
				
				for (int i = 0; i < 8; i++) {
					if (Character.isDigit(input.charAt(i))) {
						continue;
					} else {
						if (startMap.get(input.substring(i, 8)) == null) {
							startMap.put(input.substring(i, 8), count);
							termIdMap.put(input.substring(i, 8), Integer.parseInt(input.substring(8, 13)));
							count++;
							break;
						} 
					}
				}
				
				input = reader.readLine();
			}
		}

		reader.close();
	}
	
	public static class ScoreFormatter implements Comparator<String> {

		/**
		* Comparing the return value from calculateSimilarity for each value in PriorityQueue. 
		* @return Integer to determine if str1 or str2 ranks higher for similarity
		* @param word1 First similarity score to compare
		* @param word2 Second similarity score to compare
		* Time Complexity: O(1)
		* Space Complexity: O(Length of each String)
		*/
		public int compare(String word1, String word2) {
			String[] split1 = word1.split(",");
			String[] split2 = word2.split(",");
			if (Float.isNaN(Float.parseFloat(split1[2])) || Float.isNaN(Float.parseFloat(split2[2]))) {
				return Integer.MIN_VALUE;
			} else {
				//return str.substring(str.indexOf(": ") + 2, str.indexOf(", ")).compareTo(str1.substring(str1.indexOf(": ") + 2, str1.indexOf(", ")));
				int temp = Float.compare(Float.parseFloat(split2[2]), Float.parseFloat(split1[2]));
				return Float.compare(Float.parseFloat(split2[2]), Float.parseFloat(split1[2]));					
			}
		}
	}
	
	
	/**
	 * Queries the user's input.
	 * V = All unique words in lexicon.
	 * {D} = subset of words from all documents.
	 * @param input User's input
	 * @return String[] of the top ten documents that is most similar to the user's query
	 * @throws IOException Handles any IO errors while accessing the dict.raf file
	 * Time Complexity: O({D}V)
	 * Space Complexity: O({D}V)
	 */
	public static String[] runQuery(Object[] input) throws IOException {
		
		RandomAccessFile dictRaf = new RandomAccessFile("C:/Users/Owner/Documents/raf/dict.raf" , "r");
		RandomAccessFile mapRaf = new RandomAccessFile("C:/Users/Owner/Documents/raf/mapRaf.raf" , "r");
		RandomAccessFile postRaf = new RandomAccessFile("C:/Users/Owner/Documents/raf/post.raf" , "r");
		HashSet<Integer>  allDocuments = new HashSet<Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader("C:/Users/Owner/Documents/raf/fileCount.txt"));
		int numFiles = Integer.parseInt(br.readLine());
		br.close();
		
		for (int currentWord = 0; currentWord < input.length; currentWord++) {
			if (startMap.get((String)input[currentWord]) != null) {
				
				long start = (startMap.get((String)input[currentWord]));
				dictRaf.seek(start * 29);
			
				String record = dictRaf.readUTF();
				int numDocs = Integer.parseInt(record.substring(13, 19));
				start = Long.parseLong(record.substring(19));
				for (int currentDoc = 0; currentDoc < numDocs; currentDoc++) {
					postRaf.seek((currentDoc + start) * (27 + 2));
					record = postRaf.readUTF();
					if (record.length() == 0) {
						continue;
					} else {
						try {
							if (Integer.parseInt(record.substring(0, 10)) > numFiles) {
								continue;
							}
							allDocuments.add(Integer.parseInt(record.substring(0, 10)));
						} catch (NumberFormatException e) {
							continue;
						}
					}
						
				}
				
				
			} else {
				input[currentWord] = null;
			}
		}
		ArrayList<String> similarityScore = new ArrayList<String>();
		Iterator<Integer> iter = allDocuments.iterator();
		
		float[] queryVector = new float[termIdMap.size()];
		HashMap<String, Integer> queryMap = new HashMap<String, Integer>();
		for (int i = 0; i < input.length; i++) {
			if (termIdMap.get((String)input[i]) != null) {
				queryVector[termIdMap.get((String)input[i])] += 1;
				if (queryMap.get((String)input[i]) == null)
					queryMap.put((String)input[i], 1);
				else 
					queryMap.put((String)input[i], queryMap.get((String)input[i]) + 1);

			}
		}

		for (int i = 0; i < input.length; i++) {
			if (termIdMap.get((String)input[i]) != null) {
				int x = termIdMap.get((String)input[i]);
				queryVector[termIdMap.get((String)input[i])] /= input.length;
				queryVector[termIdMap.get((String)input[i])] *= idf(allDocuments.size(), queryMap.get((String)input[i]));
		
			}
		}
		
		while (iter.hasNext()) {
			similarityScore.add(calculateTermDocumentVector(iter.next(), input, queryVector, numFiles));
		}
		
		String[] topTenDocuments = new String[10];
		similarityScore.sort(new ScoreFormatter());
		for (int topTen = 0; topTen < topTenDocuments.length; topTen++) {
			if (!similarityScore.isEmpty())
				topTenDocuments[topTen] = similarityScore.remove(0);
		}

		dictRaf.close();
		mapRaf.close();
		postRaf.close();
		
		return topTenDocuments;
	}
	
	
	/**
	 * Helper method to calculate Inverse Document Frequency
	 * @param totalDocs Total number of documents in directory
	 * @param count Total occurrences of a specified word
	 * @return IDF value
	 * Time Complexity: O(1)
	 * Space Complexity: O(1)
	 */
	public static float idf(int totalDocs, int count) {
		return (float)Math.log((float)totalDocs / count);
	}
	
	
	/**
	 * Revised calculateSimilarity method to be more fitted for the querying phase.
	 * @param inputVector User's input vector
	 * @param documentVector Current document vector
	 * @param file The file number to be sent back to the user
	 * @return Similarity score and document vector index
	 * Time Complexity: O(V)
	 * Space Complexity: O(dV)
	 */
	public static String calculateSimilarity(float[] documentVector, float[] inputVector, int file) {
		float numerator = 0;
		float word1Sum = 0, word2Sum = 0;
		for (int i = 0; i < documentVector.length; i++) {
			numerator = numerator + (inputVector[i] * documentVector[i]);
			word1Sum += (float)Math.pow((float)inputVector[i], 2);
			word2Sum += (float)Math.pow(documentVector[i], 2);
		}
		
		float temp = (float)(numerator/(Math.sqrt(word1Sum) * Math.sqrt(word2Sum)));
		if (Float.isNaN(temp)) {
			return String.format("%d,%s,%f", file, "C:/Users/Owner/processedTokens/" + file + "p.html.out", -1.0);
		} else {
			return String.format("%d,%s,%.10f", file, "C:/Users/Owner/processedTokens/" + file + "p.html.out", temp);
		}

	}
	
	
	/**
	 * Builds the term-document matrix in order to easily calculate similarity between the user's input vector and every document
	 * @param file File to be processed
	 * @param stringInputArr Input array that user entered
	 * @param queryVector Input vector calculated from user input
	 * @param numDocs The number of documents in the training corpus
	 * @return The term-document matrix
	 * @throws IOException Handles any IO errors that may occur when accessing the .raf and trainingCorpus files
	 * Time Complexity: O(d)
	 * Space Complexity: O(V)
	 * @throws FileNotFoundException Exception thrown in case there is any issues finding a document
	 * @throws IOException Exception thrown if there are issues reading a file
	 */
	public static String calculateTermDocumentVector(int file, Object[] stringInputArr, float[] queryVector, int numDocs) throws IOException, FileNotFoundException {
		
		RandomAccessFile dictRaf = new RandomAccessFile("C:/Users/Owner/Documents/raf/dict.raf" , "r");
		RandomAccessFile postRaf = new RandomAccessFile("C:/Users/Owner/Documents/raf/post.raf" , "r");
		RandomAccessFile mapRaf  = new RandomAccessFile("C:/Users/Owner/Documents/raf/mapRaf.raf" , "r");

		BufferedReader br = null;
		//mapRaf.seek(file * 22);
		//String filePath = mapRaf.readUTF();
		float[] documentVector = new float[termIdMap.size()];
		//if (filePath.length() > 0) {
			//int fileNum = Integer.parseInt(filePath.substring(0, 10));
			br = new BufferedReader(new FileReader("C:/Users/Owner/Documents/processedTokens/"  + file + "p.html.out"));
			
			String input = br.readLine();
			int numTerms = 0;
			
			while (input != null) {
				
				if (termIdMap.get(input) != null && termIdMap.get(input) < termIdMap.size()) {
					documentVector[termIdMap.get(input)]+= 1;
					numTerms++;
				} else {
					input = br.readLine();
					continue;
				}
				input = br.readLine();

			}
			
			
			for (int i = 0; i < documentVector.length; i++) {
					if (documentVector[i] != 0) {
						documentVector[i] /= numTerms;
						documentVector[i] *= idf(numDocs, i);
					}
				}
			//}	
	
			br.close();
		
			

			mapRaf.close();
			postRaf.close();
			dictRaf.close();
			
			return calculateSimilarity(documentVector, queryVector, file);//, filePath);
		
	}
	

	
}

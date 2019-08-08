import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class UAMatrix {
	
	protected HashMap <String, Integer> hashMap, wordMap;
	
	public UAMatrix(HashMap<String, Integer> hashMap) {
		this.hashMap = hashMap;
	}

	public UAMatrix() {
		hashMap = new HashMap<String, Integer>();
		wordMap = new HashMap<String, Integer>();
	}
	
	
	public static void main(String[] args) throws IOException, FileNotFoundException {
		
		UAMatrix matrix = new UAMatrix();
		matrix.hashMap = matrix.buildHashMap("word2.txt");
		
		float[][] termContextMatrix = matrix.buildTermContextMatrix("word2.txt", matrix.hashMap.size(), matrix.hashMap);
				
				Integer data     = matrix.hashMap.get("data");
                Integer computer = matrix.hashMap.get("computer");
                Integer dog      = matrix.hashMap.get("dog");
                Integer pencil   = matrix.hashMap.get("pencil");
                Integer hot      = matrix.hashMap.get("hot");
                String data_computer, data_dog, data_pencil, hot_dog;
                

                if (data != null && computer != null) {
                        System.out.println("Data and Computer: " + data + ", " + computer + termContextMatrix[data][computer] + ", " + termContextMatrix[computer][data]);
                        data_computer = matrix.calculateSimilarity(termContextMatrix, matrix.hashMap.get("data"), matrix.hashMap.get("computer"));
                        System.out.println(data_computer);
                }

                if (data != null && dog != null) {
                        System.out.println("Data and Dog: " + data + ", " + dog + termContextMatrix[data][dog] + ", " + termContextMatrix[dog][data]);
                        data_dog = matrix.calculateSimilarity(termContextMatrix, matrix.hashMap.get("data"), matrix.hashMap.get("dog"));
                        System.out.println(data_dog);
                }

                if (data != null && pencil != null) {
                        System.out.println("Data and Pencil: " + data + ", " + pencil + termContextMatrix[data][pencil] + ", " + termContextMatrix[pencil][data]);
                        data_pencil = matrix.calculateSimilarity(termContextMatrix, matrix.hashMap.get("data"), matrix.hashMap.get("pencil"));
                        System.out.println(data_pencil);
                }

                if (hot != null && dog != null) {
                        System.out.println("Hot and Dog: " + hot + ", " + dog + termContextMatrix[hot][dog] + ", " + termContextMatrix[dog][hot]);
                        hot_dog = matrix.calculateSimilarity(termContextMatrix, matrix.hashMap.get( "hot"), matrix.hashMap.get( "dog"));
                        System.out.println(hot_dog);
                 }


                Set<String> set = matrix.hashMap.keySet();
                Object[] objArr = set.toArray();

	        PollWord[] contextComputer = matrix.getContext(termContextMatrix, matrix.hashMap.get("computer"), 10, objArr);
	        PollWord[] contextData = matrix.getContext(termContextMatrix, matrix.hashMap.get("data"), 10, objArr);
	        PollWord[] contextPencil = matrix.getContext(termContextMatrix, matrix.hashMap.get("pencil"), 10, objArr);
	        PollWord[] contextDog = matrix.getContext(termContextMatrix, matrix.hashMap.get("dog"), 10, objArr);
        
		System.out.println("Context: Computer");
		for (PollWord str: contextComputer) {
			System.out.println(str.getWord() + ", " + str.getSimScore());
		}

		System.out.println("Context: Data");
       		for (PollWord str: contextData) {
        	        System.out.println(str.getWord() + ", " + str.getSimScore());
	        }

		System.out.println("Context: Pencil");
      		for (PollWord str: contextPencil) {
        	        System.out.println(str.getWord() + ", " + str.getSimScore());
	        }

        	System.out.println("Context: Dog");
	        for(PollWord str: contextDog) {
        	        System.out.println(str.getWord() + ", " + str.getSimScore());
	        }
	
	
	}
	
	
	
			public static class CosineComparator implements Comparator<PollWord> {

				/**
				* Comparing the return value from calculateSimilarity for each value in PriorityQueue. 
				* @return Integer to determine if str1 or str2 ranks higher for similarity
				* @param p1 First similarity score to compare
				* @param p2 Second similarity score to compare
				* Time Complexity: O(1)
				* Space Complexity: O(Length of each String)
				*/
				public int compare(PollWord p1, PollWord p2) {
					float simScore1 = Float.parseFloat(p1.getSimScore());
					float simScore2 = Float.parseFloat(p2.getSimScore());
					if (Double.isNaN((double)simScore1) || Double.isNaN((double)simScore2)) {
						return Integer.MIN_VALUE;
					} else {
						//return str.substring(str.indexOf(": ") + 2, str.indexOf(", ")).compareTo(str1.substring(str1.indexOf(": ") + 2, str1.indexOf(", ")));
						return Float.compare(simScore1, simScore2);					
					}
				}
			}


			/**
			 * Custom class to wrap the "word" and similarity score (simScore) fields together.
			 * Used in the calculateSimilarity() function.
			 */
			
			public static class PollWord {
				private String word;
				private String simScore;
				
				public PollWord(String word, String simScore) {
					this.word = word;
					this.simScore = simScore;
				}
				
				public String getWord() {
					return word;
				}
				
				public String getSimScore() {
					return simScore;
				}

				public void setSimScore(String simScore) {
					this.simScore = simScore;
				}			
			}

		  	/**
			* Function to build the Term-Context Matrix by accepting the
			* word2.txt file.
			* v (lowercase) = all words in a document
			* V (capital) = all unique words in the lexicon
			* @return The term-context matrix after calculating PPMI and its adjustments
			* @param trainingCorpus word2.txt file path
			* @param dim The dimensions of the matrix, which is |V| by |V| or the size of the lexicon.
			* @param hashMap All of the unique words in the lexicon.
			* Time Complexity: O(v * V^3 * V^2)
			* Space Complexity: O(V^6)
			* @throws IOException Handles any IO issues that may occur.
			* @throws FileNotFoundException Throws when file is not found in directory.
		  	*/
		  
			public float[][] buildTermContextMatrix(String trainingCorpus, int dim, HashMap<String, Integer> hashMap) throws IOException, FileNotFoundException {
								
						
				String[] temp;
				int logicalSize;
				int count;
				int keyWord, windowWord;
				BufferedReader corpusReader;

				float[][] termContextMatrix = new float[dim][dim];
					
				corpusReader = new BufferedReader(new FileReader(trainingCorpus));
				int stream = (int)Files.lines(Paths.get(trainingCorpus)).count();
				int loop = 0;
				
			while (loop < stream/100) {
				String[] strArr = new String[stream/100];

				String input;
				for (int i = 0; i < strArr.length; i++) {
					input = corpusReader.readLine();
					if (input != null && hashMap.get(input) != null) {
						strArr[i] = input;
//						System.out.println(input);
					}
				}
					
				int counter = 0;
				String key = null;
				int key2 = -1;				
				while (counter <= strArr.length) {
				
	                    temp = new String[11]; //Window size = 5
        	            logicalSize = 0;
//				System.out.println("Counter: " + counter);
						if (counter - 5 < 0) {
							for (int i = counter; i > 0; i--) {				
								if (counter < strArr.length && strArr[counter] != null) {
 				                                    temp[logicalSize] = strArr[counter - i];
        	                			            logicalSize++;
                	                                                

				                                }
							}
							
							if (counter + 1 < strArr.length && strArr[counter + 1] != null) { 
								temp[logicalSize] = strArr[counter + 1];
								logicalSize++;
								key = strArr[++counter];
								key2 = counter;
							}
							
							for (int i = 0; i < 5; i++) {
								if (counter < strArr.length && strArr[counter] != null) { 
									temp[logicalSize] = strArr[counter + i];
									logicalSize++;
								}
							}



												     
					} else if ( strArr.length - counter < 11 ) {
						temp = new String[strArr.length - counter];
							  
						for (int i = 5; i > 0; i--) {
							if (counter < strArr.length && logicalSize < temp.length  && strArr[counter] != null) {
                               	                		temp[logicalSize] = strArr[counter - i];
                               	                		logicalSize++;
										
							}
        	        	                }

						if (counter + 1 < strArr.length && logicalSize < temp.length && strArr[counter + 1] != null) { 
							temp[logicalSize] = strArr[counter + 1];
                        	   		        logicalSize++;
							key = strArr[++counter];
							key2 = counter;
	        
									
						}

        		               	        for (int i = 0; i < strArr.length - counter; i++) {
							if (counter + i < strArr.length && logicalSize < temp.length && strArr[counter] != null) {
								temp[logicalSize] = strArr[counter + i];
        	        		                        logicalSize++;
                	        	                                        
                      					}
						}


					} else {
						for (int i = 5; i > 0; i--) {
							if (strArr[counter] != null) {
								temp[logicalSize] = strArr[counter - i];
								logicalSize++;
										
							}
						}


						if (counter + 1 < strArr.length && strArr[counter + 1] != null) { 
								temp[logicalSize] = strArr[counter + 1];
			                       		        logicalSize++;
								key = strArr[++counter];
                        	                                key2 = counter;
								
						}
 							
						for (int i = 0; i < 5; i++ ) {
							temp[logicalSize] = strArr[counter + i];
							logicalSize++;
								
						}
							
					}
/*					
					System.out.println("\n\n\n");
					for ( int i = 0; i < temp.length; i++) {
						System.out.println(temp[i]);
					}
*/					
					if (key2 == -1 || key == null || hashMap.get(key) == null) {
					  	counter++;
						continue;
					} else {
						keyWord = hashMap.get(key);
					}
				
					count = 0;
					while (count < logicalSize) {
		
						if (key2 == count) { // Don't want to repeat over keyword
							count++;
							continue;
						}

						if (temp[count] == null) { 						
//							System.out.println("temp[count] is null");
							count++;
							continue;
						} else if (hashMap.get(temp[count]) == null) {
//							System.out.println("hashMap is null");
                                                        count++;
                                                        continue;
						} else { 
                                                        windowWord = hashMap.get(temp[count]);
						}
											
//						System.out.println("windowWord: " + windowWord);
//						System.out.println("keyWord: " + keyWord);

                                                if (windowWord > 0 && keyWord > 0) {
							termContextMatrix[keyWord][windowWord] += 1; 
//							System.out.println("KeyWord: " +  strArr[keyWord] + ", WindowWord: " + strArr[windowWord] + ", " + termContextMatrix[keyWord][windowWord]);
//							System.out.println(counter);
						}
						count++;
						
						
					}
				counter++;
					
				}
				loop++;
			}	
				corpusReader.close();
				
				
				float matrixTotal = totalSum(termContextMatrix);
				float[] rowsTotals = new float[dim];
				float[] columnTotals = new float[dim];
				for (int i = 0; i < dim; i++) {
					rowsTotals[i] = rowColSum(termContextMatrix[i]);
					columnTotals[i] = rowColSum(getColumnArray(termContextMatrix, i));
				}
				
                                
				for (int row = 0; row < dim; row++) {
                     for (int col = 0; col < dim; col++) {						
                            termContextMatrix[row][col] = ppmi(termContextMatrix, row, col, matrixTotal, rowsTotals[row], columnTotals[col]);
                     }
					
				}
				
				return termContextMatrix;
			}		

		/**
		* Function to be used for calculating the PPMI (Positive
		* Pointwise Mutual Information) in order to translate raw
 		* frequencies into normalized data.
 		* V = all unique terms in lexicon.
		* @return PPMI value for that entry in the term-context matrix.
		* @param matrix Term-context matrix.
		* @param indexWord1 Index position of the row.
		* @param indexWord2 Index position of the column.
		* @param matrixTotal The total of the entire termContextMatrix in raw frequencies.
		* @param rowTotal The total of the current row.
		* @param colTotal The total of the current column.
		* Time Complexity: O(1) 
		* Space Complexity: O(V^2)
		*/		

		public float ppmi(float[][] matrix, int indexWord1, int indexWord2, float matrixTotal, float rowTotal, float colTotal) {
			
			
			float numerator = matrix[indexWord1][indexWord2] / matrixTotal;
			float denominator = (rowTotal/matrixTotal) * (colTotal/matrixTotal);
			
			if (numerator == 0 || denominator == 0) {
				return 0;
			}

			float ppmi = Math.max((float)Math.log(numerator/denominator)/(float)Math.log(2), 0);
			
			//float ppmi = (float)Math.max(Math.log(Math.abs((float)(matrix[indexWord1][indexWord2] / matrixTotal) / ((rowTotal/matrixTotal) * (colTotal/matrixTotal))))/Math.log(2),0); 
			if(ppmi > 0.75) {
				return (float)ppmiAdjustment(colTotal, (float)0.75, matrixTotal);
			} else {
				return ppmi;
			}
		}

		/**
		* PPMI Adjustment for the rare cases of words that causes
		* extremely high PPMI values
		* @return The adjusted PPMI value.
		* @param word The sum of that row's values.
		* @param power The power to raise the row's values by.
		* @param sum Total sum of matrix.
		* Time Complexity: O(1)
		* Space Complexity: O(1)
		*/

		public float ppmiAdjustment(float word, float power, float sum) {
			return ((float)(Math.pow(word, power))/((float)Math.pow(sum, power)));
		}

		/**
		* Used to determine if two words are similar in context by
		* frequencies.
		* @return The amount of similarity between two words.		
		* @param matrix The term-context matrix.
		* @param indexWord1 Index position of the first word.
		* @param indexWord2 Index position of the second word.
		* Time Complexity: O(V)
		* Space Complexity: O(V)
		*/
		
		public String calculateSimilarity(float[][] matrix, int indexWord1, int indexWord2) {
			float numerator = 0;
			float word1Sum = 0, word2Sum = 0;
			for (int i = 0; i < matrix.length; i++) {
				numerator = numerator + (float)(matrix[indexWord1][i] * matrix[indexWord2][i]);
				word1Sum += (float)Math.pow(matrix[indexWord1][i], 2);
				word2Sum += (float)Math.pow(matrix[indexWord2][i], 2);
			}


			return String.format("%.20f", (float)(numerator/(Math.sqrt(word1Sum) * Math.sqrt(word2Sum))));
		}

		
		/**
		* Gets the k number of relevant words to a specified word
		* @return The k number of relevant words
		* @param matrix The term-context matrix
		* @param indexWord The specified word to get relevant words from
		* @param numberToExtract The number of words to extract relevant to the specified word
		* @param objArr All unique words in lexicon.
		* Time Complexity: O(V)
		* Space Complexity: O(V^4)
		*/		


		public PollWord[] getContext(float[][] matrix, int indexWord, int numberToExtract, Object[] objArr) {
			
			if (indexWord == -1)
				return null;
			PollWord[] contextedArray = new PollWord[numberToExtract];
			PollWord[] temp = new PollWord[matrix.length];
			TreeSet<PollWord> treeSet = new TreeSet<PollWord>(new CosineComparator());
			
			for (int i = 0; i < objArr.length; i++) {
			  	if (i == indexWord)
			  		continue;
				temp[i] = new PollWord((String)objArr[i], calculateSimilarity(matrix, indexWord, i));
				treeSet.add(temp[i]);
			}

			int x = 0;
			int counter = 0;
			while (counter < numberToExtract && x < temp.length) {
				PollWord poll = treeSet.pollLast();
				if (Float.isNaN(Float.parseFloat(poll.getSimScore()))) {
					x++;
					continue;
					
				}
				contextedArray[counter] = poll;
				x++;
				counter++;
			}
			
			return contextedArray;
			
		}

		/** 
		* Helper method to calculate the sum of a row array
		* @return The sum of the row array
		* @param arr The array to be summed
		* Time Complexity: O(V)
		* Space Complexity: O(V)
		*/

		public float rowColSum(float[] arr) {
			float sum = 0;
			
			for (int i = 0; i < arr.length; i++) {
				sum += arr[i];
			}
			
			return sum;
		}

		/**
		* Helper method to find total sum of a 2D array
		* @return Sum of the entire array
		* @param arr The 2D array to be summed
		* Time Complexity: O(V^2)
		* Space Complexity: O(V^2)
		*/
		
		public float totalSum(float[][] arr) {
			float sum = 0;
			
			for (int row = 0; row < arr.length; row++) {
				for (int col = 0; col < arr[0].length; col++) {
					sum += arr[row][col];
				}
			}
			
			return sum;
		}
		
		/**
		* Helper method to get an array of the specified column
		* @return Array of specified column
		* @param matrix The term-context matrix
		* @param col The specified column
		* Time Complexity: O(v)
		* Space Complexity: O(v^2)
		*/
					
		public float[] getColumnArray(float[][] matrix, int col) {
			float[] temp = new float[matrix[col].length];
			
			for (int i = 0; i < temp.length; i++) {
				temp[i] = matrix[i][col];
			}
			
			return temp;
		}	
		
		public HashMap<String, Integer> buildHashMap(String trainingCorpus) throws FileNotFoundException, IOException {
			HashMap<String, Integer> temp = new HashMap<String, Integer>();
			int logicalSize = 0;
			
			BufferedReader br = new BufferedReader(new FileReader("word2.txt"));
			String input = br.readLine();
					
			while (input != null) {
				if (wordMap.get(input) == null) {
					wordMap.put(input, 1);
				} else {
					Integer x = wordMap.get(input);
					wordMap.put(input, x + 1);
				}
				input = br.readLine();
			}

			Set<String> set = wordMap.keySet();
			Object[] objArr = set.toArray();

			for (int i = 0; i < objArr.length && logicalSize < 50000; i++) {
				String word = (String)objArr[i];
				if (wordMap.get(word) < 30 || wordMap.get(word) > 400000 || word.length() < 3 || word.length() > 8) {
					continue;
				}
				
				temp.put((String)objArr[i], logicalSize++);
			}

			br.close();
//			System.out.println("Logical Size: " + logicalSize);
			return temp;
		}

	}

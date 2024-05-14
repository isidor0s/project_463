package Search;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Necessary Class for Searching
 * ---------------------------------------------------------------
 * Searching : Using a query to find a specific item in a collection.
 * We keep the results that contain at least one of the query words.
 * ---------------------------------------------------------------
 * Specify : category [ diagnosis , test , treatment ]
 * ---------------------------------------------------------------
 */
public class Search {
    String VocabularyFileName;          // correspond to the vocabulary file
    String PostingFileName;             // correspond to the posting file
    String[] Query;                     // correspond to the query written by the user
    int numResults;

    /* ---------------------------- Getters ------------------------ */
    public String getVocabularyFileName() { return VocabularyFileName; }
    public String getPostingFileName() { return PostingFileName; }
    public String[] getQuery() { return Query; }
    public int getNumResults() { return numResults; }
    /* ---------------------------- Setters ------------------------ */
    public void setVocabularyFileName(String vocabularyFileName) { VocabularyFileName = vocabularyFileName; }
    public void setPostingFileName(String postingFileName) { PostingFileName = postingFileName; }
    public void setQuery(String[] query) { Query = query; }
    public void setNumResults(int numResults) { this.numResults = numResults; }
    /* -------------------------- Constructors --------------------- */
    /**
     * Constructor
     * @param vocabFilePath : the path of the vocabulary file
     * @param postingFilePath : the path of the posting file
     */
    public Search(String vocabFilePath, String postingFilePath) {
        this.VocabularyFileName = vocabFilePath;
        this.PostingFileName = postingFilePath;
        this.numResults=0;
    }
    /*--------------------------------------------------------------------*/

    // test functions
    public static List<String> performSearch(String query, String type) {
        // Implement your search logic here
        // Return a list of results
        return List.of("Result 1", "Result 2", "Result 3");

    }
    public static String performS(String query) {
        // Perform the search with the given query
        return "Results for query: \t" + query;
    }

    /**
     * Function that searches for a specific word in the vocabulary file
     * @param query : the words to search for
     * @throws IOException : if an I/O error occurs
     */
    public void search(String query) throws IOException {
        RandomAccessFile vocabFile = new RandomAccessFile(getVocabularyFileName(), "r");
        RandomAccessFile postingFile = new RandomAccessFile(getPostingFileName(), "r");

        String[] queryWords = query.split(" "); // Split the query into words

        for( String queryWord : queryWords) {
            String line;
            while ((line = vocabFile.readLine()) != null) {
                String[] splitLine = line.split(" ");
                String word = splitLine[0];         // get the word from the VOCAB file line
                if (word.equals(queryWord)) {       // FOUND the query word
                    long postingListPointer = Long.parseLong(splitLine[2]);
                    System.out.println("following pointer.. "+postingListPointer);
                    postingFile.seek(postingListPointer);
                    String postingList = postingFile.readLine();
                    String firstString = postingList.split(" ")[0];
                    System.out.println(firstString);
                    System.out.println("The word '" + query + "' appears in documents: " + postingList);
                    break;
                }
            }
        }
        vocabFile.close();
        postingFile.close();
    }

    /* -------------------------- helper functions -------------------------- */

    /**
     * Function that returns the total document frequency of a specific word
     * @param query : the words to search for
     * @return the total document frequency of the word (sum)
     * @throws IOException : if an I/O error occurs
     */
    public int[] getTotalDf(String query) throws IOException {
        RandomAccessFile vocabFile = new RandomAccessFile(getVocabularyFileName(), "r");

        String[] queryWords = query.split(" "); // Split the query into words
        int[] totalDfs = new int[queryWords.length]; // Initialize the array
        int dfs = 0;

        for(int i=0; i<queryWords.length; i++){     // words in the Query
            String line;
            String queryWord = queryWords[i];
            dfs=0;
            while ((line = vocabFile.readLine()) != null) {
                String[] splitLine = line.split(" ");
                String word = splitLine[0];
                if (word.equals(queryWord)) {
                    int df = Integer.parseInt(splitLine[1]);
                    dfs = dfs + df;
                    totalDfs[i] = dfs;
                }
            }
            vocabFile.seek(0); // Reset the file pointer to the beginning of the file for the next word
        }

        vocabFile.close();
        return totalDfs;
    }
}

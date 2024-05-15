package Search;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
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
    List<String> FileNames;             // correspond to the results of the search
    List<String> Snippets;              // correspond to the snippets of the results
    List<String> Scores;                // correspond to the scores of the results

    /* ---------------------------- Getters ------------------------ */
    public String getVocabularyFileName() { return VocabularyFileName; }
    public String getPostingFileName() { return PostingFileName; }
    public String[] getQuery() { return Query; }
    public int getNumResults() { return numResults; }
    public List<String> getFileNames() { return FileNames; }
    public List<String> getSnippets() { return Snippets; }
    public List<String> getScores() { return Scores; }
    /* ---------------------------- Setters ------------------------ */
    public void setVocabularyFileName(String vocabularyFileName) { VocabularyFileName = vocabularyFileName; }
    public void setPostingFileName(String postingFileName) { PostingFileName = postingFileName; }
    public void setQuery(String[] query) { Query = query; }
    public void setNumResults(int numResults) { this.numResults = numResults; }
    public void setFileNames(List<String> fileNames) { FileNames = fileNames; }
    public void setSnippets(List<String> snippets) { Snippets = snippets; }
    public void setScores(List<String> scores) { Scores = scores; }
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
        this.FileNames = new ArrayList<>();
        this.Snippets = new ArrayList<>();
        this.Scores = new ArrayList<>();
    }
    /*--------------------------------------------------------------------*/

    // test functions
    public static List<String> performSearch(String query, String type) {
        // Implement your search logic here
        // Return a list of results
        return List.of("Result 1", "Result 2", "Result 3");

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

        for( String queryWord : queryWords) {        // for each word of the QUERY
            String line;
            while ((line = vocabFile.readLine()) != null) {
                String[] splitLine = line.split(" ");
                String word = splitLine[0];         // get the word from the VOCAB file line
                if (word.equals(queryWord)) {       // FOUND the query word
                    long postingListPointer = Long.parseLong(splitLine[2]);
                    int df = Integer.parseInt(splitLine[1]);
                    System.out.println("following pointer.. "+postingListPointer);
                    postingFile.seek(postingListPointer);
                    for (int i = 0; i < df; i++) { // read the posting list (df lines

                        String postingList = postingFile.readLine();
                        String FileName = postingList.split(" ")[0];
                        System.out.println(FileName);
                        /* ----- store findings ----- */
                        FileNames.add(FileName);
                        Snippets.add("Snippet: .... ");
                        Scores.add("Score: ");
                        /* -------------------------- */
                        System.out.println("The word '" + queryWord + "' appears in documents: " + postingList);
                    }

                    //break;
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

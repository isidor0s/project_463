package Search;

import Doc_voc_data.term_data;
import QueryAnalysis.QueryEditor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public void setPaths(List<String> paths) { Paths = paths;}
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
     * @param vocabulary : the vocabulary of the collection
     * @param postingFilePath : the path of the posting file
     */
    public Search(Map<String, term_data> vocabulary, String postingFilePath, Boolean flag) {
        this.vocabulary = vocabulary;
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
        Boolean withVSM = getWithVSMflag();              // returns true if we need to do search with Vector Space Model or no
        String[] queryWords = query.split(" ");     // Split the query into words
        int NumDocs = 0;

        /*------ Vector Space Model -------*/
        if( withVSM ){ // calc VSM weights on the query
            QueryEditor queryEditor = new QueryEditor(query,queryWords.length,NumDocs); // create editor on query
            // - [ PREPROCESSING ] --------------------------------------------------------
            //  Filter out Stopwords - Stemm - Keep uniqueWords
            //  sets list CleanedQuery_l with only the unique cleaned ^ words
            //  initializes the QueryWeights with the words of the query and 0.0f
            queryEditor.preprocessQuery();

            // ----------------------------------------------------------------------------
            queryEditor.VSM();      // QueryWeights = { weights of each word of the Query }



        /*----------- No sorting ----------*/
        }else{
            RandomAccessFile postingFile = new RandomAccessFile(getPostingFileName(), "r");
            RandomAccessFile docsFile = new RandomAccessFile(new File("resources/if/DocumentsFile.txt"), "r");
            /* load vocabulary */

            for( String queryWord : queryWords) {        // for each word of the QUERY

                long postingListPointer = vocabulary.get(queryWord).getPointer();
                // a word may not exits
                // ...
                // System.out.println();
                // break ;

                int df = vocabulary.get(queryWord).getDf();
                System.out.println("following pointer.. "+postingListPointer);
                postingFile.seek(postingListPointer);
                for (int i = 0; i < df; i++) { // read the posting list (df lines
                    String postingList = postingFile.readLine();
                    String parts[] = postingList.split(" ");

                    System.out.println("DocId "+parts[0]);
                    /* ----- store findings ----- */
                    FileNames.add(parts[0]);
                    Snippets.add("Snippet: .... ");
                    Scores.add("Score: ");
                    docsFile.seek(Long.parseLong(parts[2]));
                    String doc = docsFile.readLine();
                    System.out.println("================Doc: "+doc);
                    String[] docParts = doc.split(" ");

                    Paths.add(docParts[1]);
                    /* -------------------------- */
                    System.out.println("The word '" + queryWord + "' appears in documents: " + postingList);
                }
                //break;

            }

            postingFile.close();
        }

    }

    /* -------------------------- helper functions -------------------------- */

    /**
     * Function that returns the total document frequency of a specific word
     * @param query : the words to search for
     * @return the total document frequency of the word (sum)
     * @throws IOException : if an I/O error occurs
     */
    public int[] getTotalDf(String query) throws IOException {

        String[] queryWords = query.split(" "); // Split the query into words
        int[] totalDfs = new int[queryWords.length]; // Initialize the array
        
        for(int i=0; i<queryWords.length; i++){                 // For every word in the Query
            int df = vocabulary.get(queryWords[i]).getDf();
            totalDfs [i] = df;
        }
        return totalDfs;
    }
}

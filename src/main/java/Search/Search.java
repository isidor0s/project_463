package Search;

import Doc_voc_data.term_data;
import QueryAnalysis.QueryEditor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    int NumDocs;                        // number of documents in the collection
    List<String> Paths;
    private Map<String, term_data> vocabulary;
    Boolean withVSMflag = false;        // flag to determine if we need to do search with Vector Space Model or no

    /* ---------------------------- Getters ------------------------ */
    public String getVocabularyFileName() { return VocabularyFileName; }
    public String getPostingFileName() { return PostingFileName; }
    public String[] getQuery() { return Query; }
    public int getNumResults() { return numResults; }
    public List<String> getFileNames() { return FileNames; }
    public List<String> getSnippets() { return Snippets; }
    public List<String> getScores() { return Scores; }
    public Boolean getWithVSMflag() { return withVSMflag; }
    public List<String> getPaths() { return Paths; }
    public Map<String, term_data> getVocabulary() { return vocabulary; }
    public int getNumDocs() { return NumDocs; }
    /* ---------------------------- Setters ------------------------ */
    public void setPaths(List<String> paths) { Paths = paths;}
    public void setVocabularyFileName(String vocabularyFileName) { VocabularyFileName = vocabularyFileName; }
    public void setPostingFileName(String postingFileName) { PostingFileName = postingFileName; }
    public void setQuery(String[] query) { Query = query; }
    public void setNumResults(int numResults) { this.numResults = numResults; }
    public void setFileNames(List<String> fileNames) { FileNames = fileNames; }
    public void setSnippets(List<String> snippets) { Snippets = snippets; }
    public void setScores(List<String> scores) { Scores = scores; }
    public void setWithVSMflag(Boolean flag){ withVSMflag = flag;}
    public void setVocabulary(Map<String, term_data> vocabulary) { this.vocabulary = vocabulary; }
    public void setNumDocs(int numDocs) { NumDocs = numDocs; }
    /* -------------------------- Constructors --------------------- */
    /**
     * Constructor
     * @param vocabulary : the vocabulary of the collection
     * @param postingFilePath : the path of the posting file
     */
    public Search(Map<String, term_data> vocabulary, String postingFilePath, Boolean flag,int numDocs) {
        this.vocabulary = vocabulary;
        this.PostingFileName = postingFilePath;
        this.numResults = 1;
        this.FileNames = new ArrayList<>();
        this.Snippets = new ArrayList<>();
        this.Scores = new ArrayList<>();
        this.Paths = new ArrayList<>();
        this.withVSMflag = flag;
        this.NumDocs = numDocs;
    }
    /*--------------------------------------------------------------------*/
    public static int countLines(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int lines = 0;
            while (reader.readLine() != null) lines++;
            return lines;
        }
    }
    // test functions
    public static List<String> search_wtype(String query, String type) {
        // Implement your search logic here
        // Return a list of results
        return List.of("Result 1", "Result 2", "Result 3");

    }

    /**
     * Function that will update the necessary items of the Search class,
     * given the data collected in the QueryEditor class
     */
    public void updateResults(HashMap<Long,Double> doc_CosSim) throws IOException {
        RandomAccessFile docsFile = new RandomAccessFile("resources/CollectionIndex/DocumentsFile.txt", "r");
        for (Map.Entry<Long, Double> entry : doc_CosSim.entrySet()) {

            long dpointer = entry.getKey();     // pointer to doc

            docsFile.seek(dpointer);
            String doc = docsFile.readLine();
            String[] docParts = doc.split(" ");

            String FileId = (docParts[0]);
            String FilePath = (docParts[1]);
            String Score = String.valueOf(entry.getValue());

            System.out.println("-----Update Results-----");
            // update results
            FileNames.add(FileId);
            Snippets.add("Snippet: .... ");
            Scores.add("Score: "+Score);
            Paths.add(FilePath);
        }
        // closes the file
        docsFile.close();

    }

    /**
     * Function that searches for a specific word in the vocabulary file
     * @param query : the words to search for
     * @throws IOException : if an I/O error occurs
     */
    public void search(String query) throws IOException {
        Boolean withVSM = getWithVSMflag();              // returns true if we need to do search with Vector Space Model or no
        String[] queryWords = query.split(" ");     // Split the query into words
        int NumDocs = getNumDocs();

        /*------ Vector Space Model -------*/
        if( withVSM ){ // calc VSM weights on the query
            QueryEditor queryEditor = new QueryEditor(query,queryWords.length,NumDocs , vocabulary); // create editor on query

            // - [ PREPROCESSING ] --------------------------------------------------------
            //  Filter out Stopwords - Stemm - Keep uniqueWords
            //  sets list CleanedQuery_l with only the unique cleaned ^ words
            //  initializes the QueryWeights with the words of the query and 0.0f
            queryEditor.preprocessQuery();
            // ----------------------------------------------------------------------------
            queryEditor.VSM();      // QueryWeights = { weights of each word of the Query }
            HashMap<String,Float> QueryWeights = queryEditor.getQueryWeights();
            // ----------------------------------------------------------------------------
            /* - [ COSINE SIMILARITY ] ---------------------------------------------------*/
            queryEditor.setDoc_CosSim( queryEditor.CosSim(vocabulary));     // between doc and query

            // - [ UPDATE results ] --------------------------------------------------------
            // sort the results by the score
            // ----------------------------------------------------------------------------
            queryEditor.sort( queryEditor.getDoc_CosSim() );
            // ----------------------------------------------------------------------------
            // store the results in the lists FileNames, Snippets, Scores, Paths
            updateResults( queryEditor.getDoc_CosSim() );


            // ----------------------------------------------------------------------------

        /*----------- No sorting ----------*/
        }else{
            RandomAccessFile postingFile = new RandomAccessFile(getPostingFileName(), "r");
            RandomAccessFile docsFile = new RandomAccessFile(new File("resources/CollectionIndex/DocumentsFile.txt"), "r");
            /* load vocabulary */

            for( String queryWord : queryWords) {        // for each word of the QUERY

                term_data termData = vocabulary.get(queryWord);
                long postingListPointer = 0;
                // a word may not exist in the collection
                if (termData != null) {
                    postingListPointer = termData.getPointer();
                } else {
                    System.out.println("The word '" + queryWord + "' does not exist in the collection.");
                    continue;
                }

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
                    Scores.add("Score: -");
                    docsFile.seek(Long.parseLong(parts[2]));
                    String doc = docsFile.readLine();


                    String[] docParts = doc.split(" ");
                    String numberConvertion= docParts[2].replace(",", ".");
                    float dnorm = Float.parseFloat(numberConvertion);

                    System.out.println("dnorm: "+dnorm);
                    System.out.println("================Doc: "+doc+" "+dnorm);
                    Paths.add(docParts[1]);
                    /* -------------------------- */
                    System.out.println("The word '" + queryWord + "' appears in documents: " + postingList);
                }

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
            term_data termData = vocabulary.get(queryWords[i]);
            int df = 0;
            if (termData != null) {     // the word exists in the Vocab
                df = termData.getDf();
                System.out.println(df);
            }else{                      // if the word is not found in the Vocab
                df = 0;
            }
            totalDfs [i] = df;
        }
        return totalDfs;
    }
}

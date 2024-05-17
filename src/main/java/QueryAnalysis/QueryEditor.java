package QueryAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static Doc_voc_data.utilities.*;
import static Stemming.Stemming.stemWords;

/**
 * Class that is responsible for the Analysis of the Query based on the :
 * ###################################
 * #       Vector Space Model        #
 * ###################################
 *
 * @Author: antigoni
 * @version 1.0
 *
 */
public class QueryEditor {
    String Query;                       // the query itself ---- PRIMITIVE
    List<String> CleanedQuery_l;        // the query as a list of words, but filtered out
    HashMap<String,Float> QueryWeights; // the weights of the query words
    int numQueryWords;                  // number of words in the query  ---- PRIMITIVE
    int numDocs;                        // number of documents in the collection

    /* ------------- GETTERS ---------------*/
    public String getQuery() { return Query; }
    public List<String> getCleanedQuery_l() { return CleanedQuery_l; }
    public int getNumQueryWords() {   return numQueryWords;  }
    public int getNumDocs() { return numDocs; }

    /* ------------- SETTERS ---------------*/
    public void setQuery(String query) { Query = query; }
    public void setCleanedQuery_l(List<String> cleanedQuery_l) { CleanedQuery_l = cleanedQuery_l; }
    public void setNumQueryWords(int numQueryWords) { this.numQueryWords = numQueryWords; }
    public void setNumDocs(int numDocs) { this.numDocs = numDocs; }

    /* ------------------------------------ */
    //constructors
    public QueryEditor(){
        Query="";
        CleanedQuery_l = new ArrayList<>();
        QueryWeights = new HashMap<>();
        numQueryWords=0;
        numDocs=0;
    }
    public QueryEditor(String query,int numwords, int numdocs){
        setQuery(query);
        CleanedQuery_l = new ArrayList<>();
        QueryWeights = new HashMap<>();
        setNumQueryWords(numwords);
        setNumDocs(numdocs);
    }

    /**
     * Function that initializes the QueryWeights
     * with the words of the query and 0.0f
     */
    public void init_QueryWeights(){
        for(String word : CleanedQuery_l){
            QueryWeights.put(word,0.0f);
        }
    }

    /**
     * ---------------------------------------------------------
     * Function that Preprocesses the query:
     * -- Removes duplicate words
     * -- Stems the words
     * -- Filters out stopwords
     *---------------------------------------------------------
     */
    public void preprocessQuery(){
        if(Query==null){
            System.out.println("Empty Query!");
            return;
        }
        // split the query into words
        String[] words = Query.split(" ");
        numQueryWords = words.length;

        /* ----------- Remove duplicate words - Keep Unique ------------------ */
        List<String> QueryList = findUniqueTermsOfArray(words);     // Query List --- ( used for filtering )

        /* -------------------- Stemming the Query words --------------------- */
        QueryList = stemWords(QueryList);

        /*  ---------------------- Filter Out Stopwords ---------------------- */
        List<String> termsList_filtered = FilterOutStopwords("stopwordsEn.txt", QueryList);
        termsList_filtered = FilterOutStopwords("stopwordsGr.txt", termsList_filtered);
        /* ------------------------------------------------------------------- */

        setCleanedQuery_l(termsList_filtered);  // CleanedQuery_l = termsList_filtered
        init_QueryWeights();                    // QueryWeights = { CleanedQuery_l ,  0.0f }
    }


    /**
     * Function that calculates how many times a word was found in a query
     * @param query the query to be analyzed
     */
    public HashMap<String, Float> calculateQuery_TFs(String query){
        HashMap<String, Float> TFs = new HashMap<>();

        float tf_i = 0;          // term frequency of word-i
        float max_tfi = 1;       // max term frequency of some word-i

        // for every unique word in the query
        for(String k_i : CleanedQuery_l){                // ----- UniqueWord ~ k_i ------
            tf_i = countWordOccurrences( query, k_i );   // word is found -- count x times
            /* --------- store max tf_i -------------- */
            if( max_tfi < tf_i ){  max_tfi=tf_i; }

            // TF_i = { k_i, count }
            TFs.put(k_i,(float)tf_i);                           // save in TFs map
        }
        /*---------- Normalization by max_tfi ---------*/
        for(String k_i : CleanedQuery_l){
            tf_i = TFs.get(k_i);
            float normalized_tf_i = (float) tf_i / max_tfi;
            TFs.put(k_i, normalized_tf_i);
        }
        /*---------------------------------------------*/

        return TFs;
    }

    /**
     * Function that evaluates the Vector Space Model
     * on the query (cleaned query)
     * --------------------------------------------------
     * --> calculate the weight of each word in the query
     * OUTPUT :   tf_{iq} * idf_i
     * --------------------------------------------------
     */
    public void VSM(){
        // Implement the Vector Space Model
        int k = CleanedQuery_l.size();      // number of unique words in query
        int N = numDocs;                    // number of documents in the collection

        HashMap <String,Float> normalizedTFs = calculateQuery_TFs(getQuery());

        for(String word : CleanedQuery_l){

            float tf_i = normalizedTFs.get(word);

            float idf_i =0;
            // df_i = number of documents that contain the word
            //float tf_i = 1 + (float) Math.log10(1 + QueryWeights.get(word));

            float weight = tf_i * idf_i;
            QueryWeights.put(word,weight);
        }
    }


}

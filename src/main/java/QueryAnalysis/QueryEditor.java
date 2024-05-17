package QueryAnalysis;

import Doc_voc_data.term_data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import static Doc_voc_data.utilities.*;
import static Stemming.Stemming.stemWords;


/**
 * Class that is responsible for the Analysis of the Query based on the :
 * ###################################
 * #       Vector Space Model        #
 * ###################################
 *
 * @version 1.0
 *
 */
public class QueryEditor {
    String Query;                       // the query itself ---- PRIMITIVE
    List<String> CleanedQuery_l;        // the query as a list of words, but filtered out
    HashMap<String,Float> QueryWeights; // the weights of the query words
    int numQueryWords;                  // number of words in the query  ---- PRIMITIVE
    int numDocs;                        // number of documents in the collection
    Map<String, term_data> vocabulary;  // the vocabulary of the collection
    HashMap<Long,Double> Doc_CosSim;    // the cosine similarity of the documents

    double query_DistanceVector;
    /* ------------- GETTERS ---------------*/
    public String getQuery() { return Query; }
    public List<String> getCleanedQuery_l() { return CleanedQuery_l; }
    public HashMap<String, Float> getQueryWeights() { return QueryWeights; }
    public int getNumQueryWords() {   return numQueryWords;  }
    public int getNumDocs() { return numDocs; }
    public Map<String, term_data> getVocabulary() { return vocabulary; }
    public double getQuery_DistanceVector() { return query_DistanceVector; }
    public HashMap<Long, Double> getDoc_CosSim() { return Doc_CosSim; }

    /* ------------- SETTERS ---------------*/
    public void setQuery(String query) { Query = query; }
    public void setCleanedQuery_l(List<String> cleanedQuery_l) { CleanedQuery_l = cleanedQuery_l; }
    public void setQueryWeights(HashMap<String, Float> queryWeights) { QueryWeights = queryWeights; }
    public void setNumQueryWords(int numQueryWords) { this.numQueryWords = numQueryWords; }
    public void setNumDocs(int numDocs) { this.numDocs = numDocs; }
    public void setVocabulary(Map<String, term_data> vocabulary) { this.vocabulary = vocabulary; }
    public void setQuery_DistanceVector(double query_DistanceVector) { this.query_DistanceVector = query_DistanceVector; }
    public void setDoc_CosSim(HashMap<Long, Double> doc_CosSim) { Doc_CosSim = doc_CosSim; }

    /* ------------------------------------ */
    //constructors
    public QueryEditor(){
        Query="";
        CleanedQuery_l = new ArrayList<>();
        QueryWeights = new HashMap<>();
        numQueryWords=0;
        numDocs=0;
    }
    public QueryEditor(String query,int numwords, int numdocs, Map<String, term_data> vocabulary){
        setQuery(query);
        CleanedQuery_l = new ArrayList<>();
        QueryWeights = new HashMap<>();
        setNumQueryWords(numwords);
        setNumDocs(numdocs);
        setVocabulary(vocabulary);
    }
    /**
     *
     */
    public List<String> removePunctuation ( List<String> words){
        List<String> filteredWords = new ArrayList<>();
        for(String word : words){
            String filteredWord = word.replaceAll("[^a-zA-Z0-9]", "");
            filteredWords.add(filteredWord);
        }
        System.out.println("Punctuation Removed: "+filteredWords);
        return filteredWords;

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

    public static double calculateIDF(int df, int totalDocuments) {
        // Implement IDF calculation here, e.g., log(totalDocuments / df)
        return Math.log(((double) totalDocuments / df)/Math.log(2));
    }

    /**
     * Function that calculates the idfs of the words in the query
     * @return the number of times the word appears in the query
     */
    public HashMap<String, Float> calculateIDF_query() {
        HashMap<String, Float> idfs = new HashMap<>();

        // for each word in the vocabulary
        for (String word : getVocabulary().keySet()) {
            float df_i = getVocabulary().get(word).getDf(); // get the document frequency of the word
            float idf_i = (float) Math.log(((float) numDocs / df_i)/Math.log(2));
            idfs.put(word, idf_i); // store the idf of the word
        }

        return idfs;
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
        // remove ,./;'] etc
        System.out.println("------------------------------------------------------");
        //QueryList = removePunctuation(QueryList);

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
            float normalized_tf_i = tf_i / max_tfi;
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
        /*
         * * |--- k1 -- k2 -- k3 -- k4 -- k5 -- k6 -- k7 --*
         * * |_____________________________________________*
         * * |d1|  w11   w12   w13   w14   w15   w16   w17 *
         * * |_ |                                          *
         * * |d2|  w12   w22   w23   w24   w25   w26   w27 *
         * * |_ |                                          *
         * * |d3| w13   w32   w33   w34   w35   w36   w37  *
         * * |__|__________________________________________*
         * * *--- ------------------------------------- --*
         */

        HashMap <String,Float> normalizedTFs = calculateQuery_TFs(getQuery());  // get the TFs of the query
        HashMap <String,Float> allIDFS = calculateIDF_query();  // get the TFs of the query
        query_DistanceVector = 0;
        float weight = 0;
        for(String word : CleanedQuery_l){  // for each word of the query
            Float tf_i = normalizedTFs.get(word);
            Float idf_i = allIDFS.get(word);
            if (tf_i==null || idf_i==null){
                weight = 0;
            }else{
                weight = (float) tf_i * idf_i;
            }

            query_DistanceVector = query_DistanceVector+Math.pow(weight, 2);
            //System.out.println("Word: "+word+"  Weight: "+weight);
            QueryWeights.put(word,weight);
        }
        query_DistanceVector = Math.sqrt(query_DistanceVector);
    }

    /**
     * Function that will calculate the similarity between a Document and the Query
     * @return the cosine similarity between the document and the query
     */
    public HashMap<Long, Double> CosSim(Map<String, term_data> vocabulary) throws IOException {
        float cos_sim = 0;
        RandomAccessFile postingFile = new RandomAccessFile("resources/if/PostingFile.txt", "r");

        List<String> queryWords = getCleanedQuery_l();  // each word of the query
        HashMap<Long, Double> multi_weights = new HashMap<>();  // w_id * wiq

        for( String queryWord : queryWords) {
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
            double weight_q = QueryWeights.get(queryWord);
            postingFile.seek(postingListPointer);
            for (int i = 0; i < df; i++) { // read the posting list (df lines
                String postingList = postingFile.readLine();
                String parts[] = postingList.split(" ");
                float tf = Float.parseFloat(parts[1]);
                double idf = calculateIDF(df,numDocs);
                double weight = tf * idf;


                double total_w = weight * weight_q;
                long dpointer = Long.parseLong(parts[2]);
                double currentW= multi_weights.getOrDefault(dpointer, 0.0);
                double updatedW = currentW + total_w;
                multi_weights.put(dpointer,updatedW);
            }
        }
        HashMap<Long,Double> similarity = new HashMap<>();
        RandomAccessFile docsFile = new RandomAccessFile("resources/if/DocumentsFile.txt", "r");
        for( Map.Entry<Long, Double> entry : multi_weights.entrySet()){
            long dpointer = entry.getKey();
            double multi_w = entry.getValue();

            docsFile.seek(dpointer);
            String doc = docsFile.readLine();
            String[] docParts = doc.split(" ");
            String numberConvertion= docParts[2].replace(",", ".");
            float norma = Float.parseFloat(numberConvertion);
            double cos = multi_w / (norma * query_DistanceVector);
            System.out.println("Doc: "+docParts[0]+"  Cosine Similarity: "+cos);
            similarity.put(dpointer,cos);
        }

        // return hashmap
        return similarity;
    }

    /**
     * Function that sorts the similarity hashmap
     * @param similarity a hashmap storing all the cos_sim between a doc and the query
     */
    public void sort_results (HashMap<Long, Double> similarity){
        // Convert the similarity hashmap into a list of entries
        List<HashMap.Entry<Long, Double>> list = new ArrayList<>(similarity.entrySet());

        // Sort the list using a comparator that compares the values of the entries
        list.sort(HashMap.Entry.comparingByValue(Comparator.reverseOrder()));

        // Create a new linked hashmap and put the sorted entries into it
        HashMap<Long, Double> sortedSimilarity = new LinkedHashMap<>();
        for (HashMap.Entry<Long, Double> entry : list) {
            sortedSimilarity.put(entry.getKey(), entry.getValue());
        }
        setDoc_CosSim(sortedSimilarity);
    }


}

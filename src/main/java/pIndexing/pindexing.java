package pIndexing;

import Doc_voc_data.Vocabulary;
import Doc_voc_data.document;
import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static Doc_voc_data.document.*;
//import static PostingFile.PostingFile.compute_PostingFile;


/**
 * [B6]
 * ---GOAL---:    to create an InvertedIndex File with all the relevant information
 * --------------------------------------------------------------------------------
 *  pindexing           |    holds information about :
 * --------------------------------------------------------------------------------
 * - the vocabulary         (word, df, pointer)
 * - the partial indexes    (Queue of filenames)
 * - the Posting Files      (doc_id, tf, positions, points)
 * - the threshold          (associated heap size)
 * - the docFile            (RandomAccessFile ~ DocumentsFile.txt)
 *
 * @version 2.0
 *
 */
public class pindexing {
    /* -------------------------------- Basic Parameters ---------------------------------------- */
    private static final int THRESHOLD = 1000000;                 // Threshold -- associated heap size
    static Vocabulary voc = new Vocabulary();                   // Vocabulary -- holds
    static Queue<String> partialIndexes = new LinkedList<>();   // Queue with partialIndexes' names
    static Queue<String> partialPostings = new LinkedList<>();   // Queue with partialPosting' names
    static int indexCount = 0;                                  // no use yet
    static RandomAccessFile docFile;                            // creates DocumentsFile.txt with the 3 important info - filename, filepath, tf*idf
    /* ------------------------------------------------------------------------------------------ */
    static {
        try {
            docFile = new RandomAccessFile("resources/if/DocumentsFile.txt","rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //getter
    public static Vocabulary getVoc() {
        return voc;
    }
    public static Queue<String> getPartialIndexes() {
        return partialIndexes;
    }
    /* ------------------------------------------------------------------------------------------- */


    /**
     * Function that computes the
     *
     * @param directoryPath path of the directory(files) we want to compute the metrics upon
     * @throws IOException ioexception
     */
    public static void compute_occurrences_for_directory(String directoryPath) throws IOException {
        File folder = new File(directoryPath);
        File[] list_of_files = folder.listFiles();
        int threshold_flag = 0;

        if ( list_of_files != null) {
            for (File file :  list_of_files) {
                if (file.isFile() && file.getName().endsWith(".nxml")) {

                    Path path = Paths.get(file.getAbsolutePath()); // k - absolute path of the document
                    String fileName = path.getFileName().toString();
                    String id = fileName.substring(0, fileName.lastIndexOf('.'));

                    //read the file
                    NXMLFileReader xmlFile = new NXMLFileReader(file);
                    // finds the unique terms in our xmlFile
                    List<String> uniqueTermsList = findUniqueTerms(xmlFile);
                    List<String> allTerms = findTerms(xmlFile);

                    document doc = new document(); //Make a new document class
                    doc.setDocPointer(docFile.getFilePointer()); //pass file pointer of document txt
                    voc.getDocList().put(file.getAbsolutePath(), doc);
                    docFile.writeBytes(String.format("%s %s\n",id,file.getAbsolutePath())); //write the info to documents.txt
                    /* ------------------------ Partial Index Making ... -------------------------------- */
                    for (int i = 0; i < uniqueTermsList.size(); i++) {
                        String word = uniqueTermsList.get(i);

                        // fill the Doc_TF <word, total_tf>
                        for ( String term : allTerms ){//this loop calculates the df
                            if(Objects.equals(term, word)) {
                                //get the saved document from the doclist of the vocabulary
                                voc.getDocList().get(file.getAbsolutePath()).getDoc_TF().compute(term, (k, v) -> v == null ? 1 : v + 1); // reference https://www.baeldung.com/java-word-frequency
                            }
                        }
//                        System.out.println(word+" " +doc.getDoc_TF().get(word));

                        List<String> documents = voc.getVocabulary().getOrDefault(word, new ArrayList<>()); // for df purposes
                        documents.add(file.getAbsolutePath());
                        voc.getVocabulary().put(word, documents);

                        if (voc.getVocabulary().size() >= THRESHOLD && i == uniqueTermsList.size() - 1) {
                            System.out.println(voc.getVocabulary().size());
                            threshold_flag = 1;
                            createPartialIndex();
                        }
                        // fills the Vocabulary.vocabulary with a list of docs
                    }
                    /* ----------------------------------------------------------------------------------- */
                } else if (file.isDirectory()) {
                    compute_occurrences_for_directory(file.getAbsolutePath()); // recursively search subdirectories

                }
            }

        } else {
            System.out.println("No files found in the directory.");
        }
    }

    /**
     * Function that creates a Partial Index for the Vocabulary and the Posting Files
     * Txt PartialIndex Filename : partialPosting + indexCount + .txt
     *
     * ---------------------------------- Vocabulary ---------------------------------
     * -                       <   word  ,  df  ,  pointer  >
     * -------------------------------------------------------------------------------
     * ---------------------------------- Posting ------------------------------------
     * -                   <  doc_id  ,  tf  , positions , points >
     * -------------------------------------------------------------------------------
     * @throws IOException due to the RandomAccessFile creation
     */
    private static void createPartialIndex() throws IOException {
        List<String> sortedWords = new ArrayList<>(voc.getVocabulary().keySet());
        Collections.sort(sortedWords);
        String pathPrefix = "resources/if/";
        String partialPosting = pathPrefix + "partialPosting" + partialIndexes.size() + ".txt";
        String partialVocab = pathPrefix + "partialVocab" + partialIndexes.size() + ".txt";
        // VocabularyFile.txt ---> < word df >
        // HashMap<String,Long> term_posting_pointer = new HashMap<>(); // points to ... , is located in ...
        RandomAccessFile posting = new RandomAccessFile(partialPosting, "rw"); // partial Postin
        RandomAccessFile vocab = new RandomAccessFile(partialVocab, "rw"); // partial vocabulary
//        System.out.println("docList" +voc.getDocList().size() + "\n");
        for (String word : sortedWords) {
            long pointer = posting.getFilePointer();
            voc.getVocabulary().get(word).size();
            for(String docid : voc.getVocabulary().get(word)) {
                Path path = Paths.get(docid); // k - absolute path of the document
                String fileName = path.getFileName().toString();
                String id = fileName.substring(0, fileName.lastIndexOf('.'));
                document temp  = voc.getDocList().get(docid);
                posting.writeBytes(String.format("%s %s %d\n",id,temp.getDoc_TF().get(word),temp.getDocPointer())); /// add doc pointer
            }
//            System.out.println(voc.getVocabulary().get(word).size());
            vocab.writeBytes(String.format("%s %s %d\n",word,voc.getVocabulary().get(word).size(),pointer)); // medicine 324851.nxml
//            term_posting_pointer.put(word, pointer);
        }
//        System.out.println("--------------\n"+voc.getVocabulary()+"\n--------------");

        partialIndexes.add(partialVocab); // saves the name partialIndexFile to the Queue
        partialPostings.add(partialPosting);

        voc.getVocabulary().clear();
        voc.getDocList().clear();
    }


    /***
     * Function that calculates the tf*idf weight for each word of our collection (uniwue term list)
     * @param path the path of the document on which to calculate the terms' weights
     * @return the normalization factor of the document | tf*idf| weight
     */
    public static double calculate_doc_norm(String path){
        double docLength_v = 0;
        int N = voc.getDocList().size(); // number of documents in the collection
        for (Map.Entry<String,Integer> term : voc.getDocList().get(path).getDoc_TF().entrySet()) {
            System.out.println(term.getKey() + " " + term.getValue());
            int tf = term.getValue();
            String word = term.getKey();
            int df_i = voc.getVocabulary().get(word).size();
            double idf_i = (Math.log( N/df_i )/ Math.log(2));
            double weight =  (tf * idf_i);
            docLength_v = docLength_v + Math.pow(weight,2);
        }
        return Math.sqrt(docLength_v);
    }

    /**
     * Function that merges pair of Partial Indexes (Vocabularies) saved in a queue called partialIndicesQueue
     * @param partialIndicesQueue a queue of PartialIndexFiles' names
     *
     * Saves to the merged file information about a word found (and sometimes about its df)
     * Returns the new queue with the merged partial indices
     */
    public static void mergePartialIndicesVOCAB(Queue<String> partialIndicesQueue) throws IOException {
        int initial_size = partialIndicesQueue.size();
        while (!partialIndicesQueue.isEmpty() && partialIndicesQueue.size() > 1 ){
            String partialIndex1 = partialIndicesQueue.poll(); // takes the 1st element
            String partialIndex2 = partialIndicesQueue.poll(); //
            //System.out.println(partialIndex1);
            //System.out.println(partialIndex2);

            /* Open partialIndex1 and partialIndex2, read partial vocabulary and posting file names */
            RandomAccessFile vocab1 = new RandomAccessFile(partialIndex1, "rwd");
            RandomAccessFile vocab2 = new RandomAccessFile(partialIndex2, "rwd");

            /* Create a new merged file: mergedVocab + partialIndicesQueue.size() + ".txt" */
            String mergedVocab = "resources/if/mergedVocab" + partialIndicesQueue.size() + ".txt";
            RandomAccessFile merged = new RandomAccessFile(mergedVocab, "rw");

            String line1 = vocab1.readLine(); // indice 1
            String line2 = vocab2.readLine(); // indice 2

            //System.out.println(line1);
            //System.out.println(line2);
            while (line1 != null && line2 != null) {
                String[] split1 = line1.split(" "); // space seperated values
                // get the word
                String[] split2 = line2.split(" ");

                String word1 = split1[0];   // works because each line stores a word
                String word2 = split2[0];   // ...

                if (word1.compareTo(word2) < 0) {              // word_i < word_j
                    merged.writeBytes(line1 + "\n");        // write word_i to merged file
                    line1 = vocab1.readLine();                 // moves file pointer of file vocab1
                } else if (word1.compareTo(word2) > 0) {       // word_i > word_j
                    merged.writeBytes(line2 + "\n");        // write word_j to merged file
                    line2 = vocab2.readLine();                 // moves file pointer of file vocab2
                } else if(word1.compareTo(word2) == 0){        // word_i == word_j , IT'S THE SAME WORD
                    if(split1.length>1){
                        int df = Integer.parseInt(split1[1]) + Integer.parseInt(split2[1]); // df_i + df_j
                        int p = Integer.parseInt(split1[2]); // pointer to posting file
                        merged.writeBytes(word1 + " " + df + " "+ p +"\n");  // write word_i to merged file along with its df
                    }
                    line1 = vocab1.readLine();                      // moves the files pointers
                    line2 = vocab2.readLine();                      // from both files
                }else{
                    System.out.println("Error in mergePartialIndices");
                }
            }
            /* ----------- remaining words ------------ */
            while (line1 != null) {
                merged.writeBytes(line1 + "\n");
                line1 = vocab1.readLine();
            }
            while (line2 != null) {
                merged.writeBytes(line2 + "\n");
                line2 = vocab2.readLine();
            }

            vocab1.close();
            vocab2.close();
            merged.close();

            Path path1 = Paths.get(partialIndex1);
            Path path2 = Paths.get(partialIndex1);

            //Files.deleteIfExists(path1);
            //Files.deleteIfExists(path2);
            // Delete partialIndex1 and partialIndex2
            try {
                new File(partialIndex1).delete();
                new File(partialIndex2).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Add merged file name to queue
            partialIndicesQueue.add(mergedVocab); // new updated queue
        }
    }


    /**
     * Function that merges pair of Partial Indexes (Vocabularies) saved in a queue called partialIndicesQueue_V
     * as well as pairs of other Partial Indexes (Posting Files) saved in a queue called partialIndicesQueue_P
     *
     *
     * @param partialIndicesQueue_V queue with partial indexes of the vocabulary
     * @param partialIndicesQueue_P queue with partial indexes of the posting files
     * @throws IOException ioexception
     */
    public static void mergeBOTHPartialIndices(Queue<String> partialIndicesQueue_V, Queue <String> partialIndicesQueue_P) throws IOException {

        while ((!partialIndicesQueue_V.isEmpty() && partialIndicesQueue_V.size() > 1) && (!partialIndicesQueue_P.isEmpty() && partialIndicesQueue_P.size() > 1)) {
            /* ------------------------------------ Vocabulary -------------------------------------- */
            String partialIndex1_V = partialIndicesQueue_V.poll(); // takes the 1st element
            String partialIndex2_V = partialIndicesQueue_V.poll(); // takes the 2nd element
            /* Open partialIndex1 and partialIndex2, read partial vocabulary and posting file names */
            RandomAccessFile vocab1 = new RandomAccessFile(partialIndex1_V, "r");
            RandomAccessFile vocab2 = new RandomAccessFile(partialIndex2_V, "r");
            /* Create a new merged file: mergedVocab + partialIndicesQueue.size() + ".txt" */
            String mergedVocab = "resources/if/mergedVocab" + partialIndicesQueue_V.size() + ".txt";
            RandomAccessFile merged_V = new RandomAccessFile(mergedVocab, "rw");

            String line1_V = vocab1.readLine(); // indice 1
            //System.out.println(line1_V);
            String line2_V = vocab2.readLine(); // indice 2
            //System.out.println(line2_V);
            /* -------------------------------------------------------------------------------------- */
            /* ------------------------------------- Posting ---------------------------------------- */
            String partialIndex1_P = partialIndicesQueue_P.poll(); // takes the 1st element
            String partialIndex2_P = partialIndicesQueue_P.poll(); //
            /* Open partialIndex1 and partialIndex2, read partial vocabulary and posting file names */
            RandomAccessFile post1 = new RandomAccessFile(partialIndex1_P, "r");
            RandomAccessFile post2 = new RandomAccessFile(partialIndex2_P, "r");
            /* Create a new merged file: mergedVocab + partialIndicesQueue.size() + ".txt" */
            String mergedPost = "resources/if/mergedPost" + partialIndicesQueue_P.size() + ".txt";
            RandomAccessFile merged_P = new RandomAccessFile(mergedPost, "rw");

            String line1_P = post1.readLine(); // indice 1
            String line2_P = post2.readLine(); // indice 2
            /* -------------------------------------------------------------------------------------- */
            /* ---------------------------- Merging Posting files ----------------------------------- */
            /* | posting files :  |   < doc_id  , tf  ,  pos  >                                       */
            while ( ((line1_V != null && line2_V != null)) && (line1_P != null && line2_P != null)  ) {
                String[] split1 = line1_P.split(" "); // space seperated values
                String[] split2 = line2_P.split(" ");

                String doc_id1 = split1[0];   // works because each line stores a word
                String doc_id2 = split2[0];   // ...

                String[] split1_V = line1_V.split(" "); // space seperated values
                // get the word
                String[] split2_V = line2_V.split(" ");

                String word1_V = split1_V[0];   // works because each line stores a word
                String word2_V = split2_V[0];   // ...

                /* ---  Posting : P1 < P2 --- */
                if (doc_id1.compareTo(doc_id2) < 0) {              // doc_id_i < doc_id_j , D1 < D3
                    merged_P.writeBytes(line1_P + "\n");        // write doc_id1 to merged file

                    if (word1_V.compareTo(word2_V) < 0) {              // word_i < word_j
                        int df = Integer.parseInt(split1_V[1]);
                        // update --------------------------------------------------------------------------
                        long p = merged_P.getFilePointer(); // pointer to posting file
                        merged_V.writeBytes(word1_V + " " + df + " " + p + "\n");

                        //merged_V.writeBytes(line1_V + "\n");        // write word_i to merged file
                        while (line1_V != null) {
                            line1_V = vocab1.readLine();
                        }
                        while(line1_P != null){
                            line1_P = post1.readLine();
                        }
                    } else if (word1_V.compareTo(word2_V) == 0) {
                        int df = Integer.parseInt(split1_V[1]);
                        // update --------------------------------------------------------------------------
                        long p = merged_P.getFilePointer(); // pointer to posting file
                        merged_V.writeBytes(word1_V + " " + df + " " + p + "\n");

                        merged_P.writeBytes(line2_P + "\n");        // write doc_id2 to merged file

                        df = Integer.parseInt(split2_V[1]);
                        // update --------------------------------------------------------------------------
                        p = merged_P.getFilePointer(); // pointer to posting file
                        merged_V.writeBytes(word2_V + " " + df + " " + p + "\n");

                        //merged_V.writeBytes(line1_V + "\n");        // write word_i to merged file
                        if( line1_V != null){
                            line1_V = vocab1.readLine();
                        }
                        if(line1_P != null){
                            line1_P = post1.readLine();
                        }
                        if(line2_P != null){
                            line2_P = post2.readLine();
                        }
                        if(line2_V != null){
                            line2_V = vocab2.readLine();
                        }

                    } else if (word1_V.compareTo(word2_V) > 0) {      // word_i > word_j
                        long old_p = merged_P.getFilePointer();       // p1 = pointer to posting file
                        int df = Integer.parseInt(split2_V[1]);

                        merged_P.writeBytes(line2_P + "\n");        // write doc_id2 to merged file
                        long new_p = merged_P.getFilePointer();       // p2 = pointer to posting file
                        int df2 = Integer.parseInt(split1_V[1]);
                        merged_V.writeBytes(word2_V + " " + df + " " + new_p + "\n");

                        merged_V.writeBytes(word1_V + " " + df2 + " " + old_p + "\n");

                        if( line1_V != null){
                            line1_V = vocab1.readLine();
                        }
                        if(line1_P != null){
                            line1_P = post1.readLine();
                        }
                        if(line2_P != null){
                            line2_P = post2.readLine();
                        }
                        if(line2_V != null){
                            line2_V = vocab2.readLine();
                        }
                    } else {
                        System.out.println("Error in mergePartialIndices");
                    }
                    /* ---  Posting : P2 < P1 --- */
                } else if (doc_id1.compareTo(doc_id2) > 0) {       // doc_id_i > doc_id_j , D3  < D1
                    merged_P.writeBytes(line2_P + "\n");        // write word_j to merged file

                    if (word1_V.compareTo(word2_V) < 0) {              // word_i < word_j
                        long old_p = merged_P.getFilePointer();       // p2 = pointer to posting file
                        int df = Integer.parseInt(split1_V[1]);

                        merged_P.writeBytes(line1_P + "\n");        // write doc_id1 to merged file
                        long new_p = merged_P.getFilePointer();       // p1 = pointer to posting file
                        int df2 = Integer.parseInt(split2_V[1]);
                        merged_V.writeBytes(word1_V + " " + df + " " + new_p + "\n");
                        merged_V.writeBytes(word2_V + " " + df2 + " " + old_p + "\n");

                        line1_V = vocab1.readLine();
                        line1_P = post1.readLine();
                        line2_V = vocab2.readLine();
                        line2_P = post2.readLine();
                    } else if (word1_V.compareTo(word2_V) == 0) {
                        int df = Integer.parseInt(split2_V[1]);
                        // update --------------------------------------------------------------------------
                        long p = merged_P.getFilePointer(); // pointer to posting file
                        merged_V.writeBytes(word2_V + " " + df + " " + p + "\n");

                        merged_P.writeBytes(line1_P + "\n");        // write doc_id2 to merged file

                        df = Integer.parseInt(split1_V[1]);
                        // update --------------------------------------------------------------------------
                        p = merged_P.getFilePointer(); // pointer to posting file
                        merged_V.writeBytes(word1_V + " " + df + " " + p + "\n");

                        //merged_V.writeBytes(line1_V + "\n");        // write word_i to merged file
                        line1_V = vocab1.readLine();
                        line1_P = post1.readLine();
                        line2_V = vocab2.readLine();
                        line2_P = post2.readLine();
                    } else if (word1_V.compareTo(word2_V) > 0) {      // word_i > word_j
                        int df = Integer.parseInt(split2_V[1]);
                        // update --------------------------------------------------------------------------
                        long p = merged_P.getFilePointer(); // pointer to posting file
                        merged_V.writeBytes(word2_V + " " + df + " " + p + "\n");

                        //merged_V.writeBytes(line1_V + "\n");        // write word_i to merged file
                        line2_V = vocab2.readLine();
                        line2_P = post2.readLine();
                    } else {
                        System.out.println("Error in mergePartialIndices");
                    }
                    /* -- IMPOSSIBLE -- */
                } else if (doc_id1.compareTo(doc_id2) == 0) {        // word_i == word_j , IT'S THE SAME WORD
                    System.out.println("Error in mergePartialIndices_ P1~P2");
                }
                /* -------------- remaining words --------------- */
                while ((line1_P != null)&& (line1_V != null)) {
                    merged_P.writeBytes(line1_P + "\n");
                    line1_P = post1.readLine();
                    merged_V.writeBytes(line1_V + "\n");
                    line1_V = vocab1.readLine();
                }
                while ((line2_P != null)&&(line2_V != null)) {
                    merged_P.writeBytes(line2_P + "\n");
                    line2_P = post2.readLine();
                    merged_V.writeBytes(line2_V + "\n");
                    line2_V = vocab2.readLine();
                }
                /* ---------------------------------------------- */
                /* --------------- Cleaning .. ------------------ */
                // Delete partialIndex1 and partialIndex2
                vocab1.close();
                vocab2.close();
                merged_V.close();
                post1.close();
                post2.close();
                merged_P.close();

                new File(partialIndex1_V).delete();
                new File(partialIndex2_V).delete();
                new File(partialIndex1_P).delete();
                new File(partialIndex2_P).delete();
                /* ---------------------------------------------- */
                /* ------------------------ Add Merged Queues back ----------------------- */
                // Add merged file name to queue
                partialIndicesQueue_V.add(mergedVocab); // new updated queue FOR VOCABULARY
                partialIndicesQueue_P.add(mergedPost); // new updated queue FOR POSTING
                /* ----------------------------------------------------------------------- */
            }
        }
    }



    /* ---------------------------------------------------------------------------------- */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try {
            // Specify the directory path
            String directoryPath = "resources/MedicalCollection/";

            // Compute occurrences for directory
            compute_occurrences_for_directory(directoryPath);
            createPartialIndex();
            // Print out the size of the vocabulary
            System.out.println("Vocabulary Size: " + voc.getVocabulary().size());

            // Print out the number of partial indexes created
            System.out.println("Number of Partial Indexes: " + partialIndexes.size());

            // Merge the partial indexes - every two indexes
            //mergeBOTHPartialIndices(partialIndexes, partialPostings);



        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + elapsedTime);
    }
}

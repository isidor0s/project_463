package pIndexing;

import Doc_voc_data.Vocabulary;
import Doc_voc_data.document;
import gr.uoc.csd.hy463.NXMLFileReader;

import javax.management.Query;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static Doc_voc_data.document.compute_occurrences;
import static Doc_voc_data.document.*;
//import static PostingFile.PostingFile.compute_PostingFile;
import static Doc_voc_data.Vocabulary.*;


public class pindexing {
    /* -------------------------------- Basic Parameters ---------------------------------------- */
    private static final int THRESHOLD = 10200;                  // Threshold -- heap size
    static Vocabulary voc = new Vocabulary();                   // Vocabulary -- holds
    static Queue<String> partialIndexes = new LinkedList<>();   // Queue with partialIndexes' names
    static int indexCount = 0;                                  // no use yet
    static RandomAccessFile docFile;

    static {
        try {
            docFile = new RandomAccessFile("resources/if/DocumentsFile.txt","rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    /* ------------------------------------------------------------------------------------------ */


    /**
     * Function that computes the
     *
     * @param directoryPath path of the directory(files) we want to compute the metrics upon
     * @throws IOException ioexception
     */
    public static void compute_occurrences_for_directory(String directoryPath) throws IOException {
        File folder = new File(directoryPath);
        File[] list_of_files = folder.listFiles();
        HashMap<String,Long> doc_pointers = new HashMap<>();
        if ( list_of_files != null) {
            for (File file :  list_of_files) {
                if (file.isFile() && file.getName().endsWith(".nxml")) {

                    Path path = Paths.get(file.getAbsolutePath()); // k - absolute path of the document
                    String fileName = path.getFileName().toString();
                    String id = fileName.substring(0, fileName.lastIndexOf('.'));
                    doc_pointers.put(id,docFile.getFilePointer());
                    docFile.writeUTF(id+" "+file.getAbsolutePath()+"\n");
//                    System.out.println(id+" "+ docFile.getFilePointer());


                    //read the file
                    NXMLFileReader xmlFile = new NXMLFileReader(file);

                    // finds the unique terms in our xmlFile
                    List<String> uniqueTermsList = findUniqueTerms(xmlFile);
                    List<String> allTerms = findTerms(xmlFile);

                    document doc = new document(); //Make a new document class
                    voc.getDocList().put(file.getAbsolutePath(), doc);
                    /* ------------------------ Partial Index Making ... -------------------------------- */
                    for (int i = 0; i < uniqueTermsList.size(); i++) {
                        String word = uniqueTermsList.get(i);
                        if (voc.getVocabulary().size() >= THRESHOLD && i == uniqueTermsList.size() - 1) {
                            createPartialIndex(doc_pointers);
                        }
                        // fill the Doc_TF <word, total_tf>
                        for ( String term : allTerms ){//this loop calculates the df
                            if(Objects.equals(term, word)) {
                                //get the saved document from the doclist of the vocabulary
                                voc.getDocList().get(file.getAbsolutePath()).getDoc_TF().compute(term, (k, v) -> v == null ? 1 : v + 1);
                            }
                        }
//                        System.out.println(word+" " +doc.getDoc_TF().get(word));

                        List<String> documents = voc.getVocabulary().getOrDefault(word, new ArrayList<>()); // for df purposes
                        documents.add(file.getAbsolutePath());
                        voc.getVocabulary().put(word, documents);
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
    private static void createPartialIndex(HashMap<String,Long> docpointer) throws IOException {
        List<String> sortedWords = new ArrayList<>(voc.getVocabulary().keySet());
        Collections.sort(sortedWords);
        String pathPrefix = "resources/if/";
        String partialPosting = pathPrefix + "partialPosting" + partialIndexes.size() + ".txt";
        String partialVocab = pathPrefix + "partialVocab" + partialIndexes.size() + ".txt";
        // VocabularyFile.txt ---> < word df >
        // HashMap<String,Long> term_posting_pointer = new HashMap<>(); // points to ... , is located in ...
        RandomAccessFile posting = new RandomAccessFile(partialPosting, "rw"); // partial Posting
        RandomAccessFile vocab = new RandomAccessFile(partialVocab, "rw"); // partial vocabulary
//        System.out.println("docList" +voc.getDocList().size() + "\n");
        for (String word : sortedWords) {
            long pointer = posting.getFilePointer();
            voc.getVocabulary().get(word).size();
            for(String docid : voc.getVocabulary().get(word)) {
                Path path = Paths.get(docid); // k - absolute path of the document
                String fileName = path.getFileName().toString();
                String id = fileName.substring(0, fileName.lastIndexOf('.'));
//                System.out.println(id +" " + docpointer.get(id));
                posting.writeUTF(id+" "+ voc.getDocList().get(docid).getDoc_TF().get(word) +" "+docpointer.get(id)+ "\n"); /// add doc pointer
            }
//            System.out.println(voc.getVocabulary().get(word).size());
            vocab.writeUTF(word + " " + voc.getVocabulary().get(word).size()+ " "+ pointer+"\n"); // medicine 324851.nxml
//            term_posting_pointer.put(word, pointer);
        }
//        System.out.println("--------------\n"+voc.getVocabulary()+"\n--------------");

        partialIndexes.add(partialVocab); // saves the name partialIndexFile to the Queue
        voc.getVocabulary().clear();
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
    public static Queue<String> mergePartialIndices(Queue<String> partialIndicesQueue) throws IOException {
        while (!partialIndicesQueue.isEmpty() && partialIndicesQueue.size() > 1 ){
            String partialIndex1 = partialIndicesQueue.poll(); // takes the 1st element
            String partialIndex2 = partialIndicesQueue.poll(); //
            System.out.println(partialIndex1);
            System.out.println(partialIndex2);

            /* Open partialIndex1 and partialIndex2, read partial vocabulary and posting file names */
            RandomAccessFile vocab1 = new RandomAccessFile(partialIndex1, "r");
            RandomAccessFile vocab2 = new RandomAccessFile(partialIndex2, "r");

            /* Create a new merged file: mergedVocab + partialIndicesQueue.size() + ".txt" */
            String mergedVocab = "resources/if/mergedVocab" + partialIndicesQueue.size() + ".txt";
            RandomAccessFile merged = new RandomAccessFile(mergedVocab, "rw");

            String line1 = vocab1.readUTF(); // indice 1
            String line2 = vocab2.readUTF(); // indice 2

            System.out.println(line1);
            System.out.println(line2);
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
                        merged.writeBytes(word1 + " " + df + "\n");  // write word_i to merged file along with its df
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

            // Delete partialIndex1 and partialIndex2
            vocab1.close();
            vocab2.close();
            merged.close();

            new File(partialIndex1).delete();
            new File(partialIndex2).delete();

            // Add merged file name to queue
            partialIndicesQueue.add(mergedVocab); // new updated queue
        }
        return partialIndicesQueue;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try {
            // Specify the directory path
            String directoryPath = "resources/MiniCollection/";

            // Compute occurrences for directory
            compute_occurrences_for_directory(directoryPath);

            // Print out the size of the vocabulary
            System.out.println("Vocabulary Size: " + voc.getVocabulary().size());

            // Print out the number of partial indexes created
            System.out.println("Number of Partial Indexes: " + partialIndexes.size());

            // Merge the partial indexes - every two indexes
            /*if (partialIndexes.size()%2 == 0){
                mergePartialIndices(partialIndexes);

            }else{
                for ( int i = 0; i < 9; i+=2){
                    mergePartialIndices(partialIndexes);
                }
            }*/


        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + elapsedTime);
    }
}

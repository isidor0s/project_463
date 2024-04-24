package pIndexing;

import Doc_voc_data.Vocabulary;
import Doc_voc_data.document;
import gr.uoc.csd.hy463.NXMLFileReader;

import javax.management.Query;
import java.io.File;
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
    private static final int THRESHOLD = 2000;
    static Vocabulary voc = new Vocabulary();
    static Queue<String> partialIndexes = new LinkedList<>();
    static int indexCount = 0;
    public static void compute_occurrences_for_directory(String directoryPath) throws IOException {
        File folder = new File(directoryPath);
        File[] list_of_files = folder.listFiles();

        if ( list_of_files != null) {
            for (File file :  list_of_files) {
                if (file.isFile() && file.getName().endsWith(".nxml")) {
                    //read the file
                    NXMLFileReader xmlFile = new NXMLFileReader(file);
//                  // finds the unique terms in our xmlFile
                    List<String> uniqueTermsList = findUniqueTerms(xmlFile);
                    List<String> allTerms = findTerms(xmlFile);
                    document doc = new document(); //Make a new document class
                    voc.getDocList().put(file.getAbsolutePath(), doc);
                    for (int i = 0; i < uniqueTermsList.size(); i++) {
                        String word = uniqueTermsList.get(i);
                        if (voc.getVocabulary().size() >= THRESHOLD && i == uniqueTermsList.size() - 1) {
                            createPartialIndex();
                        }
                        // fill the Doc_TF <word, total_tf>
                        for ( String term : allTerms ){//this calculates the df
                            if(Objects.equals(term, word)) {
                                voc.getDocList().get(file.getAbsolutePath()).getDoc_TF().compute(term, (k, v) -> v == null ? 1 : v + 1);
                            }
                        }
//                        System.out.println(word+" " +doc.getDoc_TF().get(word));

                        List<String> documents = voc.getVocabulary().getOrDefault(word, new ArrayList<>()); // for df purposes
                        documents.add(file.getAbsolutePath());
                        voc.getVocabulary().put(word, documents);

                        // fills the Vocabulary.vocabulary with a list of docs
                    }

                } else if (file.isDirectory()) {
                    compute_occurrences_for_directory(file.getAbsolutePath()); // recursively search subdirectories
                }
            }
        } else {
            System.out.println("No files found in the directory.");
        }
    }

    private static void createPartialIndex() throws IOException {
        List<String> sortedWords = new ArrayList<>(voc.getVocabulary().keySet());
        Collections.sort(sortedWords);
        String pathPrefix = "resources/if/";
        String partialPosting = pathPrefix + "partialPosting" + partialIndexes.size() + ".txt";
        String partialVocab = pathPrefix + "partialVocab" + partialIndexes.size() + ".txt";
        // VocabularyFile.txt ---> < word df >
//        HashMap<String,Long> term_posting_pointer = new HashMap<>(); // points to ... , is located in ...
        RandomAccessFile posting = new RandomAccessFile(partialPosting, "rw"); // partial Posting
        RandomAccessFile vocab = new RandomAccessFile(partialVocab, "rw"); // partial vocabulary

        for (String word : sortedWords) {
            long pointer = posting.getFilePointer();
            for(String docid : voc.getVocabulary().get(word)) {
                Path path = Paths.get(docid); // k - absolute path of the document
                String fileName = path.getFileName().toString();
                String id = fileName.substring(0, fileName.lastIndexOf('.'));
                posting.writeUTF(id+" "+ voc.getDocList().get(docid).getDoc_TF().get(word) + "\n"); /// add doc pointer
            }
          vocab.writeUTF(word + " " + voc.getVocabulary().get(word).size()+ " "+ pointer + "\n"); // medicine 324851.nxml
//            term_posting_pointer.put(word, pointer);
        }
//        System.out.println("--------------\n"+voc.getVocabulary()+"\n--------------");

        partialIndexes.add(partialPosting); // saves the name partialIndexFile to the Queue
        voc.getVocabulary().clear();
    }

    public static void calculate_doc_norm(){

    }

    /**
     * Function that merges pair of Psrtial Indexes saved in a queue called partialIndicesQueue
     * @param partialIndicesQueue a queue of PartialIndexFiles' names
     */
    public Queue<String> mergePartialIndices(Queue<String> partialIndicesQueue) {
        while (!partialIndicesQueue.isEmpty()) {
            String partialIndex1 = partialIndicesQueue.poll(); // takes the 1st element
            String partialIndex2 = partialIndicesQueue.poll(); // 

            // Open partialIndex1 and partialIndex2, read partial vocabulary and posting file names

            // Compare words and merge
            mergePartialIndexFiles(partialIndex1, partialIndex2);

            // Delete partialIndex1 and partialIndex2
            // Add merged file name to queue

        }
    }

    private void mergePartialIndexFiles(String partialIndex1, String partialIndex2) {

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + elapsedTime);
    }
}

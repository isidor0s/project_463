package pIndexing;

import Doc_voc_data.Vocabulary;
import Doc_voc_data.document;
import gr.uoc.csd.hy463.NXMLFileReader;

import javax.management.Query;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
                    document doc = new document(); //Make a document class
                    for (int i = 0; i < uniqueTermsList.size(); i++) {
                        String word = uniqueTermsList.get(i);

                        if (voc.getVocabulary().size() >= THRESHOLD && i == uniqueTermsList.size() - 1) {
                            //System.out.println("gamw tin manoula sou");
                            createPartialIndex();
                        }
                        //int tf = compute_term_occurrences(word, uniqueTermsList, 7, xmlFile.getTitle(), xmlFile.getAbstr(), xmlFile.getBody(), xmlFile.getJournal(), xmlFile.getPublisher(), xmlFile.getAuthors(), xmlFile.getCategories(), doc.getDoc_TF(), doc.getTerm_Position());

                        // fill the Doc_TF <word, total_tf>
                        for ( String term : allTerms ){
                            if(Objects.equals(term, word)) {
                                doc.getDoc_TF().compute(term, (k, v) -> v == null ? 1 : v + 1);
                            }
                        }
                        System.out.println(word+" " +doc.getDoc_TF().get(word));

                        List<String> documents = voc.getVocabulary().getOrDefault(word, new ArrayList<>()); // for df purposes
                        documents.add(file.getName());
                        voc.getVocabulary().put(word, documents);

                        //System.out.println(uniqueTermsList.iterator().hasNext());
                        //System.out.println(voc.getVocabulary().size());

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

        String partialIndexFileName = "resources/if/partialIndex" + partialIndexes.size() + ".txt";
        // VocabularyFile.txt ---> < word df >

        try (RandomAccessFile raf = new RandomAccessFile(partialIndexFileName, "rw")) {
            for (String word : sortedWords) {
                long pointer = raf.getFilePointer();

                    raf.writeUTF(word + " " + voc.getVocabulary().get(word).size() + "\n"); // medicine 324851.nxml
                voc.getVocabulary().put(word, Collections.singletonList(String.valueOf(pointer)));

            }
        }

        partialIndexes.add(partialIndexFileName); // saves the name partialIndexFile to the Queue
        voc.getVocabulary().clear();
    }

    //this calculates tf for tag for one term to be used inside a loop
    public static int compute_term_occurrences(String word, List<String> uniqueTerms, int numTags, String title, String abstr, String body,
                                               String journal, String publisher, ArrayList<String> authors, HashSet<String> categories, HashMap<String, Integer> Doc_TF, HashMap<String, Integer> Term_Position){
        int total_TF;
        String allTags = title + " " + abstr + " " + body + " " + journal + " " + publisher + " " + String.join(" ", authors) + " " + String.join(" ", categories);
            // given the unique terms
            total_TF = countWordOccurrences(allTags, word);
            // saves the total TF for each word in the HashMap of the caller function
            Doc_TF.put(word, total_TF);
            //System.out.println(word+"- Total TF: "+total_TF);
            if (!Term_Position.containsKey(word)) {
                int position = allTags.indexOf(word); // stores the first occurrence of the term in the document
                Term_Position.put(word, position);
            }

        return total_TF;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            // Specify the directory path
            String directoryPath = "resources/MiniCollection/diagnosis/Topic_1/3";

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

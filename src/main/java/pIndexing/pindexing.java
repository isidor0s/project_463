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
import static PostingFile.PostingFile.compute_PostingFile;
import static Doc_voc_data.Vocabulary.*;


public class pindexing {
    private static final int THRESHOLD = 2000;
    static Vocabulary voc = new Vocabulary();
    static Queue<String> partialIndexes = new LinkedList<>();
    static int indexCount = 0;
    public static void compute_occurrences_for_directory(String directoryPath) throws IOException {
        File folder = new File(directoryPath);
        File[] list_of_files = folder.listFiles();

        if ( list_of_files != null) { //   if the directory is not empty
            for (File file :  list_of_files) {
                if (file.isFile() && file.getName().endsWith(".nxml")) {
                    //read the file
                    NXMLFileReader xmlFile = new NXMLFileReader(file);
//                  // finds the unique terms in our xmlFile
                    List<String> uniqueTermsList = findUniqueTerms(xmlFile);

                    document doc = new document();
                    for (int i = 0; i < uniqueTermsList.size(); i++) { //Notes: put this loop inside the findUniqueTerms function
                        String word = uniqueTermsList.get(i);
                        if (voc.getVocabulary().size() >= THRESHOLD && i == uniqueTermsList.size() - 1) {
                            System.out.println("gamw tin manoula sou");
                            createPartialIndex();
                        }
                        compute_term_occurrences(word, uniqueTermsList, 7, xmlFile.getTitle(), xmlFile.getAbstr(), xmlFile.getBody(), xmlFile.getJournal(), xmlFile.getPublisher(), xmlFile.getAuthors(), xmlFile.getCategories(), doc.getDoc_TF(), doc.getTerm_Position());

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
    public static  Map<Integer, Integer> compute_term_occurrences (String word,List<String> uniqueTerms, int numTags, String title , String abstr ,String body,
                                                                              String journal , String publisher, ArrayList<String> authors , HashSet<String> categories, HashMap<String, Integer> Doc_TF, HashMap<String, Integer> Term_Position){
       Map<Integer, Integer> occurrences = new HashMap<>();
        int total_TF;
        String allTags = title + " " + abstr + " " + body + " " + journal + " " + publisher + " " + String.join(" ", authors) + " " + String.join(" ", categories);
        // given the unique terms
                       // for each word
            int tf = 0; // the number of times the term was found in the tag specified by the tag_id
            total_TF = 0;
            for (int tag = 0; tag < numTags; tag++) {  // for each tag

                tf = switch (tag) {
                    case 0 -> // check title
                            countWordOccurrences(title, word);
                    case 1 -> // check abstr
                            countWordOccurrences(abstr, word);
                    case 2 -> // check body
                            countWordOccurrences(body, word);
                    case 3 -> // check journal
                            countWordOccurrences(journal, word);
                    case 4 -> // check publisher
                            countWordOccurrences(publisher, word);
                    case 5 -> // check authors
                            countWordOccurrences_l(authors, word); // this function is prone to error . it seems to not see the authors' names
                    case 6 -> // check categories
                            countWordOccurrences_s(categories, word);
                    default -> tf = 0;
                };

                // stores the individual tf for a tag
                if (tf > 0) {
                    occurrences.put(tag, tf);

                }
                // adds the TFs from all the tags for each word :
                total_TF = total_TF + tf;
            }
            // saves the total TF for each word in the HashMap of the caller function
            Doc_TF.put(word, total_TF);
            //System.out.println(word+"- Total TF: "+total_TF);

            if (!Term_Position.containsKey(word)) {
                int position = allTags.indexOf(word); // stores the first occurrence of the term in the document
                Term_Position.put(word, position);
            }

        return occurrences;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            // Specify the directory path
            String directoryPath = "resources/MiniCollection/diagnosis/";

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

package xmlReader;
import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.*;
import java.util.*;


import static xmlReader.xmlReader.*;

public class folderReader {
    static HashMap<String, List<String>> vocabulary = new HashMap<>();
    static HashMap<String,xmlReader> DocList = new HashMap<>();
    public static void setVocabulary(HashMap<String, List<String>> vocabulary) {
        folderReader.vocabulary = vocabulary;
    }

    public static HashMap<String, List<String>> getVocabulary() {
        return vocabulary;
    }

    public static void compute_occurrences_for_directory(String directoryPath) throws IOException {
        File folder = new File(directoryPath);
        File[] list_of_files = folder.listFiles();

        if ( list_of_files != null) { //   if the directory is not empty
            for (File file :  list_of_files) {
                if (file.isFile() && file.getName().endsWith(".nxml")) {
                    //read the file
                    NXMLFileReader xmlFile = new NXMLFileReader(file);
                    String title = xmlFile.getTitle();
                    String abstr = xmlFile.getAbstr();
                    String body = xmlFile.getBody();
                    String journal = xmlFile.getJournal();
                    String publisher = xmlFile.getPublisher();
                    ArrayList<String> authors = xmlFile.getAuthors();
                    HashSet<String> categories = xmlFile.getCategories();

                    // finds the unique terms in our xmlFile
                    List<String> uniqueTermsList = findUniqueTerms(xmlFile);
                    for (String word: uniqueTermsList) { //Notes: put this loop inside the findUniqueTerms function
                        List<String> documents = vocabulary.getOrDefault(word, new ArrayList<>());
                        documents.add(file.getName());
                        vocabulary.put(word, documents);
                    }

                    xmlReader xmlReader = new xmlReader();
                    count = count + uniqueTermsList.size();
                    xmlReader.setUnique_word_count(uniqueTermsList.size());

                    // compute term occurrences
                    xmlReader.setTermFrequencies(compute_occurrences(uniqueTermsList, 7, title, abstr, body, journal, publisher, authors, categories, xmlReader.getDoc_TF()));
                    DocList.put(file.getAbsolutePath(), xmlReader); // add the xmlReader object to the list

                    // print results

//                    System.out.println("--------------------------------------------------------------------");
//                    System.out.println("\u001B[32mFile:" + file.getName()+"\u001B[0m");
//                    System.out.println("\u001B[0mUnique words: \u001B[34m" + uniqueTermsList.size());
//                    System.out.println("\u001B[36mTerms with Tag_id & tf: \u001B[0m ");
//                    System.out.println(xmlReader.getTermFrequencies());
                } else if (file.isDirectory()) {
                    compute_occurrences_for_directory(file.getAbsolutePath()); // recursively search subdirectories
                }
            }
        } else {
            System.out.println("No files found in the directory.");
        }

    }

    /***
     * Function that Computes the Length of the Normalized Document Vector .
     * The calculation of the Document Vector is based on the TF*IDF.
     * ----------------------------------------------------------------------------------
     * (*) TF calculation :
     *
     * -- For each Unique Term inside a Doc,
     * ADD all the appearances of the term through all the Tags.
     * -------------------------------------------------
     * (*) IDF calculation :
     *
     * log_2 ( N / df_i) , where N = all the Docs in our collection
     * &  df_i = the number of Documents that include the term i
     * --- N = calculate_number_of_Docs_in_collection()
     * --- df_i =
     * ----------------------------------------------------------------------------------
     */
    public static void calculate_normalization_factor() {
        int N = DocList.size(); // number of docs in collection
        int counter = 0;
        DocList.forEach((k,v)->{
            double docLength_v = 0;
            int df_i;           // apo to vocabulary
            double idf_i;
            for (Map.Entry<String,Integer> term : v.getDoc_TF().entrySet()) {
                int tf = term.getValue();
                String word = term.getKey();
                df_i = vocabulary.get(word).size();
                idf_i = (Math.log( N/df_i )/ Math.log(2));
                double weight =  (tf * idf_i);
                System.out.printf("%.4f , ",weight );
                docLength_v = docLength_v + Math.pow(weight,2);
            }
            double normalizationFactor = Math.sqrt(docLength_v);
            System.out.println("\nNormalization Factor for Doc: "+ k + " is: "+ normalizationFactor);

        });
    }
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        long startTime = System.currentTimeMillis();
        // specify the directory path
        String directoryPath = "resources/MiniCollection/diagnosis/Topic_1/0";
        compute_occurrences_for_directory(directoryPath);

        System.out.println("Total number of unique words in each doc found: "+ count);
        System.out.println("Vocabulary Size: "+ vocabulary.size());

        // Create CollectionIndex directory
        File dir = new File("Resources/CollectionIndex");
        if (!dir.exists()) {
            dir.mkdir();
        }

        // Create VocabularyFile.txt
        File vocabFile = new File(dir, "VocabularyFile.txt");
        vocabFile.createNewFile();

        // Write vocabulary to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(vocabFile))) {
            // Sort vocabulary
            Map<String, List<String>> sortedVocabulary = new TreeMap<>(vocabulary);
            // Write each word and its document frequency to the file
            for (Map.Entry<String, List<String>> entry : sortedVocabulary.entrySet()) {
                writer.println(entry.getKey() + " " + entry.getValue().size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // k - file
        // v - the exact xmlReader created for this use. (incl. vocabulary)
//        DocList.forEach((k,v) -> {
//            System.out.println("--------------------------------------------------------------------");
//            System.out.println("\u001B[32mFile:" + k + "\u001B[0m");
//            System.out.println("\u001B[0mUnique words: \u001B[34m" + v.getUnique_word_count());
//            System.out.println("\u001B[36mTerms with Tag_id & tf: \u001B[0m ");
//            System.out.println(v.getTermFrequencies());
//        });
        calculate_normalization_factor();
        System.out.println(DocList.size());
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + elapsedTime);
    }
}

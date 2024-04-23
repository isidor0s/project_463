package xmlReader;
import Doc_voc_data.Vocabulary;
import Doc_voc_data.document;
import PostingFile.PostingFile;
import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


import static Doc_voc_data.document.*;
import static Doc_voc_data.Vocabulary.*;
import static PostingFile.PostingFile.*;

public class folderReader {
    static Vocabulary voc = new Vocabulary();
    static PostingFile postfile = new PostingFile("PostingFile.txt");
//    static HashMap<String, List<String>> vocabulary = new HashMap<>();
//    static HashMap<String, document> DocList = new HashMap<>();
//
//    public static void setVocabulary(HashMap<String, List<String>> vocabulary) {
//        folderReader.vocabulary = vocabulary;
//    }
//
//    public static HashMap<String, List<String>> getVocabulary() {
//        return vocabulary;
//    }

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
                        List<String> documents = voc.getVocabulary().getOrDefault(word, new ArrayList<>());
                        documents.add(file.getName());
                        voc.getVocabulary().put(word, documents); // fills the Vocabulary.vocabulary with a list of docs

                    }

                    document xmlReader = new document();

                    xmlReader.setUnique_word_count(uniqueTermsList.size());

                    // compute term occurrences
                    xmlReader.setTermFrequencies(compute_occurrences(uniqueTermsList, 7, title, abstr, body, journal, publisher, authors, categories, xmlReader.getDoc_TF(),xmlReader.getTerm_Position()));
                    voc.getDocList().put(file.getAbsolutePath(), xmlReader); // add the xmlReader object to the list

                    // Function that finds the doc_ids, tf_i, pos for each word and
                    // writes the info in a posting file called PostingFile.txt (raf)
                    //                   <word, TotalTF>  ,   <word, List of Doc_names> ,       <word, pos>
                    System.out.println("Doc_TF: " + xmlReader.getDoc_TF());

//                    compute_PostingFile(xmlReader.getDoc_TF(), voc.getVocabulary(), xmlReader.getTerm_Position());


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
    public static void calculate_normalization_factor() throws IOException {
        int N = voc.getDocList().size(); // number of docs in collection

        File file = new File("Resources/CollectionIndex/DocumentsFile.txt");
        if(file.exists()){
            file.delete();
        }
        file.createNewFile();
        try(PrintWriter writer = new PrintWriter(new FileWriter(file,true))) {
            voc.getDocList().forEach((k,v)->{
                double docLength_v = 0;

                for (Map.Entry<String,Integer> term : v.getDoc_TF().entrySet()) {
                    int tf = term.getValue();
                    String word = term.getKey();
                    int df_i = voc.getVocabulary().get(word).size();
                    double idf_i = (Math.log( N/df_i )/ Math.log(2));
                    double weight =  (tf * idf_i);
                    docLength_v = docLength_v + Math.pow(weight,2);
                }
                double normalizationFactor = Math.sqrt(docLength_v);

                // Extract the ID from the file path
                Path path = Paths.get(k); // k - absolute path of the document
                String fileName = path.getFileName().toString();
                String id = fileName.substring(0, fileName.lastIndexOf('.'));

                writer.println(id + " " + k + " " + normalizationFactor);
            });
        }catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        long startTime = System.currentTimeMillis();
        // specify the directory path
        String directoryPath = "resources/MiniCollection/diagnosis/";
        compute_occurrences_for_directory(directoryPath);


        System.out.println("Vocabulary Size: "+ voc.getVocabulary().size());

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
            Map<String, List<String>> sortedVocabulary = new TreeMap<>(voc.getVocabulary());
            // Write each word and its document frequency to the file
            for (Map.Entry<String, List<String>> entry : sortedVocabulary.entrySet()) {
                writer.println(entry.getKey() + " " + entry.getValue().size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        calculate_normalization_factor(); // creates the DocumentsFile.txt



        System.out.println("Docs read" + voc.getDocList().size());
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + elapsedTime);
    }
}

//some prints to check the results
// k - file
// v - the exact xmlReader created for this use. (incl. vocabulary)
//        DocList.forEach((k,v) -> {
//            System.out.println("--------------------------------------------------------------------");
//            System.out.println("\u001B[32mFile:" + k + "\u001B[0m");
//            System.out.println("\u001B[0mUnique words: \u001B[34m" + v.getUnique_word_count());
//            System.out.println("\u001B[36mTerms with Tag_id & tf: \u001B[0m ");
//            System.out.println(v.getTermFrequencies());
//        });
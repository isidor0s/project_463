package GUI;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import Doc_voc_data.Vocabulary;
import pIndexing.pindexing;
import xmlReader.folderReader;
import static pIndexing.pindexing.mergePartialIndicesVOCAB;
import static xmlReader.folderReader.compute_occurrences_for_directory;
import static xmlReader.folderReader.calculate_normalization_factor;

public class TestingMain {

    public static void main(String[] args) throws IOException {
        folderReader folderReader = new folderReader();
        Vocabulary voc = xmlReader.folderReader.getVocabulary();

        /* ----------------------- Folder Reader part ---------------------- */
        long startTime = System.currentTimeMillis();
        // specify the directory path
        String directoryPath = "resources/MiniCollection/";
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

        /* ------------------------- pindexing part -------------------------- */
        pindexing pindexing = new pindexing();

        Queue <String> partialIndexes = pindexing.getPartialIndexes();
        long startTime2 = System.currentTimeMillis();

        try {
            // Specify the directory path
            String directoryPath_minicollection = "resources/MiniCollection/";

            // Compute occurrences for directory
            compute_occurrences_for_directory(directoryPath_minicollection);

            // Print out the size of the vocabulary
            System.out.println("Vocabulary Size: " + voc.getVocabulary().size());

            // Print out the number of partial indexes created
            System.out.println("Number of Partial Indexes: " + partialIndexes.size());

            // Merge the partial indexes - every two indexes
            if (partialIndexes.size()%2 == 0){
                mergePartialIndicesVOCAB(partialIndexes);
            }else{
                System.out.println("Odd number of partial indexes. Cannot merge.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime_pindexing = System.currentTimeMillis();
        long elapsedTime_pindexing = endTime_pindexing - startTime2;
        System.out.println("Execution time in milliseconds: " + elapsedTime_pindexing);

    }
}

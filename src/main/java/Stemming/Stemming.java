package Stemming;
import Doc_voc_data.term_data;
import mitos.stemmer.Stemmer;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.*;
import java.util.*;

/**
 * This class STEMS the terms found in B1, B2 and stores them in a txt file
 * The terms are stemmed using the mitos.stemmer.Stemmer class
 * Txt File is stored in resources/Stemming/Stemmed_words.txt
 *
 * @author antig
 *
 */
public class Stemming {

    /**
     * Function that stems the words in the given List<String
     *
     * @param words the array of words to be stemmed
     * @return the stemmed words
     */
    public static List<String> stemWords(List<String> words) {
        for (int i = 0; i < words.size(); i++) {
            words.set(i, Stemmer.Stem(words.get(i)));
        }
        return words;
    }

    /**
     * Function that given a map of terms and their data, stems the terms and returns the stemmed map
     * @param voc
     * @return
     */
    public static Map<String, term_data> stemWords_m(Map<String, term_data> voc) {
        Map<String, term_data> stemmedVoc = new HashMap<>();
        for (Map.Entry<String, term_data> entry : voc.entrySet()) {
            String stemmedWord = Stemmer.Stem(entry.getKey());
            term_data data = entry.getValue();
            stemmedVoc.put(stemmedWord, data);
        }
        return stemmedVoc;
    }
    /**
     * Reads the first word of each line from our file and returns a set of
     * these words Stemmed and Filtered to exclude Duplicates
     * @param filePath the path of the file to be read
     * @return set of Stemmed Words ( those words are the first word of each line in the file)
     */
    public static Set<String> readFirstWordsFromFile(String filePath) {
        Set<String> uniqueFirstWords = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split("\\s+");
                if (words.length > 0) {
                    uniqueFirstWords.add(Stemmer.Stem(words[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uniqueFirstWords;
    }

    /**
     * Writes the stemmed words to a file
     * @param words set of words to be written to the file
     * @param filePath the path of the file to be written
     */
    public static void writeWordsToFile(Set<String> words, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (String word : words) {
                writer.println(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Stemmer.Initialize();

        Set<String> uniqueFirstWords = readFirstWordsFromFile("resources/CollectionIndex/VocabularyFile.txt");
        writeWordsToFile(uniqueFirstWords, "resources/Stemming/Stemmed_words.txt");

    }
}

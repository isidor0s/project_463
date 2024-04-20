package Stemming;
import mitos.stemmer.Stemmer;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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

package QueryAnalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.io.*;
import java.util.*;

/**
 * Class that can be used to sort the results.txt file into an ascending order,
 * given the topic - number
 */
public class Cleaner {
    public void sortResultsFile() throws IOException {
        // read results from file (1st column)
        BufferedReader results_file = new BufferedReader(new FileReader("resources/results.txt"));
        List<String[]> lines = new ArrayList<>();

        String line;
        while ((line = results_file.readLine()) != null) {
            String[] parts = line.split("\t");
            lines.add(parts);
        }
        results_file.close();

        // Sort lines based on the topic_no
        lines.sort(Comparator.comparingInt(a -> Integer.parseInt(a[0])));

        // write the sorted lines back to a new file
        BufferedWriter writer = new BufferedWriter(new FileWriter("resources/results_sorted.txt"));
        for (String[] parts : lines) {
            writer.write(String.join("\t", parts));
            writer.newLine();
        }
        writer.close();
    }

    public static void main(String[] args) {
        Cleaner cleaner = new Cleaner();
        try {
            cleaner.sortResultsFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

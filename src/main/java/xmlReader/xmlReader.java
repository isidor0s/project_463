package xmlReader;
import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.*;
import java.util.*;

/***
 * Indexing Process:
 * [ Β1 ]
 *      Class that Reads the tags' contents of an XML file (biomedical paper) in UTF-8 encoding,
 * excluding punctuation/stopwords and Prints:
 * (i) the num of Different-Unique terms,                                                                   --check--
 * (ii) Each individual unique Term along with,                                                             --     --
 * (iii) the Tags in which the Term showed up, and                                                          --     --
 * (iv) Term Frequency (inside each tag)                                                                    --     --
 *
 * @author Antigoni
 * 04-04-2024
 * --------------------------------------------------------
 *
 */
public class xmlReader {
    // Structure that stores the terms and their info
    private int unique_word_count;
    HashMap<String, List<Integer>> termFrequencies = new HashMap<>();

    // decode tag names into integers:
    private static final int TITLE   = 0;
    private static final int ABSTR   = 1;
    private static final int BODY    = 2;
    private static final int JOURNAL = 3;
    private static final int PUBLISHER = 4;
    private static final int AUTHORS  = 5;
    private static final int CATEGORIES  = 6;

    public int getUnique_word_count() {
        return unique_word_count;
    }
    public void setUnique_word_count(int unique_word_count) {
        this.unique_word_count = unique_word_count;
    }

    /*** Function that takes a List of terms and Filters out Stopwords specified in the (filepath) txt file .
     * @param filenm , filename of the file that has the stopwords
     * @param UniqueTerms , List of words (unique)
     * @return new Filtered List of words that excludes the stopwords
     */
    public static List<String> FilterOutStopwords(String filenm, List<String> UniqueTerms){
        List<String> stopWords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("resources/Stopwords/"+filenm ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim()); // Remove leading/trailing whitespace
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(stopWords);
        // filter out stopwords
        List<String> filteredTerms = new ArrayList<>();
        for (String w : UniqueTerms ) {
            if (!stopWords.contains(w.toLowerCase())) { // case-insensitive matching
                filteredTerms.add(w);
            }
        }
        return filteredTerms;
    }

    /*** Function that Searches a given xml file to Find all the different terms in the file
     * @param file , nxml file from our collection
     * @return list of all the different words in the given nxml file
     */
    public static List <String> findUniqueTerms (NXMLFileReader file){
        List<String> uniqueTerms = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(file.getTitle());
        textBuilder.append(" ");
        textBuilder.append(file.getAbstr());
        textBuilder.append(" ");
        textBuilder.append(file.getBody());
        textBuilder.append(" ");
        textBuilder.append(file.getJournal());
        textBuilder.append(" ");
        textBuilder.append(file.getPublisher());
        textBuilder.append(" ");
        textBuilder.append(file.getAuthors());
        textBuilder.append(" ");
        textBuilder.append(file.getCategories());

        String words = textBuilder.toString().replaceAll("[^\\sa-zA-Z0-9]", " ");
        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        uniqueTerms.addAll(uniqueWords);
        String largeString = uniqueTerms.get(0);
        String[] terms = largeString.split("\\s+");  // "\\s+" matches whitespace characters
        List<String> termsList = new ArrayList<>();
        for (String word : terms) {
            termsList.add(word);
        }

        //System.out.println("\nwith stopwords:  "+termsList);
        List<String> termsList_filtered = FilterOutStopwords("stopwordsEn.txt",termsList);

        // Optional: Convert to lowercase for case-insensitive uniqueness
//        for (String word : uniqueWords) {
//            uniqueTerms.add(word.toLowerCase());
//        }

        return termsList_filtered;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, IOException {

        File example = new File("resources/MiniCollection/diagnosis/Topic_1/0/1852545.nxml");
        NXMLFileReader xmlFile = new NXMLFileReader(example);
        String pmcid = xmlFile.getPMCID();
        String title = xmlFile.getTitle();
        String abstr = xmlFile.getAbstr();
        String body = xmlFile.getBody();
        String journal = xmlFile.getJournal();
        String publisher = xmlFile.getPublisher();
        ArrayList<String> authors = xmlFile.getAuthors();
        HashSet<String> categories =xmlFile.getCategories();
        System.out.println("- PMC ID: " + pmcid);
        System.out.println("- Title: " + title);
        System.out.println("- Abstract: " + abstr);
        System.out.println("- Body: " + body);
        System.out.println("- Journal: " + journal);
        System.out.println("- Publisher: " + publisher);
        System.out.println("- Authors: " + authors);
        System.out.println("- Categories: " + categories);

        // finds the unique terms in our xmlFIle
        List <String> uniqueTermsList =findUniqueTerms(xmlFile);

        xmlReader xmlReader = new xmlReader();
        xmlReader.setUnique_word_count(uniqueTermsList.size());

        // compute term occurrences



        // prints count of unique terms
        System.out.println();
        System.out.println("--------------------------------------------------------------------");
        System.out.println("\u001B[34mUNIQUE WORDS COUNT: \u001B[0m "+uniqueTermsList.size());
        System.out.println("--------------------------------------------------------------------");

    }
}

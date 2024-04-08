package xmlReader;
import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/***
 * Indexing Process:
 * [ Β1 ]
 *      Class that Reads the tags' contents of an XML file (biomedical paper) in UTF-8 encoding,
 * excluding punctuation/stopwords and Prints:
 * (i) the num of Different-Unique terms,                                                                   --check--
 * (ii) Each individual unique Term along with,                                                             --check--
 * (iii) the Tags in which the Term showed up, and                                                          --check--
 * (iv) Term Frequency (inside each tag)                                                                    --check--
 *
 * @author Antigoni
 * 04-04-2024
 * --------------------------------------------------------
 *
 */
public class xmlReader {
    // Structure that stores the terms and their info

    /*********************************************************************************/
    private int unique_word_count;                                              // number of unique words in acquired doc
    HashMap<String, Map<Integer,Integer>> termFrequencies = new HashMap<>();    // hash mapping of words with their occurrences-info (Doc_id,tf)
    /*********************************************************************************/

    //                      setters and getters :
    public HashMap<String, Map<Integer,Integer>> getTermFrequencies() {
        return termFrequencies;
    }

    public void setTermFrequencies(HashMap<String, List<Integer>> termFrequencies) {
        this.termFrequencies = termFrequencies;
    }

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

        List<String> termsWithoutDuplicates = termsList_filtered.stream().distinct().collect(Collectors.toList());
        // Optional: Convert to lowercase for case-insensitive uniqueness
//        for (String word : uniqueWords) {
//            uniqueTerms.add(word.toLowerCase());
//        }

        return termsWithoutDuplicates;
    }


    /*** Function that searches through a text(List<String>) to find the number of times a word (String) was found.
     * @param textList , the text (tag) we want to search into
     * @param word , the term we are searching for
     * @return count , number of times the word was found
     */
    public static int countWordOccurrences_l(List<String> textList, String word) {
        if (textList == null || word == null || textList.isEmpty() || word.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String text : textList) {
            // Case-insensitive matching (optional: adjust as needed)
            if (text.toLowerCase().contains(word)) { // Adjust for whole word search if needed
                count++;
            }
        }

        return count;
    }

    /*** Function that searches through a text(Set<String>) to find the number of times a word (String) was found.
     * @param textSet , the text (tag) we want to search into
     * @param word , the term we are searching for
     * @return count , number of times the word was found
     */
    public static int countWordOccurrences_s(Set<String> textSet, String word) {
        if (textSet == null || word == null || textSet.isEmpty() || word.isEmpty()) {
            return 0;
        }

        // Convert HashSet to List (consider efficiency for large sets)
        List<String> textList = new ArrayList<>(textSet);

        return countWordOccurrences_l(textList, word); // Reuse existing function
    }

    /*** Function that searches through a text(String) to find the number of times a word (String) was found.
     * @param text , the text (tag) we want to search into
     * @param word , the term we are searching for
     * @return count , number of times the word was found
     */
    public static int countWordOccurrences(String text, String word) {
        if (text == null || word == null || text.isEmpty() || word.isEmpty()) {
            return 0;
        }
        // Regular expression for word boundaries (optional for whole words only)
        String regex = "\\b" + word + "\\b";

        // Split the text into words (case-insensitive)
        String[] words = text.toLowerCase().split("\\s+");

        int count = 0;
        for (String w : words) {
            // Case-insensitive matching
            if (w.equalsIgnoreCase(word)) {
                count++;
            }
        }

        return count;
    }




    /*** Function that for each unique term, it finds
     * the Tags in which the Term showed up, and                                                          --     --
     * Term Frequency (inside each tag)
     *
     * @param uniqueTerms
     * @param numTags , number of tags we search ( here = 7 )
     * @param title , tag
     * @param abstr , tag
     * @param body , tag
     * @param journal , tag
     * @param publisher , tag
     * @param authors , tag
     * @param categories , tag
     *
     * */
    public static HashMap<String, List<Integer>> compute_occurrences (List<String> uniqueTerms, int numTags, String title , String abstr ,String body,
                                                                      String journal , String publisher, ArrayList<String> authors , HashSet<String> categories) {

        HashMap<String, List<Integer>> occurrences = new HashMap<>();
        int tag_id = 0; // the number [0-6] of the tag , e.g. : 0 - title
        int tf = 0;     // the number of times the term was found in the tag specified by the tag_id

        // given the unique terms
        for (String w:uniqueTerms){                 // for each word
            tag_id = 0;
            tf     = 0;
            System.out.println(w);

<<<<<<< Updated upstream
            for ( int tag=0; tag<numTags; tag++ ){  // for each tag
                tag_id=tag;
                switch(tag){
                    case 0: // check title
                        tf = countWordOccurrences(title,w);
                        break;
                    case 1: // check abstr
                        tf = countWordOccurrences(abstr,w);
                        break;
                    case 2: // check body
                        tf = countWordOccurrences(body,w);
                        break;
                    case 3: // check journal
                        tf = countWordOccurrences(journal,w);
                        break;
                    case 4: // check publisher
                        tf = countWordOccurrences(publisher,w);
                        break;
                    case 5: // check authors
                        tf = countWordOccurrences_l(authors,w); // this function is prone to error . it seems to not see the authors' names
                        break;
                    case 6: // check categories
                        tf = countWordOccurrences_s(categories,w);
                        break;
                }
=======
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
                            countWordOccurrences_l(authors, word); // this function is prone to error . it seems to not see the authors' names the way we need
                    case 6 -> // check categories
                            countWordOccurrences_s(categories, word);
                    default -> tf = 0 ;
                };
>>>>>>> Stashed changes
                if(tf>0){
                    List<Integer> counts = occurrences.getOrDefault(w, new ArrayList<>());
                    counts.add(tag_id);
                    counts.add(tf);
                    occurrences.put(w, counts);
                    System.out.println(w+"  "+counts);
                }
            }

        }



        return occurrences;
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
        System.out.println(uniqueTermsList);
        System.out.println("--------------------------------------------------------------------");
        xmlReader.setTermFrequencies(compute_occurrences(uniqueTermsList,7, title , abstr , body, journal ,  publisher,  authors ,  categories));
        System.out.println("\u001B[36mTerms with Tag_id & tf: \u001B[0m "+ xmlReader.getTermFrequencies());

    }
}

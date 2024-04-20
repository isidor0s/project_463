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
 * (ii) Each individual unique Term along with,                                                             --     --
 * (iii) the Tags in which the Term showed up, and                                                          --     --
 * (iv) Term Frequency (inside each tag)                                                                    --     --
 *
 * @author Antigoni
 * @author Isidoros
 * 04-04-2024
 * --------------------------------------------------------
 *
 */
public class xmlReader {
    // Structure that stores the terms and their info
    private int unique_word_count;
    HashMap<String, Map<Integer,Integer>> termFrequencies; // mapping: < word , tag_id, tf>

    HashMap<String, Integer> Doc_TF; // mapping: < word , tf> where tf is total , meaning it includes all tags

    static int count =0;

    // Getters and Setters
    public HashMap<String, Integer> getDoc_TF() {
        return Doc_TF;
    }

    public void setDoc_TF(HashMap<String, Integer> doc_TF) {
        Doc_TF = doc_TF;
    }
    //Getter and setters
    public HashMap<String, Map<Integer,Integer>> getTermFrequencies() {
        return termFrequencies;
    }
    public void setTermFrequencies(HashMap<String, Map<Integer,Integer>> termFrequencies) {
        this.termFrequencies = termFrequencies;
    }
    // Getters and Setters
    public int getUnique_word_count() {
        return unique_word_count;
    }
    public void setUnique_word_count(int unique_word_count) {
        this.unique_word_count = unique_word_count;
    }

    /**
     * Constructor of the class xmlReader
     * Initializes the unique_word_count to 0
     * Initializes the termFrequencies to an empty HashMap
     * Doc_TF to an empty HashMap
     */
    public xmlReader(){
        this.unique_word_count = 0;
        this.termFrequencies = new HashMap<>();
        this.Doc_TF = new HashMap<>();
        this.count = 0;
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




    /** Function that for each unique term, it finds
     * the Tags in which the Term showed up, and                                                          --     --
     * Term Frequency (inside each tag)
     * Changes the value of the HashMap Doc_TF that stores the total TF for each word (incl. all tags)
     *
     * @param uniqueTerms
     * @param numTags  number of tags we search ( here = 7 )
     * @param title tag
     * @param abstr tag
     * @param body tag
     * @param journal tag
     * @param publisher tag
     * @param authors tag
     * @param categories tag
     * @param Doc_TF HashMap that stores the total TF for each word (incl. all tags)
     * */
    public static HashMap<String, Map<Integer, Integer>> compute_occurrences (List<String> uniqueTerms, int numTags, String title , String abstr ,String body,
                                                                      String journal , String publisher, ArrayList<String> authors , HashSet<String> categories, HashMap<String, Integer> Doc_TF){

        HashMap<String, Map<Integer, Integer>> occurrences = new HashMap<>();
        int tag_id = 0; // the number [0-6] of the tag , e.g. : 0 - title
        int total_TF=0;

        // given the unique terms
        for (String word:uniqueTerms){                 // for each word
            int tf=0; // the number of times the term was found in the tag specified by the tag_id
            total_TF=0;
            for ( int tag=0; tag<numTags; tag++ ){  // for each tag

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
                    default -> tf = 0 ;
                };
                //System.out.println(word+"- Tag: "+tag+" TF: "+tf);

                // stores the individual tf for a tag
                if(tf>0){
                    Map<Integer,Integer> counts = occurrences.getOrDefault(word, new HashMap<>()); // get the counts of the word
                    counts.put(tag, tf);
                    occurrences.put(word, counts);
                }
                // adds the TFs from all the tags for each word :
                total_TF = total_TF+tf;
            }
            // saves the total TF for each word in the HashMap of the caller function
            Doc_TF.put(word, total_TF);
            //System.out.println(word+"- Total TF: "+total_TF);

        }

        return occurrences;
    }

}

package Doc_voc_data;

import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class utilities {


    /**
     * Function that will find the unique terms in a given String[] array
     * and will filter out the duplicates
     *
     * @param words the array of words to be filtered
     * @return the list of unique terms
     */
    public static List<String> findUniqueTermsOfArray(String[] words) {
        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        List<String> termsWithoutDuplicates = uniqueWords.stream().distinct().collect(Collectors.toList());

        return termsWithoutDuplicates;
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
        // filter out stop words
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

        String words = textBuilder.toString().replaceAll("[^\\sa-zA-Z0-9]", " "); // without punctuation
        words = words.replaceAll("\\s+", " ");

        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        uniqueTerms.addAll(uniqueWords);
        //System.out.println("----------\n" + uniqueTerms+"\n----------");

        String largeString = uniqueTerms.get(0);
        String[] terms = largeString.split("\\s+");  // "\\s+" matches whitespace characters
        List<String> termsList = new ArrayList<>();
        for (String word : terms) {
            termsList.add(word);
        }
        List<String> termsList_filtered = FilterOutStopwords("stopwordsEn.txt",termsList);
        termsList_filtered = FilterOutStopwords("stopwordsGr.txt",termsList_filtered);
        List<String> termsWithoutDuplicates = termsList_filtered.stream().distinct().collect(Collectors.toList());
        return termsWithoutDuplicates;

    }

    public static List <String> findTerms (NXMLFileReader file){
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

        String words = textBuilder.toString().replaceAll("[^\\sa-zA-Z0-9]", " "); // without punctuation
        words = words.replaceAll("\\s+", " ");
        String[] terms = words.split("\\s+");
        List<String> termsList = new ArrayList<>();
        Collections.addAll(termsList, terms);
        List<String> termsList_filtered = FilterOutStopwords("stopwordsEn.txt",termsList);


        // garbage collection
        textBuilder = null;
        words = null;
        terms = null;
        termsList = null;
        System.gc(); // garbage collection

        return termsList_filtered;
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
//            if (w.equalsIgnoreCase(word)) {
            count++;
//            }
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
                                                                              String journal , String publisher, ArrayList<String> authors , HashSet<String> categories, HashMap<String, Integer> Doc_TF, HashMap<String, Integer> Term_Position){
        HashMap<String, Map<Integer, Integer>> occurrences = new HashMap<>();
        int total_TF;
        String allTags = title + " " + abstr + " " + body + " " + journal + " " + publisher + " " + String.join(" ", authors) + " " + String.join(" ", categories);
        // given the unique terms
        for (String word:uniqueTerms) {                 // for each word
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
                    Map<Integer, Integer> counts = occurrences.getOrDefault(word, new HashMap<>()); // get the counts of the word
                    counts.put(tag, tf);
                    occurrences.put(word, counts);
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
        }
        return occurrences;
    }
}

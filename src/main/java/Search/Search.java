package Search;

import java.util.List;

/**
 * Necessary Class for Searching
 * ---------------------------------------------------------------
 * Searching : Using a query to find a specific item in a collection.
 * We keep the results that contain at least one of the query words.
 * ---------------------------------------------------------------
 * Specify : category [ diagnosis , test , treatment ]
 * ---------------------------------------------------------------
 */
public class Search {
    String VocabularyFileName;          // correspond to the vocabulary file
    String PostingFileName;             // correspond to the posting file
    String[] Query;                     // correspond to the query written by the user



    public static List<String> performSearch(String query, String type) {
        // Implement your search logic here
        // Return a list of results
        return List.of("Result 1", "Result 2", "Result 3");

    }
    public static String performS(String query) {
        // Perform the search with the given query
        return "Results for query: \t" + query;
    }
}

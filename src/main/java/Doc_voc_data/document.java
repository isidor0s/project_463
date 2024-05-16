package Doc_voc_data;
import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class document {
    // Structure that stores the terms and their info
    private int unique_word_count;
    HashMap<String, Map<Integer,Integer>> termFrequencies; // mapping: < word , tag_id, tf>
    HashMap<String, Integer> Doc_TF; // mapping: < word , tf> where tf is total , meaning it includes all tags
    HashMap<String, Integer> Term_Position; // first occurrence of term in doc

    long docPointer; // pointer to the location in the PostingFile where the entry should be written.

    public long getDocPointer() {
        return docPointer;
    }

    public void setDocPointer(long docPointer) {
        this.docPointer = docPointer;
    }
    // Getters and Setters

    public HashMap<String, Integer> getTerm_Position() {return Term_Position;}
    public HashMap<String, Integer> getDoc_TF() {
        return Doc_TF;
    }
    public void setDoc_TF(HashMap<String, Integer> doc_TF) {
        Doc_TF = doc_TF;
    }
    public HashMap<String, Map<Integer,Integer>> getTermFrequencies() {
        return termFrequencies;
    }
    public void setTermFrequencies(HashMap<String, Map<Integer,Integer>> termFrequencies) {this.termFrequencies = termFrequencies;}
    public int getUnique_word_count() {return unique_word_count;}
    public void setUnique_word_count(int unique_word_count) {this.unique_word_count = unique_word_count;}

    /**
     * Constructor of the class xmlReader
     * Initializes the unique_word_count to 0
     * Initializes the termFrequencies to an empty HashMap
     * Doc_TF to an empty HashMap
     */
    public document(){
        this.unique_word_count = 0;
        this.termFrequencies = new HashMap<>();
        this.Doc_TF = new HashMap<>();
        this.Term_Position = new HashMap<>();
    }


}

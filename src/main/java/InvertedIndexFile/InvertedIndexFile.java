package InvertedIndexFile;
import java.util.*;
import java.io.*;
public class InvertedIndexFile {
    private Map<String, List<Integer>> index;

    /**
     * The constructor
     * @param vocabularyFilePath the path of the txt file containing the Vocabulary
     * @param postingFilePath the path of the txt file containing the Postings
     * @throws IOException
     */
    public InvertedIndexFile(String vocabularyFilePath, String postingFilePath) throws IOException {
        index = new HashMap<>();
        BufferedReader vocabReader = new BufferedReader(new FileReader(vocabularyFilePath));
        BufferedReader postingReader = new BufferedReader(new FileReader(postingFilePath));
        String line;
        while ((line = vocabReader.readLine()) != null) {
            String[] vocabParts = line.split(" ");
            String term = vocabParts[0];
            int pointer = Integer.parseInt(vocabParts[1]);
            List<Integer> postings = getPostings(pointer, postingFilePath);
            index.put(term, postings);
        }
        vocabReader.close();
        postingReader.close();
    }

    /**
     * Function that finds the list of postings for a term from the posting file
     * @param pointer location of the term's Posting List in the Posting File
     * @param postingFilePath the path of the txt file containing the Posting File
     * @return List of all the Documents Ids in which the term appears
     * @throws IOException
     */
    private List<Integer> getPostings(int pointer, String postingFilePath) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(postingFilePath, "r");
        raf.seek(pointer);
        String line = raf.readLine();
        raf.close();
        String[] postingParts = line.split(" ");
        List<Integer> postings = new ArrayList<>();
        for (String part : postingParts) {
            postings.add(Integer.parseInt(part));
        }
        return postings;
    }

    /**
     * Function that searches for a  term in the map
     * @param term word to be searched
     * @return List of Document Ids where the term appears
     */
    public List<Integer> search(String term) {
        return index.getOrDefault(term, new ArrayList<>());
    }

    public static void main(String[] args) {
        try {
            InvertedIndexFile ii = new InvertedIndexFile("resources/CollectionIndex/VocabularyFile.txt","sth");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

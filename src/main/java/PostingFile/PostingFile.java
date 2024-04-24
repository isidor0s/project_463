package PostingFile;

import java.io.*;
import java.util.*;

/**
 * [B6]
 * ---GOAL---:    to create a PostingFile.txt with all the relevant information
 *
 *α. το αναγνωριστικό του εγγράφου στο οποίο εμφανίζεται η λέξη,
 *b. το tf της λέξης στο αντίστοιχο έγγραφο,
 *c. τις θέσεις εμφάνισης της λέξης στο έγγραφο,
 *d. ένα δείκτη προς τις αντίστοιχες πληροφορίες του συγκεκριμένου εγγράφου στο DocumentsFile.txt
 * ++
 * add in Vocabulary.txt a Pointer to the beginning of the relevant Docs located in PostingFile.txt
 */


public class PostingFile {
    private RandomAccessFile raf;


    public RandomAccessFile getRaf() {
        return raf;
    }

    // constructor
    public PostingFile(String filename) {
        try {
            this.raf = new RandomAccessFile(filename, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** This function is used to write an entry to the PostingFile.
     *
     * @param documentId The ID of the document where the term is found.
     * @param tf The term frequency, which is the number of times the term appears in the document.
     * @param pos A list of positions where the term appears in the document.
     * @param pointer A pointer to the location in the PostingFile where the entry should be written.
     *
     * @throws IOException If an input or output exception
     */
    public void writeEntry( String documentId, int tf, List<Integer> pos, long pointer) {

        try {
            raf.seek(raf.length()); // Go to the end of the file

            raf.writeUTF(documentId+","+ tf +","+ pos +","+pointer);
            raf.writeInt(tf);
            raf.writeUTF(",");
            raf.writeInt(pos.size());

        for (int position : pos) {
            try {
                raf.writeInt(position);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

            raf.writeLong(pointer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

     public void readEntry(long position) throws IOException {
        raf.seek(position);
        String term = raf.readUTF();
        String documentId = raf.readUTF();
        int tf = raf.readInt();
        int positionsSize = raf.readInt();
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < positionsSize; i++) {
            positions.add(raf.readInt());
        }
        long pointer = raf.readLong();

        // use the read data- off the posting file
     }

     // Closes the RandomAccess file
     public void close() throws IOException {
        raf.close();
     }

    public static void main(String[] args) throws IOException {
        //compute_PostingFile();
    }

    /***
     *
     * @param Doc_TF < word , total_tf> where tf is total , meaning it includes all tags
     * @throws IOException
     */
    private static void compute_PostingFile(HashMap<String, Integer> Doc_TF ,  HashMap<String, List<String>> vocabulary,HashMap<String, Integer> Term_Position) throws IOException {
        PostingFile pp = new PostingFile("PostingFile.txt");  // CREATES A RANDOM ACCESS FILE
        BufferedReader vocabReader = new BufferedReader(new FileReader("resources/CollectionIndex/VocabularyFile.txt"));
        BufferedReader documentsReader = new BufferedReader(new FileReader("resources/CollectionIndex/DocumentsFile.txt"));

        String line;
        while ( (line = vocabReader.readLine()) != null) {
            // Parse the line to extract term and df
            String[] parts = line.split(" "); // eg apple 2 --> term:apple, df:2
            String term = parts[0];

            int df = Integer.parseInt(parts[1]);
            // int pointerVoc = ;

            List<String> Doc_Ids = vocabulary.get(term); // names.xml
            int tf = Doc_TF.get(term);
            int pos = Term_Position.get(term);
            //int pointerPost= ;

            // Write term's df to posting.txt
            long pointer = pp.getRaf().getFilePointer(); // Get current pointer position
            pp.getRaf().writeInt(df); // Write df

            // write lines for each different document
            for (String Doc: Doc_Ids){
                pp.getRaf().writeUTF(Doc.replace(".nxml", ""));
            }


            // Iterate over each document in documents.txt
            String docLine;
            while ((docLine = documentsReader.readLine()) != null) {
                // Parse the line to extract doc_id, tf, and tf*idf
                String[] docParts = docLine.split(" ");

                int docId = Integer.parseInt(docParts[0]);
                //double tf = Double.parseDouble(docParts[1]);

                // Write doc_id, tf*idf to posting.txt
                pp.getRaf().writeInt(docId);
                pp.getRaf().writeDouble(tf);

                // Write positions (if needed) - Example: postingFile.writeInt(position);

                // Update pointer to documents.txt for this term
                // Example: postingFile.writeLong(docPointer);
            }

        }
    }
}

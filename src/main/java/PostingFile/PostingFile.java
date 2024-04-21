package PostingFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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

    public static void main(String[] args) {
        PostingFile pp = new PostingFile("Postings.txt");

    }
}

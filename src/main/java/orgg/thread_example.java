package orgg;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class thread_example {

    private static void mergePartialIndices(String partialIndex1_V, String partialIndex2_V, String partialIndex1_P, String partialIndex2_P,int num,Queue<String> partialIndicesQueue_V, Queue<String> partialIndicesQueue_P) throws IOException {
        RandomAccessFile vocab1 = new RandomAccessFile(partialIndex1_V, "r");
        RandomAccessFile vocab2 = new RandomAccessFile(partialIndex2_V, "r");
        // get number from the file name


        RandomAccessFile merged_V = new RandomAccessFile("resources/if/mergedVocab" + num + ".txt", "rw");

        String line1_V = vocab1.readLine(); // indice 1
        //System.out.println(line1_V);
        String line2_V = vocab2.readLine(); // indice 2
        //System.out.println(line2_V);
        /* -------------------------------------------------------------------------------------- */
        /* ------------------------------------- Posting ---------------------------------------- */
        /* Open partialIndex1 and partialIndex2, read partial vocabulary and posting file names */
        RandomAccessFile post1 = new RandomAccessFile(partialIndex1_P, "r");
        RandomAccessFile post2 = new RandomAccessFile(partialIndex2_P, "r");
        /* Create a new merged file: mergedVocab + partialIndicesQueue.size() + ".txt" */
        RandomAccessFile merged_P = new RandomAccessFile("resources/if/mergedPost" + num + ".txt", "rw");

        String line1_P = post1.readLine(); // indice 1
        String line2_P = post2.readLine(); // indice 2
        /* -------------------------------------------------------------------------------------- */
        /* ---------------------------- Merging Posting files ----------------------------------- */
        /* | posting files :  |   < doc_id  , tf  ,  pos  >                                       */
        while (((line1_V != null && line2_V != null)) && (line1_P != null && line2_P != null)) {
            String[] split1 = line1_P.split(" "); // space seperated values
            String[] split2 = line2_P.split(" ");

            String doc_id1 = split1[0];   // get doc_ids
            String doc_id2 = split2[0];   // ...

            String[] split1_V = line1_V.split(" "); // space seperated values
            String[] split2_V = line2_V.split(" "); // get the words

            String word1_V = split1_V[0];   // get words
            String word2_V = split2_V[0];   // ...

            /* ---  Posting : P1 < P2 --- */
            if (doc_id1.compareTo(doc_id2) < 0) {              // doc_id_i < doc_id_j , D1 < D3
                merged_P.writeBytes(line1_P + "\n");        // write doc_id1 to merged file

                if (word1_V.compareTo(word2_V) < 0) {              // word_i < word_j
                    int df = Integer.parseInt(split1_V[1]);
                    // update --------------------------------------------------------------------------
                    long p = merged_P.getFilePointer(); // pointer to posting file
                    merged_V.writeBytes(word1_V + " " + df + " " + p + "\n");


                    while (line1_V != null) {
                        line1_V = vocab1.readLine();
                    }
                    while (line1_P != null) {
                        line1_P = post1.readLine();
                    }
                } else if (word1_V.compareTo(word2_V) == 0) {       // word_i == word_j
                    int df = Integer.parseInt(split1_V[1]);
                    // update --------------------------------------------------------------------------
                    long p = merged_P.getFilePointer(); // pointer to posting file
                    merged_V.writeBytes(word1_V + " " + df + " " + p + "\n");

                    merged_P.writeBytes(line2_P + "\n");        // write doc_id2 to merged file

                    df = Integer.parseInt(split2_V[1]);
                    // update --------------------------------------------------------------------------
                    p = merged_P.getFilePointer(); // pointer to posting file
                    merged_V.writeBytes(word2_V + " " + df + " " + p + "\n");

                    //merged_V.writeBytes(line1_V + "\n");        // write word_i to merged file
                    if (line1_V != null) {
                        line1_V = vocab1.readLine();
                    }
                    if (line1_P != null) {
                        line1_P = post1.readLine();
                    }
                    if (line2_P != null) {
                        line2_P = post2.readLine();
                    }
                    if (line2_V != null) {
                        line2_V = vocab2.readLine();
                    }

                } else if (word1_V.compareTo(word2_V) > 0) {      // word_i > word_j
                    long old_p = merged_P.getFilePointer();       // p1 = pointer to posting file
                    int df = Integer.parseInt(split2_V[1]);

                    merged_P.writeBytes(line2_P + "\n");        // write doc_id2 to merged file
                    long new_p = merged_P.getFilePointer();       // p2 = pointer to posting file
                    int df2 = Integer.parseInt(split1_V[1]);
                    merged_V.writeBytes(word2_V + " " + df + " " + new_p + "\n");

                    merged_V.writeBytes(word1_V + " " + df2 + " " + old_p + "\n");

                    if (line1_V != null) {
                        line1_V = vocab1.readLine();
                    }
                    if (line1_P != null) {
                        line1_P = post1.readLine();
                    }
                    if (line2_P != null) {
                        line2_P = post2.readLine();
                    }
                    if (line2_V != null) {
                        line2_V = vocab2.readLine();
                    }
                } else {
                    System.out.println("Error in mergePartialIndices");
                }
                /* ---  Posting : P2 < P1 --- */
            } else if (doc_id1.compareTo(doc_id2) > 0) {       // doc_id_i > doc_id_j , D3  < D1
                merged_P.writeBytes(line2_P + "\n");        // write word_j to merged file

                if (word1_V.compareTo(word2_V) < 0) {              // word_i < word_j
                    long old_p = merged_P.getFilePointer();       // p2 = pointer to posting file
                    int df = Integer.parseInt(split1_V[1]);

                    merged_P.writeBytes(line1_P + "\n");        // write doc_id1 to merged file
                    long new_p = merged_P.getFilePointer();       // p1 = pointer to posting file
                    int df2 = Integer.parseInt(split2_V[1]);
                    merged_V.writeBytes(word1_V + " " + df + " " + new_p + "\n");
                    merged_V.writeBytes(word2_V + " " + df2 + " " + old_p + "\n");

                    line1_V = vocab1.readLine();
                    line1_P = post1.readLine();
                    line2_V = vocab2.readLine();
                    line2_P = post2.readLine();
                } else if (word1_V.compareTo(word2_V) == 0) {
                    int df = Integer.parseInt(split2_V[1]);
                    // update --------------------------------------------------------------------------
                    long p = merged_P.getFilePointer(); // pointer to posting file
                    merged_V.writeBytes(word2_V + " " + df + " " + p + "\n");

                    merged_P.writeBytes(line1_P + "\n");        // write doc_id2 to merged file

                    df = Integer.parseInt(split1_V[1]);
                    // update --------------------------------------------------------------------------
                    p = merged_P.getFilePointer(); // pointer to posting file
                    merged_V.writeBytes(word1_V + " " + df + " " + p + "\n");

                    //merged_V.writeBytes(line1_V + "\n");        // write word_i to merged file
                    line1_V = vocab1.readLine();
                    line1_P = post1.readLine();
                    line2_V = vocab2.readLine();
                    line2_P = post2.readLine();
                } else if (word1_V.compareTo(word2_V) > 0) {      // word_i > word_j
                    int df = Integer.parseInt(split2_V[1]);
                    // update --------------------------------------------------------------------------
                    long p = merged_P.getFilePointer(); // pointer to posting file
                    merged_V.writeBytes(word2_V + " " + df + " " + p + "\n");

                    //merged_V.writeBytes(line1_V + "\n");        // write word_i to merged file
                    line2_V = vocab2.readLine();
                    line2_P = post2.readLine();
                } else {
                    System.out.println("Error in mergePartialIndices");
                }
                /* -- IMPOSSIBLE -- */
            } else if (doc_id1.compareTo(doc_id2) == 0) {        // di == dj , IT'S THE SAME WORD
                System.out.println("Error in mergePartialIndices_ P1~P2");
            }
            /* -------------- remaining words --------------- */
            while ((line1_P != null) && (line1_V != null)) {
                merged_P.writeBytes(line1_P + "\n");
                line1_P = post1.readLine();
                merged_V.writeBytes(line1_V + "\n");
                line1_V = vocab1.readLine();
            }
            while ((line2_P != null) && (line2_V != null)) {
                merged_P.writeBytes(line2_P + "\n");
                line2_P = post2.readLine();
                merged_V.writeBytes(line2_V + "\n");
                line2_V = vocab2.readLine();
            }
            /* ---------------------------------------------- */
            /* --------------- Cleaning .. ------------------ */
            // Delete partialIndex1 and partialIndex2
            vocab1.close();
            vocab2.close();
            merged_V.close();
            post1.close();
            post2.close();
            merged_P.close();

            new File(partialIndex1_V).delete();
            new File(partialIndex2_V).delete();
            new File(partialIndex1_P).delete();
            new File(partialIndex2_P).delete();
            /* ---------------------------------------------- */
            // Add merged file name to queue
            //partialIndicesQueue_V.add("resources/if/mergedVocab" + num + ".txt"); // new updated queue FOR VOCABULARY
            //partialIndicesQueue_P.add("resources/if/mergedPost" + num + ".txt"); // new updated queue FOR POSTING
            //System.out.println(num);
            partialIndicesQueue_V.add("resources/if/mergedVocab" + num + ".txt"); // new updated queue FOR VOCABULARY
            partialIndicesQueue_P.add("resources/if/mergedPost" + num + ".txt"); // new updated queue FOR POSTING
            /*synchronized (partialIndicesQueue_V) {

            }
            synchronized (partialIndicesQueue_P) {
            }*/
        } // maybe i need to put here some code - while != null read line of the remaining as well
    }


    /**
     * Merge the partial indices
     *
     * @param partialIndicesQueue_V
     * @param partialIndicesQueue_P
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void mergeBOTHPartials(Queue<String> partialIndicesQueue_V, Queue<String> partialIndicesQueue_P) throws IOException, InterruptedException, ExecutionException, ExecutionException {
        /* ----------------- Merge the partial indices ----------------- */
        // executer -- thread pool
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();
        /* ------------------------------------------------------------- */

        while ((!partialIndicesQueue_V.isEmpty() && partialIndicesQueue_V.size() > 1) && (!partialIndicesQueue_P.isEmpty() && partialIndicesQueue_P.size() > 1)) {
            String partialIndex1_V = partialIndicesQueue_V.poll();
            String partialIndex2_V = partialIndicesQueue_V.poll();
            String partialIndex1_P = partialIndicesQueue_P.poll();
            String partialIndex2_P = partialIndicesQueue_P.poll();

            Callable<Void> task = () -> {
                // Your merge logic here
                synchronized (partialIndicesQueue_V) {
                    System.out.println("Merging " + partialIndex1_V + " and " + partialIndex2_V);
                }
                synchronized (partialIndicesQueue_P) {
                    System.out.println("Merging " + partialIndex1_P + " and " + partialIndex2_P);
                    mergePartialIndices(partialIndex1_V, partialIndex2_V, partialIndex1_P, partialIndex2_P, partialIndicesQueue_V.size(),partialIndicesQueue_V, partialIndicesQueue_P);
                }

                return null;
            };

            futures.add(executor.submit(task)); // submit the task to the executor

        }

        executor.shutdown();

        for (Future<Void> future : futures) {
            future.get();  // wait for the task to complete
        }

        if(partialIndicesQueue_V.size() > 1 && partialIndicesQueue_P.size() > 1){
            //leftover files are from 0 to 14
            //synchronized (partialIndicesQueue_V) {
            //    mergeBOTHPartials(partialIndicesQueue_V, partialIndicesQueue_P);
            //}
            System.out.println(partialIndicesQueue_V.size());
        } else if (partialIndicesQueue_V.size() == 1 && partialIndicesQueue_P.size() == 1) {
            new File(partialIndicesQueue_V.poll()).renameTo(new File("resources/if/finalMergedVocab.txt"));
            new File(partialIndicesQueue_P.poll()).renameTo(new File("resources/if/finalMergedPost.txt"));
        }
    }
}



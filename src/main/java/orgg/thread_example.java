package orgg;
import java.util.LinkedList;
import java.util.concurrent.*;
//import javax.swing.*;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class thread_example {

    private static void mergePartialIndices(String partialIndex1_V, String partialIndex2_V, String partialIndex1_P, String partialIndex2_P, int num, Queue<String> partialIndicesQueue_V, Queue<String> partialIndicesQueue_P) throws IOException {
        RandomAccessFile vocab1 = new RandomAccessFile(partialIndex1_V, "r");
        RandomAccessFile vocab2 = new RandomAccessFile(partialIndex2_V, "r");
        // get number from the file name

        //System.out.println("File number: " + num);
        RandomAccessFile merged_V = new RandomAccessFile("resources/if/mergedVocab" + num + ".txt", "rw");

        String line1_V = vocab1.readLine(); // indices 1
        String line2_V = vocab2.readLine(); // indices 2
        /* -------------------------------------------------------------------------------------- */
        /* ------------------------------------- Posting ---------------------------------------- */
        /* Open partialIndex1 and partialIndex2, read partial vocabulary and posting file names */
        RandomAccessFile post1 = new RandomAccessFile(partialIndex1_P, "r");
        RandomAccessFile post2 = new RandomAccessFile(partialIndex2_P, "r");
        /* Create a new merged file: mergedVocab + partialIndicesQueue.size() + ".txt" */
        RandomAccessFile merged_P = new RandomAccessFile("resources/if/mergedPost" + num + ".txt", "rw");

        String line1_P = post1.readLine(); // indices 1
        String line2_P = post2.readLine(); // indices 2

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
            } else if (doc_id1.compareTo(doc_id2) == 0) {        // di == dj , IT'S THE SAME DOC
                System.out.println("Error in mergePartialIndices_ P1~P2");
            }
            /* -------------- remaining words --------------- */
            while ((line1_P != null) && (line1_V != null)) {
                merged_P.writeBytes(line1_P + "\n");
                line1_P = post1.readLine();
                if (line1_P == null) {
                    break;
                }else{
                    merged_V.writeBytes(line1_V + "\n");
                    line1_V = vocab1.readLine();
                }
            }
            while ((line2_P != null) && (line2_V != null)) {
                merged_P.writeBytes(line2_P + "\n");
                line2_P = post2.readLine();
                if(line2_P == null){
                    break;
                } else{
                    merged_V.writeBytes(line2_V + "\n");
                    line2_V = vocab2.readLine();
                }
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
            synchronized (partialIndicesQueue_V) {
                partialIndicesQueue_V.add("resources/if/mergedVocab" + num + ".txt"); // new updated queue FOR VOCABULARY
                System.out.println("Queue V has : " + partialIndicesQueue_P.size());
            }
            synchronized (partialIndicesQueue_P) {
                partialIndicesQueue_P.add("resources/if/mergedPost" + num + ".txt"); // new updated queue FOR POSTING
                System.out.println("Queue P has : " + partialIndicesQueue_P.size());
            }
            // if the queue has only one element, rename it to finalMergedVocab.txt
            if (partialIndicesQueue_V.size() == 1 && partialIndicesQueue_P.size() == 1) {
                new File(partialIndicesQueue_V.poll()).renameTo(new File("resources/if/finalMergedVocab.txt"));
                new File(partialIndicesQueue_P.poll()).renameTo(new File("resources/if/finalMergedPost.txt"));
            }
        } // maybe I need to put here some code - while != null read line of the remaining as well
    }

    /**
     * Function that Splits a Queue into two halves
     *
     * @param originalQueue the starting queue
     * @param <T>           the type of the elements in the queue( any)
     * @return an array of two queues
     */
    public static <T> Queue<T>[] splitQueue(Queue<T> originalQueue) {
        int originalSize = originalQueue.size();
        int halfSize = originalSize / 2;

        Queue<T> firstHalf = new LinkedList<>();
        Queue<T> secondHalf = new LinkedList<>();

        // Move half of the elements from the originalQueue to the firstHalf
        for (int i = 0; i < halfSize; i++) {
            firstHalf.add(originalQueue.poll());
        }

        // Move the remaining elements to the secondHalf
        while (!originalQueue.isEmpty()) {
            secondHalf.add(originalQueue.poll());
        }

        // Create an array to hold the split queues and return it
        @SuppressWarnings("unchecked")
        Queue<T>[] splitQueues = new Queue[]{firstHalf, secondHalf};
        return splitQueues;
    }

    /**
     * Merge the partial indices
     *
     * @param partialIndicesQueue_V queue of partial indices for vocabulary
     * @param partialIndicesQueue_P queue of partial indices for posting
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if a thread is interrupted
     * @throws ExecutionException   if the computation threw an exception
     */
    public static void mergeBOTHPartials(Queue<String> partialIndicesQueue_V, Queue<String> partialIndicesQueue_P) throws IOException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Queue<String> originalQueueVOCAB = partialIndicesQueue_V;
        Queue<String>[] splitQueuesVOCAB = splitQueue(originalQueueVOCAB);

        Queue<String> originalQueuePOST = partialIndicesQueue_P;
        Queue<String>[] splitQueuesPOST = splitQueue(originalQueuePOST);

        /* ------------------------- Vocabulary ------------------------ */
        Queue<String> HalfqueueV1 = splitQueuesVOCAB[0];
        Queue<String> HalfqueueV1_ = splitQueuesVOCAB[1];
        /* ---------------------------- Posting ------------------------ */
        Queue<String> HalfqueueP1 = splitQueuesPOST[0];
        Queue<String> HalfqueueP1_ = splitQueuesPOST[1];
        /* ------------------------------------------------------------- */

        System.out.println("size OF vocab: " + HalfqueueV1.size());
        System.out.println("size OF post: " + HalfqueueP1.size());
        System.out.println("size OF vocab:_ " + HalfqueueV1_.size());
        System.out.println("size OF post:_ " + HalfqueueP1_.size());

        while ((!HalfqueueV1.isEmpty() && !HalfqueueV1_.isEmpty() && HalfqueueV1.size() > 1 && HalfqueueV1_.size() > 1) &&
                (!HalfqueueP1.isEmpty() && !HalfqueueP1_.isEmpty() && HalfqueueP1.size() > 1 && HalfqueueP1_.size() > 1)) {
            System.out.println("Started Polling..");
            /* --- [ x x x x x  | . . . . . ] ---------------- 1st half -----*/
            String partialIndex1_V = HalfqueueV1.poll();
            String partialIndex2_V = HalfqueueV1.poll();
            String partialIndex1_P = HalfqueueP1.poll();
            String partialIndex2_P = HalfqueueP1.poll();
            /* --- [ . . . . .  | x x x x x ] ---------------- 2nd half -----*/
            String partialIndex1_V_ = HalfqueueV1_.poll();
            String partialIndex2_V_ = HalfqueueV1_.poll();
            String partialIndex1_P_ = HalfqueueP1_.poll();
            String partialIndex2_P_ = HalfqueueP1_.poll();
            /* --------------------------------------------------------------*/

            // Create tasks for merging
            Callable<Void> task1 = () -> {
                System.out.println("merging " + partialIndex1_V + " and " + partialIndex2_V);
                System.out.println("merging " + partialIndex1_P + " and " + partialIndex2_P);
                mergePartialIndices(partialIndex1_V, partialIndex2_V, partialIndex1_P, partialIndex2_P, HalfqueueP1.size(), HalfqueueV1, HalfqueueP1);
                return null;
            };
            Callable<Void> task2 = () -> {
                System.out.println("merging " + partialIndex1_V_ + " and " + partialIndex2_V_+ " __");
                System.out.println("merging " + partialIndex1_P_ + " and " + partialIndex2_P_+ " __");
                mergePartialIndices(partialIndex1_V_, partialIndex2_V_, partialIndex1_P_, partialIndex2_P_, HalfqueueP1_.size(), HalfqueueV1_, HalfqueueP1_);
                return null;
            };
            // Submit tasks to the executor
            Future<Void> future1 = executor.submit(task1);
            Future<Void> future2 = executor.submit(task2);

            // Wait for both threads to finish
            future1.get();
            future2.get();

        }
        executor.shutdown();

        System.out.println("size OF vocab: " + HalfqueueV1.size());
        System.out.println("size OF post: " + HalfqueueP1.size());
        System.out.println("size OF vocab_: " + HalfqueueV1_.size());
        System.out.println("size OF post_: " + HalfqueueP1_.size());
//        if (HalfqueueV1.size() > 1 && HalfqueueP1.size() > 1 ) {
//            System.out.println("---------------------");
//            System.out.println("size OF vocab: " + HalfqueueV1.size());
//            System.out.println("size OF post: " + HalfqueueP1.size());
//            mergeBOTHPartials(HalfqueueV1, HalfqueueP1);

//        if (HalfqueueV1.size() == 1 && HalfqueueP1.size() == 1) {
//            new File(HalfqueueV1.poll()).renameTo(new File("resources/if/finalMergedVocab.txt"));
//            new File(HalfqueueP1.poll()).renameTo(new File("resources/if/finalMergedPost.txt"));
//        }
//        if(HalfqueueV1_.size() > 1 && HalfqueueP1_.size() > 1 ){
//            System.out.println("---------------------");
//            System.out.println("size OF vocab_: "+HalfqueueV1_.size());
//            System.out.println("size OF post_: "+HalfqueueP1_.size());
//            mergeBOTHPartials(HalfqueueV1_, HalfqueueP1_);
//        }
//        if (HalfqueueV1_.size() == 1 && HalfqueueP1_.size() == 1) {
//            System.out.println("Merging the last two files");
//            new File(HalfqueueV1_.poll()).renameTo(new File("resources/if/finalMergedVocab_.txt"));
//            new File(HalfqueueP1_.poll()).renameTo(new File("resources/if/finalMergedPost_.txt"));
//        }
        System.out.println("finitooooooooooooooooooooooooooooooooooooooo");
    }

}



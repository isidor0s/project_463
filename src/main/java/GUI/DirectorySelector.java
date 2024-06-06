package GUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import Doc_voc_data.term_data;

import static pIndexing.pindexing.*;

public class DirectorySelector extends JFrame {
    private JButton button;
    private JLabel label;
    private JProgressBar progressBar;
    private JButton queryButton; // New button for QueryGUI
    //private pindexing pindexing;

    public DirectorySelector() {
        button = new JButton("Select Directory");
        label = new JLabel();
        progressBar = new JProgressBar();
        queryButton = new JButton("Open Query GUI"); // Initialize the new button
        //queryButton.setEnabled(false); // Disable the button initially

        // Add an action listener to the new button
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new QueryGUI(); // Open the QueryGUI when the button is clicked
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        button.addActionListener(new ActionListener() {
            @Override

            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setDialogTitle("Select a directory");
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

                int option = fileChooser.showOpenDialog(DirectorySelector.this);

                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    label.setText("Folder Selected: " + file.getName());
                    progressBar.setIndeterminate(true);
                    SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
                        @Override
                        protected List<String> doInBackground() throws Exception {
                            long startTime = System.currentTimeMillis();
                            Path directory = Paths.get("resources/if/");

                            // Create a ScheduledExecutorService that can schedule a task to run after a delay
                            //ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

                            // Schedule a task to shut down the JVM after 1 minute
                            //executor.schedule(() -> System.exit(0), 1, TimeUnit.MINUTES);

                            try {
                                Object mutex = new Object();

                                // Specify the directory path

                                String directoryPath = "resources/MiniCollection/";

                                // Compute occurrences for directory
                                compute_occurrences_for_directory(directoryPath);
                                createPartialIndex();

                                // Print out the number of partial indexes created
                                System.out.println("Number of Partial Indexes: " +  pIndexing.pindexing.getPartialIndexes().size());
                                long ptime= System.currentTimeMillis();
                                long partial_time = ptime - startTime;
                                System.out.println("Partial indexing execution time: " + partial_time );
                                // Merge the partial indexes - every two indexes
                                merge_function();

                                if (pIndexing.pindexing.getPartialIndexes().size() == 1 && pIndexing.pindexing.getPartialPostings().size() == 1) {
                                    new File(pIndexing.pindexing.getPartialIndexes().poll()).renameTo(new File("resources/if/VocabularyFile.txt"));
                                    new File(pIndexing.pindexing.getPartialPostings().poll()).renameTo(new File("resources/if/PostingFile.txt"));
                                }
//            //print items of each queue
//            System.out.println("Partial Indexes: " + partialIndexes);
//            System.out.println("Partial Postings: " + partialPostings);


                                long midtime = System.currentTimeMillis();
                                long MergeTime = midtime - ptime;

                                System.out.println("Merge execution time: " + MergeTime);
                                System.out.println("total merges " + mergeCounter);
                                String vocabularyFilePath = "resources/if/VocabularyFile.txt";
                                String postingFilePath = "resources/if/PostingFile.txt";
                                String docFilePath = "resources/if/temp.txt";
                                System.out.println("docsNumber: " + docsNumber);
                                Map<String, term_data> vocab = loadVocabulary(vocabularyFilePath);
                                long loadtime = System.currentTimeMillis();
                                System.out.println("Vocabulary load time: " + (loadtime - midtime) + " milliseconds");


                                HashMap<Long, Float> hash_map = calculateNormForAllDocs(vocab, postingFilePath, docFilePath);


                                RandomAccessFile new_docFile = new RandomAccessFile("resources/if/DocumentsFile.txt", "rw");
                                RandomAccessFile docFile = new RandomAccessFile(docFilePath, "rw");
                                long docPointer = 0;
                                String line = docFile.readLine();
                                DecimalFormat dec = new DecimalFormat("#00.000000");
                                while(line!= null){
                                    String[] parts = line.split(" ");
                                    float docNorm = hash_map.get(docPointer);
                                    double docNorm1 = Math.sqrt(docNorm);

                                    long newpointer = new_docFile.getFilePointer();

                                    new_docFile.writeBytes(String.format("%s %s %s\n", parts[0], parts[1], dec.format(docNorm1)));
                                    docPointer = docFile.getFilePointer();
                                    line = docFile.readLine();
                                }

                                docFile.close();
                                new_docFile.close();

                                File file = new File(docFilePath);
                                if(file.delete()){
                                    System.out.println(docFilePath + " file deleted");
                                } else {
                                    System.out.println("File " + docFilePath + " does not exist or failed to delete");
                                }
                                long calcalation_time = System.currentTimeMillis();
                                System.out.println("Calculation time: " + (calcalation_time - loadtime) + " milliseconds");

                            } catch (IOException e) {
                                e.printStackTrace();

                            }

                            long endTime = System.currentTimeMillis();
                            long elapsedTime = endTime - startTime;
                            System.out.println("Total execution Time: " + elapsedTime);

                            return List.of("Result 1", "Result 2", "Result 3");

                        }
                        @Override
                        protected void done() {
                            try {
                                List<String> result = get();
                                label.setText("      Computation Complete!");
                                progressBar.setIndeterminate(false);
                                queryButton.setEnabled(true);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    worker.execute();
                } else {
                    label.setText("Open command canceled");
                }
            }
        });
        setLayout(new FlowLayout());
        setSize(300, 200);
        add(button);
        add(label);
        add(progressBar);
        // Add vertical padding after the progress bar
        add(Box.createVerticalStrut(10)); // Change the value to increase or decrease the padding

        add(queryButton,BorderLayout.SOUTH); // Add the new button to the frame
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //pack();
        setVisible(true);
    }

    public static void main(String[] args) {

        new DirectorySelector();

    }

}
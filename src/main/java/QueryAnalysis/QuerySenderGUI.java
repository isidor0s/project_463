package QueryAnalysis;

import Doc_voc_data.term_data;
import Search.Search;
import pIndexing.pindexing;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuerySenderGUI {
    private JFrame frame;
    private JTextArea queryArea;
    private JButton sendButton;
    private IRQualityEvaluator irQualityEvaluator;
    private Map<String, term_data> vocabulary;
    private Search search;
    private JProgressBar progressBar;

    public Search getSearch() { return search; }
    public Map<String, term_data> getVocabulary() { return vocabulary; }
    public void setSearch(Search search) { this.search = search; }
    public void setVocabulary(Map<String, term_data> vocabulary) { this.vocabulary = vocabulary; }

    // constructor for QuerySenderGUI
    public QuerySenderGUI() throws IOException {
        irQualityEvaluator = new IRQualityEvaluator();
        vocabulary =  pindexing.loadVocabulary("resources/if/VocabularyFile.txt");
        frame = new JFrame("Query Sender");
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        queryArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(queryArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        sendButton = new JButton("Send Queries");
        frame.add(sendButton, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryArea.append("\t\t Queries sent:" + "\n");
                sendQueries();
            }
        });

        progressBar = new JProgressBar(0, 100); // Initialize the progress bar
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        frame.add(progressBar, BorderLayout.NORTH); // Add the progress bar to the frame

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void sendQueries() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    //key 1-diagnosis , value - keimeno(query)
                    HashMap<String, String> queries = irQualityEvaluator.readTopicsXML(0);
                    int totalQueries = queries.size();
                    int currentQuery = 0;

                    for (String query : queries.values()) {
                        String key = queries.keySet().stream().filter(k -> queries.get(k).equals(query)).findFirst().get();
                        String[] parts = key.split("-");
                        parts[1] = parts[1].toLowerCase();
                        System.out.println(parts[1]);
                        // store the key of the query
                        queryArea.append(query + "\n\n");
                        String postingFilePath = "resources/if/PostingFile.txt";
                        Boolean flag = true;
                        int numDocs = 107871;
                        // Send each query to the QueryGUI
                        search = new Search(vocabulary, postingFilePath, flag, numDocs); // search w/ VSM option on

                        search.search(query, null); // this is the type of the topic / query [ test / medication / diagnosis ]
                        // ------------------------------------------------------
                        // get the 1000 First Relevant documents (highest score)
                        // -----------   TOPIC_NO    Q0    PMCID    RANK     SCORE    RUN_NAME   ----------
                        int Q0 = 0;    // standard
                        int RANK = 1;  // starts from 1 - > 1000
                        // ------------------------------------------------------

                        List<String> FileNames = search.getFileNames();
                        List<String> Scores = search.getScores();
                        List<String> Paths = search.getPaths();

                        // get data from search.FileNames etc and store them in a file results.txt
                        for (int i = 0; i < 1000; i++) {
                            int TOPIC_NO = Integer.parseInt(parts[0]);  // key number

                            String Path = Paths.get(i);

                            File file = new File(Path);
                            String PMCID = file.getName();// file name
                            RANK = i + 1;                               // starts from 1
                            String SCORE = Scores.get(i);               // score

                            // save the info in one line of the results.txt file
                            irQualityEvaluator.writeResultsFile(TOPIC_NO, Q0, PMCID, RANK, SCORE, "1");

                        }
                        currentQuery++;
                        int progress = (int) ((currentQuery / (double) totalQueries) * 100);
                        progressBar.setValue(progress);
                    }
                    // Update the progress bar
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();
    }
    public static void main(String[] args) throws IOException {
        QuerySenderGUI qs_gui = new QuerySenderGUI();

    }
}

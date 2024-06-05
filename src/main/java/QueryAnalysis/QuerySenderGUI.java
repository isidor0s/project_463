package QueryAnalysis;

import Doc_voc_data.term_data;
import Search.Search;
import pIndexing.pindexing;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class QuerySenderGUI {
    private JFrame frame;
    private JTextArea queryArea;
    private JButton sendButton;
    private IRQualityEvaluator irQualityEvaluator;

    public QuerySenderGUI() {
        irQualityEvaluator = new IRQualityEvaluator();

        frame = new JFrame("Query Sender");
        frame.setSize(400, 300);
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

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void sendQueries() {
        try {
            HashMap<String, String> queries = irQualityEvaluator.readTopicsXML(0);
            for (String query : queries.values()) {
                String key = queries.keySet().stream().filter(k -> queries.get(k).equals(query)).findFirst().get();
                String[] parts = key.split("-");
                // store the key of the query
                //Map<String, term_data> vocabulary =  pindexing.loadVocabulary("resources/if/VocabularyFile.txt");
                String postingFilePath= "resources/if/PostingFile.txt";
                Boolean flag=true;
                int numDocs = 107871;
                // Send each query to the QueryGUI
                //Search search = new Search(vocabulary,postingFilePath,flag,numDocs);

                //search.search(query,parts[1]);
                // For now, just append it to the text area
                queryArea.append(query + "\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new QuerySenderGUI();
    }
}
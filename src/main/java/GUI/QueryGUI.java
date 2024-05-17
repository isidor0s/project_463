package GUI;import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import Doc_voc_data.term_data;
import Search.Search;

import static pIndexing.pindexing.loadVocabulary;

public class QueryGUI {
    private JFrame frame;
    private JLabel queryLabel;
    private JTextField queryField;
    private JLabel typeLabel;
    private JTextField typeField;
    private JButton searchButton;
    private JTextArea resultArea;
    private int numResultButtons;
    private JPanel buttonPanel;
    private JScrollPane scrollPane;
    private JCheckBox VSM_checkBox;
    private Search search;
    private Boolean VSMflag;

    private Map<String, term_data> LoadedVocab;



    public static int countLines(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int lines = 0;
            while (reader.readLine() != null) lines++;
            return lines;
        }
    }

    /**
     * Function that checks the VSMflag flag
     * @returns True when the flag is raised , False when it is no
     */
    public Boolean checkVSMflag(Boolean flag){
        VSMflag = flag;
        if(VSMflag==true){
            return true;
        }else{
            return false;
        }
    }
    /**
     * Function that generates n * ResultButtons in the main area of the Display,
     * to showcase each individual file and its metrics ( filepath - snippet - score).
     * -------------------------------------------------------------------------------
     * Items are added on the ---> buttonPane
     * n - numResultButton
     * -------------------------------------------------------------------------------
     */
    private void generateButtons(){
        // Create a JPanel to hold the buttons
        buttonPanel.removeAll();
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(numResultButtons, 1)); // Set layout as grid with n rows and 1 column
        buttonPanel.setBounds(10, 140, 760, numResultButtons * 80); // Set bounds, adjust as per your requirement

        // TOO MANY BUTTONS TO CREATE
        if(numResultButtons>20){
            // Create n buttons and add them to the panel
            for (int i = 0; i < 20; i++) {   // 7 -- will be replaced with numButtons
                // - buttonText -
                /* * FILE PATH ,
                 * * SNIPPET ,
                 * * SCORE */
                //System.out.println("Creating button " + i);

                String DOCID = search.getFileNames().get(i); // updating filenames..
                String SNIPPET = search.getSnippets().get(i);
                String SCORE = search.getScores().get(i);
                String Path = search.getPaths().get(i);
                String buttonText = "<html><b>" + i + "</b> : <pre>" +
                        "Doc id: "+ DOCID +"<br>"+"File path:"+Path+"<br>" + SNIPPET + "<br>" + SCORE + "</pre></html>";

                JButton button = new JButton(buttonText);
                button.setHorizontalAlignment(SwingConstants.LEFT);
                buttonPanel.add(button);
            }
        }else{
            // Create n buttons and add them to the panel
            for (int i = 0; i < numResultButtons; i++) {   // 7 -- will be replaced with numButtons
                // - buttonText -
                /* * FILE PATH ,
                 * * SNIPPET ,
                 * * SCORE */
                //System.out.println("Creating button " + i);

                String DOCID = search.getFileNames().get(i); // updating filenames..
                String SNIPPET = search.getSnippets().get(i);
                String SCORE = search.getScores().get(i);
                String Path = search.getPaths().get(i);
                String buttonText = "<html><b>" + i + "</b> : <pre>" +
                        "Doc id: "+ DOCID +"<br>"+"File path:"+Path+"<br>" + SNIPPET + "<br>" + SCORE + "</pre></html>";

                JButton button = new JButton(buttonText);
                button.setHorizontalAlignment(SwingConstants.LEFT);

                buttonPanel.add(button);
            }
        }


        //JScrollBar vertical = new JScrollBar(JScrollBar.VERTICAL);
        scrollPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(10,140, 760, numResultButtons * 80);
        scrollPane.setVisible(true);
        // Add the scrollPane to the layout
        frame.add(scrollPane, BorderLayout.CENTER);

// Refresh the button panel
        buttonPanel.revalidate();
        buttonPanel.repaint();
        frame.setVisible(true);
    }

    public QueryGUI() throws IOException {
        numResultButtons=0;
        VSMflag = false;

        /* Pre Load Vocabulary in Memory*/
        try {
            LoadedVocab = loadVocabulary("resources/if/VocabularyFile.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create the main frame
        frame = new JFrame("Query System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create the query label
        queryLabel = new JLabel("Query:");
        queryLabel.setBounds(10, 10, 50, 30);

        // Create the query field
        queryField = new JTextField(20);
        queryField.setBounds(70, 10, 300, 30);

        // Create the type label
        typeLabel = new JLabel("Type:");
        typeLabel.setBounds(10, 50, 50, 30);
        // action listener for type field


        // Create the type field
        typeField = new JTextField(20);
        typeField.setBounds(70, 50, 300, 30);

        // Create a checkbox for the ability to do VSM model analysis
        VSM_checkBox = new JCheckBox();
        VSM_checkBox.setText("Vector Space Model Analysis");
        VSM_checkBox.setBounds(typeField.getBounds().x + typeField.getBounds().width + 10, typeField.getBounds().y, 200, 30);
        VSM_checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // raise VSM flag
                VSMflag = true;
                search.setWithVSMflag(VSMflag);
            }
        });

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(numResultButtons, 1)); // Set layout as grid with n rows and 1 column
        buttonPanel.setBounds(10, 140, 760, numResultButtons * 80); // Set bounds, adjust as per your requirement
        /*----------------------------  SEARCH OBJECT -----------------------------*/
        int docsNum = countLines("resources/if/DocumentsFile.txt");
        System.out.println("Total Docs"+ docsNum);
        search = new Search(LoadedVocab, "resources/if/PostingFile.txt", VSMflag,docsNum);// Initialize the search object
        /*-------------------------------------------------------------------------*/
        // Create the search button
        searchButton = new JButton("Search");
        searchButton.setBounds(380, 10, 100, 30);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText();
                String type = typeField.getText();
                // Perform search with the query and type
                if(type!=null) {


                    // Display the results in the result area
                    try {
                        int[] results_ofQuery = search.getTotalDf(query);

                        /* e.g        Query = { cancer , book }        */
                        /*-------------------------------------------- */
                        /*  results_ofQuery = [ 18, 2 ]                */
                        /*                                             */
                        /*  allResults  =  18 + 2 =  20                */

                        int allResults = 0;
                        for (int j = 0; j < results_ofQuery.length; j++) {
                            allResults = allResults + results_ofQuery[j];
                        }

                        search.setNumResults(allResults);
                        numResultButtons = search.getNumResults();
                        String result = "Results for query: \t" + query + "\n" + "Type: \t\t" + type + "\n" + "Results: \t\t" + numResultButtons + "\t\t" + search.getSearchTime() + "mils\n";
                        resultArea.setText(result);
                        System.out.println("Number of results: " + numResultButtons);

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {

                        // Call the search method with the query
                        search.search(query,type);
                        numResultButtons = search.getNumResults();
                        // repaint resultarea
                        String result = "Results for query: \t" + query + "\n" + "Type: \t\t" + type + "\n" + "Results: \t\t" + numResultButtons + "\t\t\t\t TIME: " + search.getSearchTime() + " ms\n";
                        resultArea.setText(result);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    if (buttonPanel != null) {
                        frame.remove(buttonPanel);
                    }
                    generateButtons();
                }
            }
        });

        // Create the result area
        resultArea = new JTextArea();
        resultArea.setBounds(10, 85, 760, 55);

        // Add the button panel to the frame
        //frame.add(buttonPanel);
        // Add the components to the frame
        frame.add(queryLabel);
        frame.add(queryField);
        frame.add(typeLabel);
        frame.add(typeField);
        frame.add(searchButton);
        frame.add(VSM_checkBox);
        frame.add(resultArea);

        // Set the layout and make the frame visible
        frame.setLayout(null);
        frame.setVisible(true);
    }


    public static void main(String[] args) throws IOException {
        new QueryGUI();
    }
}
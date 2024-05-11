package GUI;import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class QueryGUI {
    private JFrame frame;
    private JLabel queryLabel;
    private JTextField queryField;
    private JLabel typeLabel;
    private JTextField typeField;
    private JButton searchButton;
    private JTextArea resultArea;

    public QueryGUI() {
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

        // Create the type field
        typeField = new JTextField(20);
        typeField.setBounds(70, 50, 300, 30);

        // Create the search button
        searchButton = new JButton("Search");
        searchButton.setBounds(380, 10, 100, 30);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText();
                String type = typeField.getText();
                // Perform search with the query and type
                String result = "Results for query: \t" + query;
                resultArea.setText(result);
                // Display the results in the result area
            }
        });

        // Create the result area
        resultArea = new JTextArea();
        resultArea.setBounds(10, 90, 760, 45);

        // create area under result area for n buttons
        // create n buttons
        // Define the number of buttons
        int n = 5;

        // Create a JPanel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(n, 1)); // Set layout as grid with n rows and 1 column
        buttonPanel.setBounds(10, 140, 760, n * 80); // Set bounds, adjust as per your requirement

        // Create n buttons and add them to the panel
        for (int i = 0; i < n; i++) {
            // - buttonText -
            /* * FILE PATH ,
             * * SNIPPET ,
             * * SCORE */
            String FILE_PATH = "src/main/java/GUI/QueryGUI.java";
            String SNIPPET = "public class QueryGUI {";
            String SCORE = "0.8";
            String buttonText = "<html><b>" + i + "</b> : <pre>" +
                    FILE_PATH + "<br>" + SNIPPET + "<br>" + SCORE + "</pre></html>";

            JButton button = new JButton(buttonText);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            buttonPanel.add(button);
        }

        // Add the button panel to the frame
        frame.add(buttonPanel);
        // Add the components to the frame
        frame.add(queryLabel);
        frame.add(queryField);
        frame.add(typeLabel);
        frame.add(typeField);
        frame.add(searchButton);
        frame.add(resultArea);

        // Set the layout and make the frame visible
        frame.setLayout(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new QueryGUI();
    }
}
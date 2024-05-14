package GUI;
import static pIndexing.pindexing.compute_occurrences_for_directory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class DirectorySelector extends JFrame {
    private JButton button;
    private JLabel label;
    private JProgressBar progressBar;
    private JButton queryButton; // New button for QueryGUI


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
                new QueryGUI(); // Open the QueryGUI when the button is clicked
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
                            compute_occurrences_for_directory(file.getAbsolutePath());
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
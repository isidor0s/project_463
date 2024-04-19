package GUI;

import xmlReader.folderReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class DirectorySelector extends JFrame {
    private JButton button;
    private JLabel label;

    public DirectorySelector() {
        button = new JButton("Select Directory");
        label = new JLabel();

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(DirectorySelector.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    label.setText("Folder Selected: " + file.getName());
                    try {
                        folderReader.compute_occurrences_for_directory(file.getAbsolutePath());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    label.setText("Open command canceled");
                }
            }
        });

        setLayout(new FlowLayout());
        add(button);
        add(label);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DirectorySelector();
            }
        });
    }
}

package me.ryandw11.odsvisualizer;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * The prompt to ask for a file.
 */
public class LoadFile extends JFrame {
    File file;

    public LoadFile() {
        super("Load file");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 230);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        JPanel pnl = new JPanel();

        JLabel title = new JLabel("Object Data Structure Visualizer", SwingConstants.CENTER);
        title.setFont(new Font("", Font.PLAIN, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnl.add(title);

        JLabel lbl = new JLabel("Please select a file!", SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnl.add(lbl);

        JLabel selectedFile = new JLabel("No selected file.");
        selectedFile.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = new JButton("Select File");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton submit = new JButton("Open File");
        submit.setAlignmentX(Component.CENTER_ALIGNMENT);
        submit.setEnabled(false);

        pnl.setLayout(new BoxLayout(pnl, BoxLayout.PAGE_AXIS));

        btn.addActionListener(e -> {
            FileDialog dialog = new FileDialog((Frame) null, "Select File to Open");
            dialog.setMode(FileDialog.LOAD);
            dialog.setVisible(true);
            File tempFile = new File(dialog.getDirectory() + dialog.getFile());
            if (!tempFile.exists()) {
                selectedFile.setText("No selected file.");
                submit.setEnabled(false);
                return;
            }
            file = tempFile;
            selectedFile.setText("Selected File: " + file.getName());
            submit.setEnabled(true);
            System.out.println("[DEBUG] Selected file: " + file.getName());
        });

        submit.addActionListener(e -> {
            if (file == null || !file.exists()) {
                JOptionPane.showMessageDialog(null, "No file was selected or the file was not found!", "File Not Found", JOptionPane.ERROR_MESSAGE);
            }

            dispose();
            new ViewFile(file).setVisible(true);
        });

        pnl.add(btn);
        pnl.add(selectedFile);
        this.add(pnl, BorderLayout.CENTER);
        this.add(submit, BorderLayout.SOUTH);
    }
}

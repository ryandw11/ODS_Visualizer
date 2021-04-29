package me.ryandw11.odsvisualizer;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Allows the user to use command arguments for the file.
        if (args.length >= 1) {
            for (String arg : args) {
                File file = new File(arg);
                if (!file.exists()) {
                    System.err.printf("Unable to locate: %s%n", arg);
                }
                new ViewFile(file).setVisible(true);
            }
        } else {
            new LoadFile().setVisible(true);
        }
    }
}

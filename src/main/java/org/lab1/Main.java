package org.lab1;

import javax.swing.SwingUtilities;

/**
 * The Main class is the entry point of the application.
 */
public class Main {
    /**
     * The main method to start the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphGui();
            }
        });
    }
}

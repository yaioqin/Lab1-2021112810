package org.Lab1;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphGUI();
            }
        });
    }
    public void fun4(){
        System.out.println("hello Lab1");
    }
    public void fun6(){
        System.out.println("hello Lab1");
    }

}
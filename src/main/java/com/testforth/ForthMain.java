package com.testforth;

import com.testforth.terminal.TerminalWindow;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

public class ForthMain {

    static TerminalWindow tw;

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {

        SwingUtilities.invokeAndWait(() -> {
            tw = new TerminalWindow();
            tw.setVisible(true);
        });

        Forth forth = new Forth(tw);
        try {
            forth.run();
        } finally {
            tw.close();
        }
    }
}

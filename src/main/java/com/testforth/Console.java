package com.testforth;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import java.awt.Font;
import java.io.IOException;

public class Console {

    private final Terminal terminal;

    public Console() throws IOException {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        defaultTerminalFactory.setTerminalEmulatorFontConfiguration(new SwingTerminalFontConfiguration(true, AWTTerminalFontConfiguration.BoldMode.EVERYTHING, new Font("Courier New", Font.BOLD, 14)));
        terminal = defaultTerminalFactory.createTerminal();
        
    }

    public String readLine() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = readChar();
            putChar(c);
            if (c == '\n') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public void setXY(int x, int y) {
        try {
            terminal.setCursorPosition(new TerminalPosition(x, y));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void moveXY(int x, int y) {
        try {
            terminal.setCursorPosition(terminal.getCursorPosition().withRelative(x, y));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCursorX() {
        try {
            return terminal.getCursorPosition().getColumn();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getCursorY() {
        try {
            return terminal.getCursorPosition().getRow();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public char readChar() {
        try {
            while (true) {
                KeyStroke ks = terminal.readInput();
                if (ks.getCharacter() != null) {
                    char c = ks.getCharacter();
                    if (c != '\b') {
                        return c;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void putChar(char c) {
        try {
            terminal.putCharacter(c);
            terminal.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void putString(String str) {
        try {
            for (int i = 0; i < str.length(); i++) {
                terminal.putCharacter(str.charAt(i));
            }
            
            terminal.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        try {
            terminal.clearScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            terminal.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

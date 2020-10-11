package com.testforth.line;

import com.testforth.terminal.TerminalWindow;
import static com.testforth.terminal.TerminalWindow.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class ReadLine2 {

    private byte TEXT_ATTRIBUTE = (byte) 0xF0;
    private final TerminalWindow terminal;
    private byte[] prefix;
    private int prefixLength;
    private List<Line> lines = new ArrayList<>();
    private int screenBufferWidth = 80;
    private int textFieldWidth = screenBufferWidth;
    private int textFieldHeight = 25;
    private List<String> initialStrings;
    private int verticalScroll;

    public ReadLine2(TerminalWindow terminal) {
        this.terminal = terminal;
    }

    public String getLine() {
        copyPrefix();
        verticalScroll = -terminal.getCursorY();
        if (initialStrings == null) {
            lines.add(new Line());
        } else {
            for (String line : initialStrings) {
                appendString(line);
            }
        }

        paint();
        while (terminal.isVisible()) {
            int key = terminal.waitKey();
            if (isPrintableChar(key)) {
                char c = convertKeyToCharWithModifiers(key);
                typedVisibleChar(c);
            } else {
                pressedNonSymbolKey(key);
            }

            paint();
        }

        return "";
    }

    public void paint() {

    }

    public void loadLines(List<String> strings) {
        initialStrings = strings;
    }

    private void appendString(String string) {
        Line line = new Line();
        line.line.append(string);
        lines.add(line);
    }

    private void typedVisibleChar(char c) {

    }

    private void pressedNonSymbolKey(int key) {
        if (key == TerminalWindow.KEY_LEFT) {
            moveCursorBackward();
        }
        if (key == TerminalWindow.KEY_RIGHT) {
            moveCursorForward();
        }
        if (key == TerminalWindow.KEY_DOWN) {
            moveCursorDown();
        }
        if (key == TerminalWindow.KEY_ENTER) {
            newLine();
        }
    }

    public void newLine() {

    }

    private void printPrefix(int y) {
        int pos = 0;
        for (int i = 0; i < prefixLength; i++) {
            byte c = prefix[pos];
            pos++;
            byte attribute = prefix[pos];
            pos++;
            terminal.putChar(c, attribute, i, y);
        }
    }

    private void copyPrefix() {
        prefix = terminal.readBuffer(0, terminal.getCursorY(), terminal.getCursorX());
        prefixLength = terminal.getCursorX();
    }

    private void moveCursorForward() {

    }

    private void moveCursorBackward() {

    }

    private void moveCursorDown() {

    }

    public void setCursorPosition(int lineColumn, int lineIndex) {
        int realCursorX = lineColumn;
        if (lineIndex == 0) {
            realCursorX += prefixLength;
        }

        int lastCurrentLineIndex = getCursorLineIndex();
        if (lineIndex > lastCurrentLineIndex) {
            
        } else {
            
        }
        terminal.setCursorXY(realCursorX, ERROR);
    }

    private int getCursorLineIndex() {
        return terminal.getCursorY() + verticalScroll;
    }

    private int getCursorLineColumn() {
        if (getCursorLineIndex() == 0) {
            return terminal.getCursorX() - prefixLength;
        }

        return terminal.getCursorX();
    }

    private int getScreenLineLength(int lineIndex) {
        if (lineIndex == 0) {
            return textFieldWidth - prefixLength;
        }

        return textFieldWidth;
    }

    private int getLineLength(int lineIndex) {
        return lines.get(lineIndex).line.length();
    }

    private char convertKeyToCharWithModifiers(int key) {
        String chars = keyToChars(key);
        if (terminal.isShiftPressed()) {
            return chars.charAt(1);
        }
        return chars.charAt(0);
    }

    private boolean isPrintableChar(int keyCode) {
        return keyCode >= KEY_SPACE && keyCode <= KEY_Z;
    }

    private String keyToChars(int key) {
        switch (key) {
            case KEY_SPACE:
                return "  ";
            case KEY_BACK_QUOTE:
                return "`~";
            case KEY_MINUS:
                return "-_";
            case KEY_EQUALS:
                return "=+";
            case KEY_QUOTE:
                return "'\"";
            case KEY_SEMICOLON:
                return ";:";
            case KEY_OPEN_BRACKET:
                return "[{";
            case KEY_CLOSE_BRACKET:
                return "]}";
            case KEY_BACK_SLASH:
                return "\\?";
            case KEY_SLASH:
                return "/|";
            case KEY_COMMA:
                return ",<";
            case KEY_PERIOD:
                return ".>";
            case KEY_0:
                return "0)";
            case KEY_1:
                return "1!";
            case KEY_2:
                return "2@";
            case KEY_3:
                return "3#";
            case KEY_4:
                return "4$";
            case KEY_5:
                return "5%";
            case KEY_6:
                return "6^";
            case KEY_7:
                return "7&";
            case KEY_8:
                return "8*";
            case KEY_9:
                return "9(";
            case KEY_A:
                return "aA";
            case KEY_B:
                return "bB";
            case KEY_C:
                return "cC";
            case KEY_D:
                return "dD";
            case KEY_E:
                return "eE";
            case KEY_F:
                return "fF";
            case KEY_G:
                return "gG";
            case KEY_H:
                return "hH";
            case KEY_I:
                return "iI";
            case KEY_J:
                return "jJ";
            case KEY_K:
                return "kK";
            case KEY_L:
                return "lL";
            case KEY_M:
                return "mM";
            case KEY_N:
                return "nN";
            case KEY_O:
                return "oO";
            case KEY_P:
                return "pP";
            case KEY_Q:
                return "qQ";
            case KEY_R:
                return "rR";
            case KEY_S:
                return "sS";
            case KEY_T:
                return "tT";
            case KEY_U:
                return "uU";
            case KEY_V:
                return "vV";
            case KEY_W:
                return "wW";
            case KEY_X:
                return "xX";
            case KEY_Y:
                return "yY";
            case KEY_Z:
                return "zZ";
        }
        return "☺☺";
    }
}

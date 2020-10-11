package com.testforth.line;

import com.testforth.terminal.TerminalWindow;
import static com.testforth.terminal.TerminalWindow.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class ReadLine {
/*
    private final TerminalWindow terminal;
    private byte[] prefix;
    private int prefixLength;
    private List<Line> lines = new ArrayList<>();
    private int verticalScroll = 0;
    private byte DEFAULT_ATTRIBUTE = (byte) 0xF0;
    private int screenBufferWidth = 80;
    private int textFieldWidth = 79;
    private List<String> initialStrings;

    public ReadLine(TerminalWindow terminal) {
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
    
    public void loadLines(List<String>strings){
        initialStrings=strings;
    }

    private void appendString(String string) {
        Line line = new Line();
        line.line.append(string);
        line.needRedraw = true;
        lines.add(line);
    }

    private void typedVisibleChar(char c) {
        int cursorX = terminal.getCursorX();
        int cursorY = terminal.getCursorY();

        int y = screenSpaceYToLineIndex(cursorY);
        int x = screenSpaceXYToLineX(cursorX, cursorY);
        Line line = lines.get(y);
        line.line.insert(x, c);
        line.needRedraw = true;
        ensureLineWrappedIfNecessary(y);
        invalidateLine(y);
        moveCursorForward();
        if (cursorX == textFieldWidth) {
            moveCursorForward();
        }
    }

    private void ensureLineWrappedIfNecessary(int lineIndex) {
        Line line = lines.get(lineIndex);
        int lineLength = line.line.length();
        int maxLineLength = (lineIndex == 0) ? textFieldWidth - prefixLength : textFieldWidth;
        int remainder = maxLineLength - lineLength;

        if (remainder < 0) {//string go out of screen. We should do wrap
            int remLength = -remainder;
            String remainderString = line.line.substring(maxLineLength);
            line.line.delete(maxLineLength, maxLineLength + remLength);
            if (!isLineSoftWrapContinuation(lineIndex + 1)) {
                Line newSoftWrappedLine = new Line();
                newSoftWrappedLine.softWrapContinuation = true;
                newSoftWrappedLine.line.append(remainderString);
                newSoftWrappedLine.needRedraw = true;
                lines.add(lineIndex + 1, newSoftWrappedLine);
            } else {
                Line softWrappedLine = lines.get(lineIndex + 1);
                softWrappedLine.line.insert(0, remainderString);
                softWrappedLine.needRedraw = true;
                invalidatePaintAllLines();
            }

            ensureLineWrappedIfNecessary(lineIndex + 1);
        } else if (remainder > 0) {//if next line is wrapped line, we should ajdust it properly
            if (isLineSoftWrapContinuation(lineIndex + 1)) {
                Line wrappedLine = lines.remove(lineIndex + 1);
                line.line.append(wrappedLine.line);
                line.needRedraw = true;
                ensureLineWrappedIfNecessary(lineIndex);
                invalidatePaintAllLines();
            }
        }
    }

    private void invalidatePaintAllLines() {
        for (int i = 0; i < lines.size(); i++) {
            invalidateLine(i);
        }
    }

    private void invalidateLine(int lineIndex) {
        Line line = lines.get(lineIndex);
        line.needRedraw = true;
    }

    private void paintLine(int lineIndex) {
        int screenY = lineIndexToScreenSpaceY(lineIndex);
        if (screenY > 24 || screenY < 0) {
            return;
        }
        Line line = lines.get(lineIndex);
        if (line.needRedraw == false) {
            return;
        }
        line.needRedraw = false;
        int x = 0;
        if (lineIndex == 0) {
            printPrefix(screenY);
            x = prefixLength;
        }
        int charsCount = line.line.length();
        for (int i = 0; i < charsCount; i++, x++) {
            char c = line.line.charAt(i);
            terminal.putChar((byte) c, DEFAULT_ATTRIBUTE, x, screenY);
        }

        //clear text until end of the line
        for (int i = x; i < textFieldWidth; i++) {
            terminal.putChar((byte) ' ', DEFAULT_ATTRIBUTE, i, screenY);
        }
    }

    private void paint() {
        int firstLineY = lineIndexToScreenSpaceY(0);
        int startLineIndex;
        if (firstLineY >= 0) {
            startLineIndex = 0;
        } else {
            startLineIndex = 0 - firstLineY;
        }

        int startLineIndexY = lineIndexToScreenSpaceY(startLineIndex);
        int maxRemainsScreenHeight = 24 - startLineIndexY;
        int maxIterations = lines.size() - startLineIndex;
        if (maxIterations > maxRemainsScreenHeight) {
            maxIterations = maxRemainsScreenHeight;
        }
        for (int i = startLineIndex; i < maxIterations; i++) {
            paintLine(i);
        }
    }

    private boolean isLineSoftWrapContinuation(int line) {
        if (line >= lines.size()) {
            return false;
        }
        return lines.get(line).softWrapContinuation;
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

    private void newLine() {
        int cursorX = terminal.getCursorX();
        int cursorY = terminal.getCursorY();

        int lineIndex = screenSpaceYToLineIndex(cursorY);
        int x = screenSpaceXYToLineX(cursorX, cursorY);
        Line line = lines.get(lineIndex);
        String remainderString = line.line.substring(x);
        line.line.delete(x, line.line.length());
        line.needRedraw = true;

        Line newLine = new Line();
        newLine.line.append(remainderString);
        newLine.needRedraw = true;
        lines.add(lineIndex + 1, newLine);
        ensureLineWrappedIfNecessary(lineIndex + 1);
        invalidatePaintAllLines();
    }

    private int lineIndexToScreenSpaceY(int lineIndex) {
        return lineIndex - verticalScroll;
    }
    
    private int screenSpaceYToLineIndex(int y) {
        int lineIndex = y + verticalScroll;
        return lineIndex;
    }

    private int screenSpaceXYToLineX(int x, int y) {
        int lineIndex = screenSpaceYToLineIndex(y);
        if (lineIndex == 0) {
            return x - prefixLength;
        }
        return x;
    }

    private int lineXToScreenSpaceX(int lineX, int lineIndex) {
        if (lineIndex == 0) {
            return prefixLength+lineX;
        }
        return lineX;
    }

    private void printPrefix(int y) {
        for (int i = 0; i < prefixLength; i++) {
            byte c = prefix[i * 2];
            byte attribute = prefix[i * 2 + 1];
            terminal.putChar(c, attribute, i, y);
        }
    }

    private void copyPrefix() {
        int bufferLineStartPosition = terminal.getCursorY() * screenBufferWidth * 2;
        prefixLength = terminal.getCursorX();
        int prefixBytesLength = prefixLength * 2;
        prefix = new byte[prefixBytesLength];
        for (int i = 0; i < prefixBytesLength; i++) {
            prefix[i] = terminal.readFromBuffer(bufferLineStartPosition + i);
        }
    }

    private void moveCursorForward() {
        int lineIndex=screenSpaceYToLineIndex(terminal.getCursorY());
        int maxLineScreenX=lineXToScreenSpaceX(lines.get(lineIndex).line.length(), lineIndex);
        
        System.out.println("maxLineScreenX:"+maxLineScreenX+" cursorX:"+terminal.getCursorX());
        int pos = terminal.getCursorY() * screenBufferWidth + terminal.getCursorX();
        pos++;
        int y = pos / screenBufferWidth;
        int x = pos - y * screenBufferWidth;
        terminal.setCursorXY(x, y);
    }

    private void moveCursorBackward() {
        int pos = terminal.getCursorY() * screenBufferWidth + terminal.getCursorX();
        pos--;
        int y = pos / screenBufferWidth;
        int x = pos - y * screenBufferWidth;
        terminal.setCursorXY(x, y);
    }

    private void moveCursorDown() {

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
    }*/
}

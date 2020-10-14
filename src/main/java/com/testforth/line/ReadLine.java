package com.testforth.line;

import com.testforth.terminal.TerminalWindow;
import static com.testforth.terminal.TerminalWindow.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class ReadLine {

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
    private int countOfRemovedLines = 0;
    private boolean completed = false;
    private boolean skipPaint = false;

    public ReadLine(TerminalWindow terminal) {
        this.terminal = terminal;
    }

    public String readLine() {
        skipPaint = false;
        completed = false;
        lines.clear();
        countOfRemovedLines = 0;
        copyPrefix();
        verticalScroll = terminal.getCursorY();
        if (initialStrings == null) {
            lines.add(new Line());
        } else {
            for (String line : initialStrings) {
                appendString(line);
            }
            initialStrings = null;
        }

        initialStrings = null;
        paint();
        while (terminal.isVisible() && completed == false) {
            int key = terminal.waitKey();
            if (isPrintableChar(key)) {
                char c = convertKeyToCharWithModifiers(key);
                typedVisibleChar(c);
            } else {
                pressedNonSymbolKey(key);
            }

            if (skipPaint == false) {
                paint();
            }
            skipPaint = false;
        }
        terminal.ensureNewLine();
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Line line : lines) {
            if (first == false) {
                sb.append("\n");
            }
            first = false;
            sb.append(line.line);
        }
        return sb.toString();
    }

    public void loadLines(List<String> strings) {
        initialStrings = strings;
    }

    private void appendString(String string) {
        Line line = new Line();
        line.line.append(string);
        lines.add(line);
    }

    private void paint() {
        int firstVisibleLineIndex = getFirstVisibleLineIndex();
        int lastVisibleLineIndex = getLastVisibleLineIndex();
        if (firstVisibleLineIndex == -1) {
            return;
        }

        int lineY = lineIndexToScreenY(firstVisibleLineIndex);
        for (int i = firstVisibleLineIndex; i <= lastVisibleLineIndex; i++, lineY++) {
            Line line = lines.get(i);
            int xOffset = 0;
            if (i == 0) {
                xOffset += prefixLength;
                printChars(prefix, 0, lineY);
            }
            printString(line, xOffset, lineY);
            xOffset += line.size();
            int remainingLength = textFieldWidth - xOffset;
            terminal.putChars(' ', TEXT_ATTRIBUTE, xOffset, lineY, remainingLength);
        }

        //if some line was removed, we should erace remainigs from screen
        if (countOfRemovedLines > 0) {
            int lastLineY = lineIndexToScreenY(lines.size() - 1);
            if (lastLineY >= 0 && lastLineY <= textFieldHeight) {
                int redrawY = lastLineY + 1;
                int countOfLinesToRedraw = textFieldHeight - redrawY;
                for (int i = 0; i < countOfLinesToRedraw; i++, redrawY++) {
                    terminal.putChars(' ', TEXT_ATTRIBUTE, 0, redrawY, textFieldWidth);
                }
            }
        }

        countOfRemovedLines = 0;
    }

    private int getFirstVisibleLineIndex() {
        int firstVisibleLineIndex = 0;
        if (verticalScroll < 0) {
            firstVisibleLineIndex = -verticalScroll;
        }
        if (firstVisibleLineIndex >= lines.size()) {
            return -1;
        }
        int lineRow = lineIndexToScreenY(firstVisibleLineIndex);
        if (lineRow >= textFieldHeight) {
            return -1;
        }
        return firstVisibleLineIndex;
    }

    private int getLastVisibleLineIndex() {
        int firstVisibleLineIndex = getFirstVisibleLineIndex();
        if (firstVisibleLineIndex == -1) {
            return -1;
        }

        int y = lineIndexToScreenY(firstVisibleLineIndex);
        int linesCount = textFieldHeight - y;
        int lastLine = firstVisibleLineIndex + linesCount - 1;
        if (lastLine > lines.size() - 1) {
            lastLine = lines.size() - 1;
        }
        return lastLine;
    }

    private void typedVisibleChar(char c) {
        int lineIndex = getCursorLineIndex();
        int lineColumn = getCursorLineColumn();
        Line line = lines.get(lineIndex);
        line.line.insert(lineColumn, c);
        ensureLineIsProperlyWrapped(lineIndex);
        if (lineColumn == getScreenLineLength(lineIndex) - 1) {
            moveCursorForward();
        }

        moveCursorForward();
    }

    private void enterKey() {
        int lineColumn = getCursorLineColumn();
        int lineIndex = getCursorLineIndex();
        Line line = lines.get(lineIndex);
        String part = line.line.substring(lineColumn, line.size());
        line.line.delete(lineColumn, line.size());
        Line newLine = new Line(false, part);
        lines.add(lineIndex + 1, newLine);
        countOfRemovedLines--;
        setCursorPosition(0, lineIndex + 1);
    }

    private void backspaceKey() {
        int lineIndex = getCursorLineIndex();
        int lineColumn = getCursorLineColumn();
        if (lineIndex == 0 && lineColumn == 0) {
            return;
        }

        if (lineColumn == 0) {
            //removing from 0 cursor position, meaning that we should append this line to previous, and remove this line
            Line currentLine = getLineByIndex(lineIndex);
            Line previousLine = getLineByIndex(lineIndex - 1);
            setCursorPosition(previousLine.size(), lineIndex - 1);
            previousLine.line.append(currentLine.line);
            lines.remove(lineIndex);
            countOfRemovedLines++;
            ensureLineIsProperlyWrapped(lineIndex - 1);
        } else {
            //removing from somewhere inside line
            Line line = lines.get(lineIndex);
            line.line.delete(lineColumn - 1, lineColumn);
            ensureLineIsProperlyWrapped(lineIndex);
            moveCursorBackward();
        }
    }

    private void ensureLineIsProperlyWrapped(int lineIndex) {
        Line line = getLineByIndex(lineIndex);
        int length = getLineLength(lineIndex);
        int maxScreenLineLength = getScreenLineLength(lineIndex);
        if (length < maxScreenLineLength - 1) {
            if (isLineIsSoftWrapLine(lineIndex + 1)) {
                // If we the next line is soft wrap, but current line is not so big. We should cut some part from soft wrap.
                // If remaining from soft wrap becomes 0 - remove soft wrap line
                int diff = maxScreenLineLength - 1 - length;
                Line softWrapLine = getLineByIndex(lineIndex + 1);
                if (diff > softWrapLine.size()) {
                    diff = softWrapLine.size();
                }

                String part = softWrapLine.line.substring(0, diff);
                softWrapLine.line.delete(0, diff);
                line.line.append(part);
                if (softWrapLine.size() == 0) {
                    lines.remove(lineIndex + 1);
                    countOfRemovedLines++;
                } else {
                    ensureLineIsProperlyWrapped(lineIndex + 1);
                }
            }
        } else {
            //Wrap long line on next line
            String remainingPart = line.line.substring(maxScreenLineLength - 1);
            line.line.delete(maxScreenLineLength - 1, line.line.length());
            if (isLineIsSoftWrapLine(lineIndex + 1)) {
                Line softWrapLine = getLineByIndex(lineIndex + 1);
                softWrapLine.line.insert(0, remainingPart);
            } else {
                lines.add(lineIndex + 1, new Line(true, remainingPart));
                countOfRemovedLines--;
            }
            ensureLineIsProperlyWrapped(lineIndex + 1);
        }
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
        if (key == TerminalWindow.KEY_UP) {
            moveCursorUp();
        }
        if (key == TerminalWindow.KEY_HOME) {
            moveCursorHome();
        }
        if (key == TerminalWindow.KEY_END) {
            moveCursorEnd();
        }
        if (key == TerminalWindow.KEY_F1) {
            terminal.moveLines(0, 5, 1);
            skipPaint = true;
        }
        if (key == TerminalWindow.KEY_F2) {
            terminal.moveLines(0, 5, -1);
            skipPaint = true;
        }
        if (key == TerminalWindow.KEY_ENTER) {
            if (terminal.isCtrlPressed()) {
                completed = true;
                int lastLineIndex = lines.size() - 1;
                setCursorPosition(getLineLength(lastLineIndex), lastLineIndex);
            } else {
                enterKey();
            }
        }
        if (key == TerminalWindow.KEY_BACK_SPACE) {
            backspaceKey();
        }
    }

    private void copyPrefix() {
        prefix = terminal.readBuffer(0, terminal.getCursorY(), terminal.getCursorX());
        prefixLength = terminal.getCursorX();
    }

    private void moveCursorForward() {
        int lineIndex = getCursorLineIndex();
        int lineColumn = getCursorLineColumn();
        if (isEndOfLinePosition(lineColumn, lineIndex) && !isLastLine(lineIndex)) {
            setCursorPosition(0, lineIndex + 1);
        } else {
            setCursorPosition(lineColumn + 1, lineIndex);
        }
    }

    private void moveCursorBackward() {
        int lineIndex = getCursorLineIndex();
        int lineColumn = getCursorLineColumn();
        if (lineColumn == 0 && lineIndex != 0) {
            setCursorPosition(getLineLength(lineIndex - 1), lineIndex - 1);
        } else {
            setCursorPosition(lineColumn - 1, lineIndex);
        }
    }

    private void moveCursorDown() {
        int lineIndex = getCursorLineIndex();
        int lineColumn = getCursorLineColumn();
        setCursorPosition(lineColumn, lineIndex + 1);
    }

    private void moveCursorUp() {
        int lineIndex = getCursorLineIndex();
        int lineColumn = getCursorLineColumn();
        setCursorPosition(lineColumn, lineIndex - 1);
    }

    private void moveCursorHome() {
        int lineIndex = getCursorLineIndex();
        setCursorPosition(0, lineIndex);
    }

    private void moveCursorEnd() {
        int lineIndex = getCursorLineIndex();
        setCursorPosition(getLineLength(lineIndex), lineIndex);
    }

    private boolean isEndOfLinePosition(int lineColumn, int lineIndex) {
        int lineLength = getLineLength(lineIndex);
        return lineColumn >= lineLength;
    }

    private boolean isLineIsSoftWrapLine(int lineIndex) {
        Line line = getLineByIndex(lineIndex);
        return line != null && line.softWrapContinuation == true;
    }

    private Line getLineByIndex(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            return null;
        }

        return lines.get(lineIndex);
    }

    public void setCursorPosition(int lineColumn, int lineIndex) {
        if (lineIndex < 0) {
            lineIndex = 0;
        }
        if (lineIndex >= lines.size()) {
            lineIndex = lines.size() - 1;
        }
        if (lineColumn < 0) {
            lineColumn = 0;
        }
        int lineLength = getLineLength(lineIndex);
        if (lineColumn > lineLength) {
            lineColumn = lineLength;
        }

        int firstVisibleLineIndex = getFirstVisibleLineIndex();
        int lastVisibleLineIndex = getLastVisibleLineIndex();
        if (firstVisibleLineIndex == -1) {
            return;
        }

        int cy;
        //if desired line is higher than scrolling window
        if (lineIndex < firstVisibleLineIndex) {
            // scroll down
            int difference = firstVisibleLineIndex - lineIndex;
            verticalScroll += difference;
            cy = 0;
        } else if (lineIndex > lastVisibleLineIndex) {
            // scroll up
            int difference = lineIndex - lastVisibleLineIndex;
            verticalScroll -= difference;
            scrollTopPartOfTextField(-difference);
            cy = textFieldHeight - 1;
        } else {
            cy = lineIndexToScreenY(lineIndex);
        }

        int cx = (lineIndex == 0) ? lineColumn + prefixLength : lineColumn;
        terminal.setCursorXY(cx, cy);
    }

    /**
     * Function scrolls the lines that was there before ReadLine was executed
     */
    private void scrollTopPartOfTextField(int distance) {
        int firstLineY = lineIndexToScreenY(0);
        int lineToScroll = firstLineY;
        if (lineToScroll > 0 && lineToScroll <= textFieldHeight) {
            terminal.moveLines(0, lineToScroll, distance);
        }
    }

    private int getCursorLineIndex() {
        return terminal.getCursorY() - verticalScroll;
    }

    private int getCursorLineColumn() {
        if (getCursorLineIndex() == 0) {
            return terminal.getCursorX() - prefixLength;
        }

        return terminal.getCursorX();
    }

    private int lineIndexToScreenY(int lineIndex) {
        return lineIndex + verticalScroll;
    }

    private int getLineLength(int lineIndex) {
        return lines.get(lineIndex).size();
    }

    private int getScreenLineLength(int lineIndex) {
        int length = textFieldWidth;
        if (lineIndex == 0) {
            length -= prefixLength;
        }

        return length;
    }

    private boolean isLastLine(int lineIndex) {
        return (lines.size() - 1) == lineIndex;
    }

    private void printChars(byte[] buffer, int x, int y) {
        for (int i = 0; i < buffer.length; i += 2, x++) {
            byte c = buffer[i];
            byte attribute = buffer[i + 1];
            terminal.putChar(c, attribute, x, y);
        }
    }

    private void printString(Line line, int x, int y) {
        for (int i = 0; i < line.size(); i++) {
            terminal.putChar((byte) line.line.charAt(i), TEXT_ATTRIBUTE, x + i, y);
        }
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

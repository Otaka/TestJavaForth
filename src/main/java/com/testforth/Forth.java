package com.testforth;

import com.testforth.line.ReadLine;
import com.testforth.line.ReadLine2;
import com.testforth.terminal.TerminalWindow;
import com.testforth.words.AbstractWord;
import com.testforth.words.BundledWords;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry
 */
public class Forth {

    private final TerminalWindow console;
    private final List<Object> stack;
    private final Map<String, AbstractWord> dictionary = new HashMap<>();

    public Forth(TerminalWindow console) {
        this.console = console;
        console.setCursorXY(0, 5);
        stack = new ArrayList<>();
        fillBundledWords();
    }

    private void fillBundledWords() {
        for (var innerClass : BundledWords.class.getDeclaredClasses()) {
            var constructor = getDefaultConstructor(innerClass);
            if (constructor != null) {
                try {
                    var word = (AbstractWord) constructor.newInstance();
                    if (word.getName() != null) {
                        dictionary.put(word.getName(), word);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private Constructor getDefaultConstructor(Class clazz) {
        try {
            return clazz.getConstructor();
        } catch (NoSuchMethodException | SecurityException ex) {
            return null;
        }
    }

    public TerminalWindow getConsole() {
        return console;
    }

    public Object pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot pop from empty stack");
        }
        return stack.remove(stack.size() - 1);
    }

    public void push(Object o) {
        stack.add(o);
    }

    private boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isString(String str) {
        return false;
    }

    private void processLine(String line) {
        var parts = line.split("\\s+");
        for (var str : parts) {
            if (isNumber(str)) {
                var intWord = new BundledWords.PushIntWord(Integer.parseInt(str));
                intWord.execute(this);
            } else if (isString(str)) {
                var stringWord = new BundledWords.PushStringWord(str);
                stringWord.execute(this);
            } else {
                AbstractWord word = dictionary.get(str);
                if (word == null) {
                    console.putString("Word [" + str + "] is not found");
                    return;
                }
                word.execute(this);
            }
        }
    }

    private void ensureNewLine() {
        if (console.getCursorX() != 0) {
            console.putChar('\n');
        }
    }

    public void run() {
        while (true) {
            try {
                ensureNewLine();
                console.putString("----->");
                ReadLine2 readLine = new ReadLine2(console);
                readLine.loadLines(Arrays.asList(
                        "He has starred in numerous other projects. His television series include",
                        "fortunes of War for which he won an International Emmy Award.",
                        "Branagh directed and starred in the romantic thriller Dead Again"));
                var line = readLine.getLine();
                if (line.equalsIgnoreCase("exit")) {
                    break;
                }
                if (!line.isBlank()) {
                    processLine(line);
                }
            } catch (Exception ex) {
                ensureNewLine();
                ex.printStackTrace();
                console.putString("ERROR: " + ex.getMessage());
            }
        }
    }
}

package com.testforth.line;

/**
 * @author Dmitry
 */
public class Line {

    public StringBuilder line = new StringBuilder();
    public boolean softWrapContinuation = false;

    public Line() {
    }

    public Line(boolean softWrap, String text) {
        softWrapContinuation = softWrap;
        line.append(text);
    }
    
    public int size(){
        return line.length();
    }

    @Override
    public String toString() {
        String result;
        if (softWrapContinuation) {
            result = "~~\"" + line.toString() + '"';
        } else {
            result = '"' + line.toString() + '"';
        }
        return "[Size=" + line.toString().length() + "]" + result;
    }

}

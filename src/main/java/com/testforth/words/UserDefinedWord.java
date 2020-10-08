package com.testforth.words;

import com.testforth.Forth;

/**
 * @author Dmitry
 */
public class UserDefinedWord extends AbstractWord {

    private AbstractWord words[];

    @Override
    public void execute(Forth forth) {
        for (int i = 0; i < words.length; i++) {
            words[i].execute(forth);
        }
    }
}

package com.testforth.words;

import com.testforth.Forth;

/**
 * @author Dmitry
 */
public abstract class AbstractWord {
    private final String name;

    public AbstractWord() {
        this.name = null;
    }

    public AbstractWord(String name) {
        this.name = name;
    }
    
    public abstract void execute(Forth forth);
    public String getName(){
        return name;
    }
}

package com.testforth.words;

import com.testforth.Forth;

/**
 * @author Dmitry
 */
public class BundledWords {

    public static class PushIntWord extends AbstractWord {

        private final int value;

        public PushIntWord(int value) {
            this.value = value;
        }

        @Override
        public void execute(Forth forth) {
            forth.push(value);
        }
    }

    public static class PushStringWord extends AbstractWord {

        private final String value;

        public PushStringWord(String value) {
            this.value = value;
        }

        @Override
        public void execute(Forth forth) {
            forth.push(value);
        }
    }

    public static class AddWord extends AbstractWord {

        public AddWord() {
            super("+");
        }

        @Override
        public void execute(Forth forth) {
            Object o1 = forth.pop();
            Object o2 = forth.pop();
            if (!(o1 instanceof Integer) && !(o2 instanceof Integer)) {
                throw new IllegalStateException("Cannot add [" + o1.getClass().getSimpleName() + "] to [" + o2.getClass().getSimpleName() + "]");
            }
            forth.push((Integer) o1 + (Integer) o2);
        }
    }

    public static class SubWord extends AbstractWord {

        public SubWord() {
            super("-");
        }

        @Override
        public void execute(Forth forth) {
            Object o1 = forth.pop();
            Object o2 = forth.pop();
            if (!(o1 instanceof Integer) && !(o2 instanceof Integer)) {
                throw new IllegalStateException("Cannot subtract [" + o1.getClass().getSimpleName() + "] and [" + o2.getClass().getSimpleName() + "]");
            }
            forth.push(o1.toString() + o2.toString());
        }
    }

    public static class PrintWord extends AbstractWord {

        public PrintWord() {
            super(".");
        }

        @Override
        public void execute(Forth forth) {
            Object o2 = forth.pop();
            forth.getConsole().printString(o2.toString());
        }
    }
}

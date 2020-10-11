package com.testforth.terminal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author Dmitry
 */
public class TerminalWindow extends JFrame {

    private final int charWidth;
    private final int charHeight;
    private final AtomicInteger dirty = new AtomicInteger(0);
    private Timer timer;
    private final JPanel drawPanel;
    private int cursorX = 0;
    private int cursorY = 0;
    private BlockingQueue<Integer> keysQueue = new ArrayBlockingQueue<>(20);
    
    private final byte[] buffer = new byte[80 * 25 * 2];
    private boolean shiftPressed = false;
    private boolean altPressed = false;
    private boolean ctrlPressed = false;

    private final Color[] backgroundColors = new Color[]{
        new Color(0, 0, 0),
        new Color(0, 0, 255),
        new Color(0, 255, 0),
        new Color(0, 255, 255),
        new Color(255, 0, 0),
        new Color(255, 0, 255),
        new Color(255, 255, 0),
        new Color(255, 255, 255)
    };

    private final Color[] foregroundColors = new Color[]{
        new Color(0, 0, 0),
        new Color(0, 0, 127),
        new Color(0, 127, 0),
        new Color(0, 127, 127),
        new Color(127, 0, 0),
        new Color(127, 0, 127),
        new Color(127, 127, 0),
        new Color(127, 127, 127),
        new Color(200, 200, 200),
        new Color(0, 0, 255),
        new Color(0, 255, 0),
        new Color(0, 255, 255),
        new Color(255, 0, 0),
        new Color(255, 0, 255),
        new Color(255, 255, 0),
        new Color(255, 255, 255),};

    public TerminalWindow() throws HeadlessException {
        setTitle("Terminal");
        //fill attributes
        for (int i = 0; i < 80 * 25; i++) {
            buffer[i * 2 + 1] = (byte) 0xF0;
        }

        dirty.set(1);
        drawPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2d = (Graphics2D) g;
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHints(rh);
                char charBuffer[] = new char[1];
                int byteBufferPosition = 0;
                for (int j = 0; j < 25; j++) {
                    for (int i = 0; i < 80; i++) {
                        byteBufferPosition++;
                        byte attributeByte = buffer[byteBufferPosition];
                        byteBufferPosition++;
                        int backgroundColorIndex = (attributeByte & 0x0E) >> 1;

                        g2d.setColor(backgroundColors[backgroundColorIndex]);
                        g2d.fillRect(i * charWidth, j * charHeight, charWidth, charHeight);
                    }
                }
                byteBufferPosition = 0;
                for (int j = 0; j < 25; j++) {
                    for (int i = 0; i < 80; i++) {
                        byte characterByte = buffer[byteBufferPosition];
                        byteBufferPosition++;
                        byte attributeByte = buffer[byteBufferPosition];
                        byteBufferPosition++;
                        char resultChar = (char) ((attributeByte & 0b1) << 8 | (characterByte & 0xFF));
                        int foregroundColorIndex = (attributeByte & 0xFF) >> 4;

                        if (resultChar != 0) {
                            g2d.setColor(foregroundColors[foregroundColorIndex]);
                            charBuffer[0] = resultChar;
                            g2d.drawChars(charBuffer, 0, 1, i * charWidth, j * charHeight + charHeight - 4);
                        }
                    }
                }

                //draw cursor
                g2d.setXORMode(Color.BLACK);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(cursorX * charWidth, cursorY * charHeight, charWidth, charHeight);
            }
        };

        Font font = new Font("Courier New", Font.BOLD, 14);
        drawPanel.setFont(font);
        Rectangle2D maxCharBounds = drawPanel.getFontMetrics(font).getMaxCharBounds(drawPanel.getGraphics());
        charWidth = (int) maxCharBounds.getWidth();
        charHeight = (int) maxCharBounds.getHeight();
        drawPanel.setSize(charWidth * 80, charHeight * 25);
        drawPanel.setPreferredSize(new Dimension(charWidth * 80, charHeight * 25));
        drawPanel.setBackground(Color.BLACK);
        drawPanel.setForeground(new Color(200, 200, 200));
        getContentPane().add(drawPanel);
        pack();
        startRedrawTimer();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Integer keyCode = KeyCodes.keyCode2LanceletKeyCode.get(e.getKeyCode());
                if (keyCode != null) {
                     checkPressedModifierKey(keyCode, true);
                    keysQueue.offer(keyCode);
                }
                e.consume();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                   Integer keyCode = KeyCodes.keyCode2LanceletKeyCode.get(e.getKeyCode());
                if (keyCode != null) {
                    checkPressedModifierKey(keyCode, false);
                }
                e.consume();
            }
        });
    }

    public boolean isAltPressed() {
        return altPressed;
    }

    public boolean isCtrlPressed() {
        return ctrlPressed;
    }

    public boolean isShiftPressed() {
        return shiftPressed;
    }

    private void checkPressedModifierKey(int keyCode, boolean valueToSet) {
        switch (keyCode) {
            case KEY_SHIFT:
                shiftPressed = valueToSet;
                break;
            case KEY_ALT:
                altPressed = valueToSet;
                break;
            case KEY_CONTROL:
                ctrlPressed = valueToSet;
                break;
            default:
                break;
        }
    }

    private void startRedrawTimer() {
        timer = new Timer(1000 / 60, (actionEvent) -> {
            int dirtyCount = dirty.get();
            if (dirtyCount > 0) {
                drawPanel.repaint();
                dirty.addAndGet(-dirtyCount);
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    public void writeToBuffer(byte value, int position) {
        buffer[position] = value;
        dirty.incrementAndGet();
    }

    public byte readFromBuffer(int position) {
        return buffer[position];
    }

    public void close() {
        timer.stop();
        setVisible(false);
        dispose();
    }

    public void setCursorXY(int cursorX, int cursorY) {
        if (cursorX > 79) {
            cursorX = 79;
        }
        if (cursorX < 0) {
            cursorX = 0;
        }
        if (cursorY > 24) {
            cursorY = 24;
        }
        if (cursorY < 0) {
            cursorY = 0;
        }
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        System.out.println("CursorXY "+this.cursorX+" "+this.cursorY);

        dirty.addAndGet(1);
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public int waitKey() {
        try {
            while (isVisible()) {
                Integer key = keysQueue.poll(1, TimeUnit.SECONDS);
                if (key != null) {
                    return key;
                }
            }
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
        return 0;
    }

    public void putString(String str) {
        for (char c : str.toCharArray()) {
            putChar(c);
        }
    }

    public void putChar(char c) {
        if (c == '\n') {
            setCursorXY(0, getCursorY() + 1);
            return;
        }
        int pos = (getCursorY() * 80 + getCursorX());
        buffer[pos * 2] = (byte) c;
        pos++;
        cursorY = pos / 80;
        cursorX = pos - (cursorY * 80);
        dirty.addAndGet(1);
    }
    
    public void putChar(char c, int x, int y) {
        int pos = (y * 80 + x);
        int byteOffset=pos*2;
        buffer[byteOffset] = (byte) c;
        buffer[byteOffset+1] =(byte) 0xF0;
        dirty.addAndGet(1);
    }
    
    /*
    read bytes from framebuffer. buffer.length = charsCount*2
    */
    public byte[] readBuffer(int x, int y, int charsCount) {
        byte[]_buffer=new byte[charsCount*2];
        int pos = (y * 80 + x)*2;
        for(int i=0;i<charsCount*2;i++){
            _buffer[i] = buffer[pos+i];
        }
        return _buffer;
    }
    
    public void putChar(byte c, byte attribute, int x, int y) {
        int pos = (y * 80 + x);
        buffer[pos * 2] = c;
        buffer[pos * 2+1] =attribute;
        dirty.addAndGet(1);
    }

    public static final int KEY_ESCAPE = 0;
    public static final int KEY_F1 = 1;
    public static final int KEY_F2 = 2;
    public static final int KEY_F3 = 3;
    public static final int KEY_F4 = 4;
    public static final int KEY_F5 = 5;
    public static final int KEY_F6 = 6;
    public static final int KEY_F7 = 7;
    public static final int KEY_F8 = 8;
    public static final int KEY_F9 = 9;
    public static final int KEY_F10 = 10;
    public static final int KEY_F11 = 11;
    public static final int KEY_F12 = 12;
    public static final int KEY_SHIFT = 13;
    public static final int KEY_CONTROL = 14;
    public static final int KEY_ALT = 15;
    public static final int KEY_HOME = 16;
    public static final int KEY_END = 17;
    public static final int KEY_PAGE_UP = 18;
    public static final int KEY_PAGE_DOWN = 19;
    public static final int KEY_UP = 20;
    public static final int KEY_DOWN = 21;
    public static final int KEY_LEFT = 22;
    public static final int KEY_RIGHT = 23;
    public static final int KEY_BACK_SPACE = 24;
    public static final int KEY_DELETE = 25;
    public static final int KEY_ENTER = 26;
    public static final int KEY_TAB = 27;
    public static final int KEY_SPACE = 28;
    public static final int KEY_BACK_QUOTE = 29;
    public static final int KEY_MINUS = 30;
    public static final int KEY_EQUALS = 31;
    public static final int KEY_QUOTE = 32;
    public static final int KEY_SEMICOLON = 33;
    public static final int KEY_OPEN_BRACKET = 34;
    public static final int KEY_CLOSE_BRACKET = 35;
    public static final int KEY_BACK_SLASH = 36;
    public static final int KEY_SLASH = 37;
    public static final int KEY_COMMA = 38;
    public static final int KEY_PERIOD = 39;
    public static final int KEY_0 = 50;
    public static final int KEY_1 = 51;
    public static final int KEY_2 = 52;
    public static final int KEY_3 = 53;
    public static final int KEY_4 = 54;
    public static final int KEY_5 = 55;
    public static final int KEY_6 = 56;
    public static final int KEY_7 = 57;
    public static final int KEY_8 = 58;
    public static final int KEY_9 = 59;
    public static final int KEY_A = 60;
    public static final int KEY_B = 61;
    public static final int KEY_C = 62;
    public static final int KEY_D = 63;
    public static final int KEY_E = 64;
    public static final int KEY_F = 65;
    public static final int KEY_G = 66;
    public static final int KEY_H = 67;
    public static final int KEY_I = 68;
    public static final int KEY_J = 69;
    public static final int KEY_K = 70;
    public static final int KEY_L = 71;
    public static final int KEY_M = 72;
    public static final int KEY_N = 73;
    public static final int KEY_O = 74;
    public static final int KEY_P = 75;
    public static final int KEY_Q = 76;
    public static final int KEY_R = 77;
    public static final int KEY_S = 78;
    public static final int KEY_T = 79;
    public static final int KEY_U = 80;
    public static final int KEY_V = 81;
    public static final int KEY_W = 82;
    public static final int KEY_X = 83;
    public static final int KEY_Y = 84;
    public static final int KEY_Z = 85;

}

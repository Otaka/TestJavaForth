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
    private final byte[] buffer = new byte[80 * 25 * 2];
    private final AtomicInteger dirty = new AtomicInteger(0);
    private Timer timer;
    private final JPanel drawPanel;
    private int cursorX = 0;
    private int cursorY = 0;
    private BlockingQueue<Integer> keysQueue = new ArrayBlockingQueue<>(20);
    private boolean shiftPressed=false;

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
                System.out.println("" + e.getKeyCode());
                Integer keyCode = KeyCodes.keyCode2LanceletKeyCode.get(e.getKeyCode());
                if (keyCode != null) {
                    if(keyCode==13){//shift
                        shiftPressed=true;
                    }
                    keysQueue.offer(keyCode);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                Integer keyCode = KeyCodes.keyCode2LanceletKeyCode.get(e.getKeyCode());
                if (keyCode != null) {
                    if(keyCode==13){//shift
                        shiftPressed=false;
                    }
                }
            }
            
        });
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

        dirty.addAndGet(1);
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public String readLine() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = getch();
            putChar(c);
            if (c == '\n') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }
    
    public char getch(){
        while(true){
            int key=waitKey();
            if(key>=40&&key<=49){
                return (char) ('0'+(key-40));
            }
            if(key>=50&&key<=75){
                if(shiftPressed){
                    return (char) ('A'+(key-50));
                }
                return (char) ('a'+(key-50));
            }
            if(key==19){
                if(shiftPressed){
                    return '<';
                }
                return ',';
            }
            if(key==20){
                if(shiftPressed){
                    return '>';
                }
                return '.';
            }
            if(key==76){
                if(shiftPressed){
                    return '?';
                }
                return '/';
            }
            if(key==28){
                if(shiftPressed){
                    return '_';
                }
                return '-';
            }
            if(key==36){
                if(shiftPressed){
                    return '+';
                }
                return '=';
            }
            if(key==16){
                return '\n';
            }
        }
    }
    
    public int waitKey() {
        try {
            return keysQueue.poll(99999, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void putString(String str) {
        for (char c : str.toCharArray()) {
            putChar(c);
        }
    }

    public void putChar(char c) {
        if(c=='\n'){
            setCursorXY(0, getCursorY()+1);
            return;
        }
        int pos = (getCursorY() * 80 + getCursorX());
        buffer[pos*2] = (byte) c;
        pos++;
        cursorY = pos / 80;
        cursorX = pos - (cursorY * 80);
        dirty.addAndGet(1);
    }
}

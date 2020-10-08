package com.testforth.terminal;

import static java.awt.event.KeyEvent.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry
 */
public class KeyCodes {

    public static Map<Integer, Integer> keyCode2LanceletKeyCode = new HashMap<>() {
        {
            put(VK_ESCAPE, 0);

            put(VK_F1, 1);
            put(VK_F2, 2);
            put(VK_F3, 3);
            put(VK_F4, 4);
            put(VK_F5, 5);
            put(VK_F6, 6);
            put(VK_F7, 7);
            put(VK_F8, 8);
            put(VK_F9, 9);
            put(VK_F10, 10);
            put(VK_F11, 11);
            put(VK_F12, 12);

            put(VK_SHIFT, 13);
            put(VK_CONTROL, 14);
            put(VK_ALT, 15);
            put(VK_ENTER, 16);
            put(VK_BACK_SPACE, 17);
            put(VK_DELETE, 18);
            put(VK_COMMA, 19);
            put(VK_PERIOD, 20);
            put(VK_BACK_SLASH, 21);
            put(VK_UP, 22);
            put(VK_DOWN, 23);
            put(VK_LEFT, 24);
            put(VK_RIGHT, 25);
            put(VK_TAB, 26);
            put(VK_BACK_QUOTE, 27);
            put(VK_MINUS, 28);
            put(VK_END, 29);
            put(VK_PAGE_UP, 30);
            put(VK_PAGE_DOWN, 31);
            put(VK_SPACE, 32);
            put(VK_HOME, 33);
            put(VK_QUOTE, 34);
            put(VK_SEMICOLON, 35);
            put(VK_EQUALS, 36);
            put(VK_OPEN_BRACKET, 37);
            put(VK_BACK_SLASH, 38);
            put(VK_CLOSE_BRACKET, 39);

            put(VK_0, 40);
            put(VK_1, 41);
            put(VK_2, 42);
            put(VK_3, 43);
            put(VK_4, 44);
            put(VK_5, 45);
            put(VK_6, 46);
            put(VK_7, 47);
            put(VK_8, 48);
            put(VK_9, 49);

            put(VK_A, 50);
            put(VK_B, 51);
            put(VK_C, 52);
            put(VK_D, 53);
            put(VK_E, 54);
            put(VK_F, 55);
            put(VK_G, 56);
            put(VK_H, 57);
            put(VK_I, 58);
            put(VK_J, 59);
            put(VK_K, 60);
            put(VK_L, 61);
            put(VK_M, 62);
            put(VK_N, 63);
            put(VK_O, 64);
            put(VK_P, 65);
            put(VK_Q, 66);
            put(VK_R, 67);
            put(VK_S, 68);
            put(VK_T, 69);
            put(VK_U, 70);
            put(VK_V, 71);
            put(VK_W, 72);
            put(VK_X, 73);
            put(VK_Y, 74);
            put(VK_Z, 75);
            put(VK_SLASH, 76);
        }
    };
}

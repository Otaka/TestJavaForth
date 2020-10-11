package com.testforth.terminal;

import static java.awt.event.KeyEvent.*;
import java.util.HashMap;
import java.util.Map;
import static com.testforth.terminal.TerminalWindow.*;

/**
 * @author Dmitry
 */
class KeyCodes {

    public static Map<Integer, Integer> keyCode2LanceletKeyCode = new HashMap<>() {
        {
            put(VK_ESCAPE, KEY_ESCAPE);
            put(VK_F1, KEY_F1);
            put(VK_F2, KEY_F2);
            put(VK_F3, KEY_F3);
            put(VK_F4, KEY_F4);
            put(VK_F5, KEY_F5);
            put(VK_F6, KEY_F6);
            put(VK_F7, KEY_F7);
            put(VK_F8, KEY_F8);
            put(VK_F9, KEY_F9);
            put(VK_F10, KEY_F10);
            put(VK_F11, KEY_F11);
            put(VK_F12, KEY_F12);
            put(VK_SHIFT, KEY_SHIFT);
            put(VK_CONTROL, KEY_CONTROL);
            put(VK_ALT, KEY_ALT);
            put(VK_ENTER, KEY_ENTER);
            put(VK_BACK_SPACE, KEY_BACK_SPACE);
            put(VK_DELETE, KEY_DELETE);
            put(VK_COMMA, KEY_COMMA);
            put(VK_PERIOD, KEY_PERIOD);
            put(VK_UP, KEY_UP);
            put(VK_DOWN, KEY_DOWN);
            put(VK_LEFT, KEY_LEFT);
            put(VK_RIGHT, KEY_RIGHT);
            put(VK_TAB, KEY_TAB);
            put(VK_BACK_QUOTE, KEY_BACK_QUOTE);
            put(VK_MINUS, KEY_MINUS);
            put(VK_END, KEY_END);
            put(VK_PAGE_UP, KEY_PAGE_UP);
            put(VK_PAGE_DOWN, KEY_PAGE_DOWN);
            put(VK_SPACE, KEY_SPACE);
            put(VK_HOME, KEY_HOME);
            put(VK_QUOTE, KEY_QUOTE);
            put(VK_SEMICOLON, KEY_SEMICOLON);
            put(VK_EQUALS, KEY_EQUALS);
            put(VK_OPEN_BRACKET, KEY_OPEN_BRACKET);
            put(VK_CLOSE_BRACKET, KEY_CLOSE_BRACKET);
            put(VK_BACK_SLASH, KEY_BACK_SLASH);
            put(VK_SLASH, KEY_SLASH);
            put(VK_0, KEY_0);
            put(VK_1, KEY_1);
            put(VK_2, KEY_2);
            put(VK_3, KEY_3);
            put(VK_4, KEY_4);
            put(VK_5, KEY_5);
            put(VK_6, KEY_6);
            put(VK_7, KEY_7);
            put(VK_8, KEY_8);
            put(VK_9, KEY_9);
            put(VK_A, KEY_A);
            put(VK_B, KEY_B);
            put(VK_C, KEY_C);
            put(VK_D, KEY_D);
            put(VK_E, KEY_E);
            put(VK_F, KEY_F);
            put(VK_G, KEY_G);
            put(VK_H, KEY_H);
            put(VK_I, KEY_I);
            put(VK_J, KEY_J);
            put(VK_K, KEY_K);
            put(VK_L, KEY_L);
            put(VK_M, KEY_M);
            put(VK_N, KEY_N);
            put(VK_O, KEY_O);
            put(VK_P, KEY_P);
            put(VK_Q, KEY_Q);
            put(VK_R, KEY_R);
            put(VK_S, KEY_S);
            put(VK_T, KEY_T);
            put(VK_U, KEY_U);
            put(VK_V, KEY_V);
            put(VK_W, KEY_W);
            put(VK_X, KEY_X);
            put(VK_Y, KEY_Y);
            put(VK_Z, KEY_Z);
        }
    };
}

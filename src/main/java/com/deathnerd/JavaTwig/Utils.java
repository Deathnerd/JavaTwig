package com.deathnerd.JavaTwig;

/**
 * Created by wes on 1/12/17.
 */
public class Utils {

    public static boolean lastCharacterIsLetter(String input) {
        return Character.isLetter(input.charAt(input.length() - 1));
    }
}

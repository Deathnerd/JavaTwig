package com.deathnerd.JavaTwig;

import java.util.regex.Pattern;

/**
 * Created by Wes Gilleland on 1/11/2017.
 */
public class Twig_Lexer {
    public final int STATE_DATA = 0;
    public final int STATE_BLOCK = 1;
    public final int STATE_VAR = 2;
    public final int STATE_STRING = 3;
    public final int STATE_INTERPOLATION = 4;
    // TODO fix regexes
    public final String REGEX_NAME = "^[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*";
    public final String REGEX_NUMBER = "^[0-9]+(?:\\.[0-9]+)?";
    public final String REGEX_STRING = "^\"([^#\"\\\\\\\\]*(?:\\\\\\\\.[^#\"\\\\\\\\]*)*)\"|'([^'\\\\\\\\]*(?:\\\\\\\\.[^'\\\\\\\\]*)*)'";
    public final String REGEX_DQ_STRING_DELIM = "^\"";
    public final String REGEX_DQ_STRING_PART = "^[^#\"\\\\\\\\]*(?:(?:\\\\\\\\.|#(?!\\{))[^#\"\\\\\\\\]*)*";
    public final String PUNCTUATION = "\(\)\[\]\{\}\?\:\.\,\|";
}

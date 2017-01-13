package com.deathnerd.JavaTwig;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

/**
 * Unit test for simple Twig.
 */
public class TwigTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TwigTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TwigTest.class );
    }

    public void testThing() throws Exception {
        String[] tag_comments = new String[]{"{#", "#}"};
        String[] tag_blocks = new String[]{"{%", "%}"};
        String[] tag_variables = new String[]{"{{", "}}"};
        String whitespace_trim = "-";
        String[] interpolations = new String[]{"#{", "}"};
        Pattern lex_tokens_start = compile("(" + quote(tag_variables[0]) + "|" + quote(tag_blocks[0]) + "|" + quote(tag_comments[0]) + ")(" + quote(whitespace_trim) + ")?", DOTALL);
        /*String [] matches = Pcre.preg_match_all(lex_tokens_start, "{% verbatim %}*********{% endverbatim %}");
        for (String match :
                matches) {
            System.out.println(match);
        }*/

        Matcher token_starts_matcher = lex_tokens_start.matcher("{% verbatim %}******{% endverbatim %}");
        while(token_starts_matcher.find()) {

            System.out.println(token_starts_matcher.start());
        }
    }
}

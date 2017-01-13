package com.deathnerd.JavaTwig;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.quote;
import static java.util.regex.Pattern.compile;

/**
 * Created by Wes Gilleland on 1/11/2017.
 */
public class Twig_Lexer {
    public final Pattern REGEX_NAME = compile("^[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*");
    public final Pattern REGEX_NUMBER = compile("^[0-9]+(?:\\.[0-9]+)?");
    public final Pattern REGEX_STRING = compile("^\"([^#\"\\\\\\\\]*(?:\\\\\\\\.[^#\"\\\\\\\\]*)*)\"|'([^'\\\\\\\\]*(?:\\\\\\\\.[^'\\\\\\\\]*)*)'", DOTALL);
    public final Pattern REGEX_DQ_STRING_DELIM = compile("^\"");
    public final Pattern REGEX_DQ_STRING_PART = compile("^[^#\"\\\\\\\\]*(?:(?:\\\\\\\\.|#(?!\\{))[^#\"\\\\\\\\]*)*", DOTALL);
    public final String PUNCTUATION = "()[]{}?:.,|";
    private Twig_Source source;
    private States state;
    private String code;
    private int cursor;
    private int lineno;
    private int end;
    private List<Twig_Token> tokens;
    private List<States> states;
    private Map<String, Object> options = new HashMap<String, Object>() {{
        put("tag_comment", new String[]{"{#", "#}"});
        put("tag_block", new String[]{"{%", "%}"});
        put("tag_variable", new String[]{"{{", "}}"});
        put("whitespace_trim", "-");
        put("interpolation", new String[]{"#{", "}"});
    }};
    private Map<String, Pattern> regexes = new HashMap<>();
    private Twig_Environment env;
    private List<Bracket> brackets;
    private int position;
    private ArrayList<List<Bracket>> positions;

    /**
     * <p>Construct a new {@link Twig_Lexer} with option overrides</p>
     *
     * @param env     The environment to use for the lexer
     * @param options Custom options to use for this lexer. See: {@link #options}
     */
    Twig_Lexer(Twig_Environment env, Map<String, Object> options) {
        this.env = env;
        this.options = Stream.of(this.options, options)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.buildRegexes();
    }

    /**
     * <p>Construct a new {@link Twig_Lexer} with default options</p>
     *
     * @param env The environment to use for the lexer
     */
    Twig_Lexer(Twig_Environment env) {
        this.env = env;
        this.buildRegexes();
    }

    /**
     * <p>Builds all of the regular expressions needed for the lexer based on what's provided in the {@link #options}</p>
     */
    private void buildRegexes() {
        String whitespace_trim = (String) this.options.get("whitespace_trim");
        String[] tag_variables = (String[]) this.options.get("tag_variable");
        String[] tag_blocks = (String[]) this.options.get("tag_block");
        String[] interpolations = (String[]) this.options.get("interpolation");
        String[] tag_comments = (String[]) this.options.get("tag_comment");
        this.regexes.put("lex_var", compile("^\\s*" + quote(whitespace_trim + tag_variables[1]) + "\\s*|^\\s*" + quote(tag_variables[1])));
        this.regexes.put("lex_block", compile("^\\s*(?:" + quote(whitespace_trim + tag_blocks[1]) + "\\s*|^\\s*)" + quote(tag_blocks[1]) + "\\n?"));
        this.regexes.put("lex_raw_data", compile("(" + quote(tag_blocks[0] + whitespace_trim) + ")|" + quote(tag_blocks[0]) + ")\\s*(?:endverbatim)\\s*(?:" + quote(whitespace_trim + tag_blocks[1]) + "\\s*|\\s*" + quote(tag_blocks[1]) + ")", DOTALL));
        this.regexes.put("operator", this.getOperatorRegex());
        this.regexes.put("lex_comment", compile("(?:" + quote(whitespace_trim + tag_comments[1]) + "\\s*|" + quote(tag_comments[1]) + ")\\n?", DOTALL));
        this.regexes.put("lex_block_raw", compile("^\\s*verbatim\\s*(?:" + quote(whitespace_trim + tag_blocks[1]) + "\\s*|^\\s*" + quote(tag_blocks[1]) + ")", DOTALL));
        this.regexes.put("lex_block_line", compile("^\\s*line\\s+(\\d+)\\s*" + quote(tag_blocks[1]), DOTALL));
        this.regexes.put("lex_tokens_start", compile("(" + quote(tag_variables[0]) + "|" + quote(tag_blocks[0]) + "|" + quote(tag_comments[0]) + ")(" + quote(whitespace_trim) + ")?", DOTALL));
        this.regexes.put("interpolation_start", compile("^" + quote(interpolations[0]) + "\\s*"));
        this.regexes.put("interpolation_end", compile("^\\s*" + quote(interpolations[1])));
    }

    /**
     * <p>Compiles the regex pattern for identifying operators by getting the
     * unary and binary operators from the environment and formatting them as needed
     * through a series of streams</p>
     *
     * @return The compiled operator regular expression pattern
     */
    private Pattern getOperatorRegex() {
        // Create a reverse sorted map of the operators and their lengths
        TreeMap<Integer, String> operators = new TreeMap<>(Collections.reverseOrder());
        (new ArrayList<>(new HashSet<String>() {{
            add("=");
            addAll(env.getUnaryOperators().keySet());
            addAll(env.getBinaryOperators().keySet());
        }})).parallelStream()
                .forEach(item -> operators.put(item.length(), item));

        // Start building the regex
        String regex = "^" + Stream.of(operators)
                .flatMap(s -> s.entrySet().stream())
                .parallel()
                .map(e -> {
                            String operator = e.getValue();
                            // An operator that ends with a character must be followed by
                            // a whitespace or a parenthesis
                            String r = quote(operator);
                            if (Character.isLetter(operator.charAt(operator.length() - 1))) {
                                r += "(?=[\\s()])";
                            }
                            // an operator with a space can be any amount of whitespaces
                            e.setValue(r.replaceAll("\\s+", "\\s+"));
                            return e;
                        }
                )
                .map(Map.Entry::getValue)
                .collect(Collectors.joining("|^"));

        return compile(regex);
    }

    public Twig_TokenStream tokenize(Twig_Source source) {
        this.source = source;
        this.code = source.getCode().replaceAll("\\r\\n|\\r", "\n");
        this.cursor = 0;
        this.lineno = 1;
        this.end = this.code.length();
        this.state = States.DATA;
        this.states = new ArrayList<>();
        this.tokens = new ArrayList<>();
        this.brackets = new ArrayList<>();
        this.position = -1;

        // find all the token starts in one go
        Matcher token_starts_matcher = this.regexes.get("lex_tokens_start").matcher(this.code);
        this.positions = new ArrayList<List<Bracket>>();

//        {
//          "positions": [
//            [
//              [
//                "{%",
//                0
//              ],
//              [
//                "{%",
//                100014
//              ]
//            ],
//            [
//              [
//                "{%",
//                0
//              ],
//              [
//                "{%",
//                100014
//              ]
//            ],
//            [
//              "",
//              ""
//            ]
//          ]
//        }

        while (token_starts_matcher.find()) {
            Bracket bracket = new Bracket(token_starts_matcher.group(), token_starts_matcher.start());
            List<Bracket> b = new ArrayList<>();
            b.add(bracket);
            this.positions.add(b);
        }

        return null;
    }

    public enum States {
        DATA,
        BLOCK,
        VAR,
        STRING,
        INTERPOLATION
    }

    class Bracket {
        protected String code;
        protected int line_no;

        Bracket(String code, int line_no) {
            this.code = code;
            this.line_no = line_no;
        }
    }
}

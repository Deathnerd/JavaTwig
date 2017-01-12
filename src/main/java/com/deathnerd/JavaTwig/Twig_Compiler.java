package com.deathnerd.JavaTwig;

import com.sun.deploy.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Wes Gilleland on 1/11/2017.
 */
public class Twig_Compiler {
    private Integer lastLine;
    private String source;
    private int indentation;
    private Twig_Environment env;
    private Map<Integer, Object> debugInfo = new HashMap<>();
    private int sourceOffset;
    private int sourceLine;

    Twig_Compiler(Twig_Environment env) {
        this.env = env;
    }

    /**
     * Compiles a node with no indentation
     *
     * @param node The node to compile
     * @return The compiler ready to be chained
     */
    public Twig_Compiler compile(Twig_Node node) {
        return this.compile(node, 0);
    }

    /**
     * Compiles a node
     *
     * @param node        The node to compile
     * @param indentation The current indentation level
     * @return The compiler ready to be chained
     */
    public Twig_Compiler compile(Twig_Node node, int indentation) {
        this.lastLine = null;
        this.source = "";
        this.debugInfo = new HashMap<>();
        this.sourceOffset = 0;
        // Source code starts as 1. It is then incremented as new lines are encountered
        this.sourceLine = 1;
        this.indentation = indentation;

        node.compile(this);

        return this;
    }

    /**
     * @param node The node to subcompile
     * @param raw  Should we apply indentation?
     * @return The compiler ready to be chained
     */
    public Twig_Compiler subCompile(Twig_Node node, boolean raw) {
        if (!raw) {
            this.source += getIndentation();
        }

        node.compile(this);

        return this;
    }

    private String getIndentation() {
        return new String(new char[this.indentation]).replace("\0", " ");
    }

    /**
     * @param node The node to subcompile
     * @return The compiler ready to be chained
     */
    public Twig_Compiler subCompile(Twig_Node node) {
        return this.subCompile(node, true);
    }

    /**
     * Adds a raw string to the compiled code
     *
     * @param string The raw string to add
     * @return The compiler ready to be chained
     */
    public Twig_Compiler raw(String string) {
        this.source += string;
        return this;
    }

    /**
     * Writes a string to the compiled code by adding indentation
     *
     * @param strings The strings to write
     * @return The compiler ready to be chained
     */
    public Twig_Compiler write(String... strings) {
        this.source += Stream.of(strings).map(s -> this.getIndentation() + s).collect(Collectors.joining(""));

        return this;
    }

    /**
     * Adds debugging info
     *
     * @param node The node to add debugging info for
     * @return The compiler ready to be chained
     */
    public Twig_Compiler addDebugInfo(Twig_Node node) {
        Integer templateLine = node.getTemplateLine();
        if (!templateLine.equals(this.lastLine)) {
            this.write(String.format("// line %d\n", templateLine));
            this.sourceLine += org.apache.commons.lang3.StringUtils.countMatches(this.source.substring(this.sourceOffset), "\n");
            this.sourceOffset = this.source.length();
            this.debugInfo.put(this.sourceLine, templateLine);

            this.lastLine = templateLine;
        }

        return this;
    }

    /**
     * Indents the generated code by one step
     *
     * @return The compiler ready to be chained
     */
    public Twig_Compiler indent() {
        return this.indent(1);
    }

    /**
     * Indents the generated code
     *
     * @param step The number of indentations to add
     * @return The compiler ready to be chained
     */
    public Twig_Compiler indent(int step) {
        this.indentation += step;

        return this;
    }

    /**
     * Convenience method to outdent by one level
     *
     * @return The compiler ready to be chained
     * @throws LogicException When trying to outdent too much so the indentation would become negative
     */
    public Twig_Compiler outdent() throws LogicException {
        return this.outdent(1);
    }

    /**
     * Outdents the generated code
     *
     * @param step The number of indentations to remove
     * @return The compiler ready to be chained
     * @throws LogicException When trying to outdent too much so the indentation would become negative
     */
    public Twig_Compiler outdent(int step) throws LogicException {
        // can't outdent by more steps than the current level
        if (this.indentation < step) {
            throw new LogicException("Unable to call outdent() as the indentation would become negative");
        }
        this.indentation -= step;

        return this;
    }

    /**
     * @return The environment instance related to this compiler
     */
    public Twig_Environment getEnvironment() {
        return this.env;
    }

    /**
     * Gets the current code after compilation
     *
     * @return The code
     */
    public String getSource() {
        return source;
    }

    /**
     * Sorts the debug info for the current compiler by its keys by putting it into a TreeMap
     * and then returns it
     *
     * @return The key sorted debug info for the current compiler
     */
    public Map<Integer, Object> getDebugInfo() {
        return new TreeMap<>(this.debugInfo);
    }

    /**
     * Generates a unique variable name
     * @return A unique variable name
     */
    public static String getVarName() {
        String out = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(UUID.randomUUID().toString().getBytes());
            byte[] digestBytes = md.digest();
            out = String.format("__internal_%s", Base64.getEncoder().encodeToString(digestBytes));
        } catch (NoSuchAlgorithmException e) {
            // empty catch because we can always generate this algorithm
        }

        return out;
    }
}

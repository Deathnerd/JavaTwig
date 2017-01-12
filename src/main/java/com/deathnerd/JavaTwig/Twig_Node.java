package com.deathnerd.JavaTwig;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Wes Gilleland on 1/11/2017.
 */
public class Twig_Node {
    protected LinkedHashMap<String, Twig_Node> nodes;
    protected LinkedHashMap<String, Object> attributes;
    protected int line_no;
    protected String tag;
    private String name;

    Twig_Node(LinkedHashMap<String, Twig_Node> nodes, LinkedHashMap<String, Object> attributes, int line_no, String tag) {
        this.nodes = nodes;
        this.attributes = attributes;
        this.line_no = line_no;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "<Twig_Node:" + this.getClass().getName() + " />";
        // TODO Implement the PHP version of this
//        String attributes = StringUtils.join(
//                this.attributes.entrySet()
//                        .parallelStream()
//                        .map(e -> e.getKey() + ": " + e.getValue().toString().replace("\n", ""))
//                        .collect(Collectors.toList()),
//                ", ");
//        ArrayList<String> repr = new ArrayList<>();
//        repr.add(this.getClass().getName() + "("+attributes;
    }

    public void compile(Twig_Compiler compiler) {
        for (String key :
                this.nodes.keySet()) {
            Twig_Node node = this.nodes.get(key);
            node.compile(compiler);
            this.nodes.replace(key, node);
        }
    }

    public Integer getTemplateLine() {
        return this.line_no;
    }

    public String getNodeTag() {
        return this.tag;
    }

    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    public Object getAttribute(String name) throws LogicException {
        if (!this.hasAttribute(name)) {
            throw new LogicException(String.format("Attribute \"%s\" does not exist for Node \"%s\".", name, this.getClass().getName()));
        }

        return this.attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public boolean hasNode(String name) {
        return this.nodes.containsKey(name);
    }

    public Twig_Node getNode(String name) throws LogicException {
        if (!this.hasNode(name)) {
            throw new LogicException(String.format("Node \"%s\" does not exist for Node \"%s\".", name, this.getClass().getName()));
        }

        return this.nodes.get(name);
    }

    public void removeNode(String name) {
        this.nodes.remove(name);
    }

    public int length() {
        return this.nodes.size();
    }

    public Iterator getIterator() {
        return this.nodes.entrySet().iterator();
    }

    public void setTemplateName(String name) {
        this.name = name;
        for (String key :
                this.nodes.keySet()) {
            Twig_Node node = this.nodes.get(key);
            node.setTemplateName(name);
            this.nodes.replace(key, node);
        }
    }

    public String getTemplateName() {
        return this.name;
    }
}

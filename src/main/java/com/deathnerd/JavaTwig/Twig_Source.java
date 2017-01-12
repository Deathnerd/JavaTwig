package com.deathnerd.JavaTwig;
import org.jetbrains.annotations.Contract;

/**
 * Created by Wes Gilleland on 1/11/2017.
 */
public final class Twig_Source {

    private String code;
    private String name;
    private String path;

    /**
     * @param code The template source code
     * @param name The template logical name
     */
    Twig_Source(String code, String name) {
        this.code = code;
        this.name = name;
        this.path = "";
    }

    /**
     * @param code The template source code
     * @param name The template logical name
     * @param path The filesystem path of the template if any
     */
    Twig_Source(String code, String name, String path) {
        this.code = code;
        this.name = name;
        this.path = path;
    }

    @Contract(pure = true)
    public String getCode() {
        return code;
    }

    @Contract(pure = true)
    public String getName() {
        return name;
    }

    @Contract(pure = true)
    public String getPath() {
        return path;
    }

}

package io.github.linyimin.plugin.configuration.model;

/**
 * @author banzhe
 * @date 2022/12/01 11:51
 **/
public class Lexicon {

    private String name;
    private String content;

    public Lexicon() {}

    public Lexicon(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

package io.github.linyimin.plugin.configuration.model;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/01 11:54
 **/
public class LexiconConfiguration {

    private List<Lexicon> lexicons;

    public List<Lexicon> getLexicons() {
        return lexicons;
    }

    public void setLexicons(List<Lexicon> lexicons) {
        this.lexicons = lexicons;
    }
}

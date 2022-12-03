package io.github.linyimin.plugin.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.configuration.model.LexiconConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author banzhe
 * @date 2022/12/01 11:55
 **/
@State(name = "lexicon", storages = {@Storage("mybatis-sql-config.xml")})
public class LexiconComponent implements PersistentStateComponent<LexiconConfiguration> {

    private LexiconConfiguration config;


    @Override
    public @Nullable LexiconConfiguration getState() {

        if (config == null) {
            config = new LexiconConfiguration();
            config.setLexicons(new ArrayList<>());
        }

        return config;
    }

    @Override
    public void loadState(@NotNull LexiconConfiguration state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

    public void setConfig(List<Lexicon> lexicons) {
        if (config == null) {
            config = new LexiconConfiguration();
        }
        config.setLexicons(lexicons);
    }

    public LexiconConfiguration getConfig() {
        return this.config;
    }
}

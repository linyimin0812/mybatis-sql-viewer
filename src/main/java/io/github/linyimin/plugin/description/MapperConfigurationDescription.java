package io.github.linyimin.plugin.description;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import io.github.linyimin.plugin.dom.model.MybatisConfiguration;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yiminlin
 * @date 2022/02/05 2:45 上午
 **/
public class MapperConfigurationDescription extends DomFileDescription<MybatisConfiguration> {

    public MapperConfigurationDescription() {
        super(MybatisConfiguration.class, "configuration");
    }

    @Override
    public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {
        return MapperDomUtils.isMybatisConfigurationFile(file);
    }
}

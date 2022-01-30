package io.github.linyimin.plugin.description;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import io.github.linyimin.plugin.dom.model.Mapper;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yiminlin
 * @date 2022/01/23 3:24 上午
 * @description mapper xml文件处理
 **/
public class MapperDescription extends DomFileDescription<Mapper> {

    public MapperDescription() {
        super(Mapper.class, "mapper");
    }

    @Override
    public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {
        return MapperDomUtils.isMybatisMapperFile(file);
    }

}

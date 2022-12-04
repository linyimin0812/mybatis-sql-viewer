package io.github.linyimin.plugin.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.linyimin.plugin.configuration.model.MockDataPrimaryId4Save;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/04 15:58
 **/
@State(name = "mock-data-id-list", storages = {@Storage("mybatis-sql-config.xml")})
public class MockDataSaveComponent implements PersistentStateComponent<MockDataPrimaryId4Save> {

    private MockDataPrimaryId4Save data;

    @Override
    public @Nullable MockDataPrimaryId4Save getState() {
        if (data == null) {
            data = new MockDataPrimaryId4Save();
            data.setList(new ArrayList<>());
        }
        return data;
    }

    @Override
    public void loadState(@NotNull MockDataPrimaryId4Save state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

    public void remove(String table) {
        List<MockDataPrimaryId4Save.PrimaryIdInTable> list = this.data.getList().stream()
                .filter( data -> !StringUtils.equals(table, data.getTable()))
                .collect(Collectors.toList());

        this.data.setList(list);
    }

    public void addPrimaryIdInTable(MockDataPrimaryId4Save.PrimaryIdInTable primaryIdInTable) {

        if (this.data.getList() == null) {
            this.data.setList(new ArrayList<>());
        }

        MockDataPrimaryId4Save.PrimaryIdInTable exist = this.data.getList().stream()
                .filter(data -> StringUtils.equals(primaryIdInTable.getTable(), data.getTable()))
                .findFirst()
                .orElse(null);

        if (exist == null) {
            this.data.getList().add(primaryIdInTable);
            return;
        }

        if (exist.getMinId() < primaryIdInTable.getMinId()) {
            primaryIdInTable.setMinId(exist.getMinId());
        }

        if (exist.getMaxId() > primaryIdInTable.getMaxId()) {
            primaryIdInTable.setMaxId(exist.getMaxId());
        }

    }
}

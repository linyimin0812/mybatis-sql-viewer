package io.github.linyimin.plugin.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.mock.enums.FieldTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import io.github.linyimin.plugin.mock.schema.TableField;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.List;
import java.util.Vector;

/**
 * @author banzhe
 * @date 2022/12/16 17:30
 **/
public class MockTypeUtils {

    public static final String MOCK_TYPE_COLUMN_NAME = "Mock Type";
    public static final String MOCK_VALUE_COLUMN_NAME = "Mock Value";

    public static DefaultTableModel copyModel(DefaultTableModel model) {

        Vector<String> columnIdentifiers = new Vector<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            columnIdentifiers.add(model.getColumnName(i));
        }

        Vector dataVector = new Vector(model.getDataVector());

        return new DefaultTableModel(dataVector, columnIdentifiers);

    }

    public static void addMockColumns(JTable table, ComboBox<String> mockTypes, boolean isStressMode) {

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (isStressMode) {
            initStressMockColumnValues(model);
        } else {
            initMockColumnValues(model);
        }


        table.setModel(model);

        TableColumnModel columnModel = table.getColumnModel();

        TableColumn typeColumn = columnModel.getColumn(columnModel.getColumnIndex(MOCK_TYPE_COLUMN_NAME));

        typeColumn.setCellEditor(new DefaultCellEditor(mockTypes));

        TableColumn valueColumn = columnModel.getColumn(columnModel.getColumnIndex(MOCK_VALUE_COLUMN_NAME));

        valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));

        mockTypes.addActionListener(e -> mockTypeSelectionListener(mockTypes, valueColumn));

    }

    private static void initStressMockColumnValues(DefaultTableModel model) {
        int tableIndex = model.findColumn("Table");
        int nameIndex = model.findColumn("Name");

        Vector<String> mockTypes = new Vector<>();
        Vector<String> mockParamTypes = new Vector<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            mockTypes.add(MockTypeEnum.database.name());
            String value = String.format("%s.%s", model.getValueAt(i, tableIndex).toString(), model.getValueAt(i, nameIndex));
            mockParamTypes.add(value);
        }

        model.addColumn(MOCK_TYPE_COLUMN_NAME, mockTypes);
        model.addColumn(MOCK_VALUE_COLUMN_NAME, mockParamTypes);
    }

    private static void initMockColumnValues(DefaultTableModel model) {

        int keyIndex = model.findColumn("Key");
        int typeIndex = model.findColumn("Type");

        Vector<String> mockTypes = new Vector<>();
        Vector<String> mockParamTypes = new Vector<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            String key = (String) model.getValueAt(i, keyIndex);
            if (StringUtils.endsWithIgnoreCase(key, "PRI")) {
                mockTypes.add(MockTypeEnum.none.name());
                mockParamTypes.add(StringUtils.EMPTY);
            } else {

                mockTypes.add(MockTypeEnum.random.name());

                String type = TableField.parseType((String) model.getValueAt(i, typeIndex));
                mockParamTypes.add(FieldTypeEnum.resolve(type).getMockType().name());
            }
        }

        model.addColumn(MOCK_TYPE_COLUMN_NAME, mockTypes);
        model.addColumn(MOCK_VALUE_COLUMN_NAME, mockParamTypes);
    }

    private static void mockTypeSelectionListener(ComboBox<String> typeCombobox, TableColumn valueColumn) {

        String type = (String) typeCombobox.getSelectedItem();

        String toolTipText = "";

        if (StringUtils.equals(type, MockTypeEnum.random.name())) {
            toolTipText = "random rule";
            ComboBox<String> comboBox = new ComboBox<>();
            for (MockRandomParamTypeEnum randomType : MockRandomParamTypeEnum.values()) {
                comboBox.addItem(randomType.name());
            }
            valueColumn.setCellEditor(new DefaultCellEditor(comboBox));
        }

        if (StringUtils.equals(type, MockTypeEnum.increment.name())) {
            toolTipText = "initial value";

            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.fixed.name())) {
            toolTipText = "constant value";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.regex.name())) {
            toolTipText = "regular expression";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.lexicon.name())) {
            toolTipText = "lexicon";
            List<Lexicon> lexicons = ApplicationManager.getApplication().getComponent(LexiconComponent.class).getConfig().getLexicons();
            ComboBox<String> comboBox = new ComboBox<>();
            for (Lexicon lexicon : lexicons) {
                comboBox.addItem(lexicon.getName());
            }
            valueColumn.setCellEditor(new DefaultCellEditor(comboBox));
        }

        if (StringUtils.equals(type, MockTypeEnum.database.name())) {
            toolTipText = "field from table";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.none.name())) {
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(toolTipText);
        valueColumn.setCellRenderer(renderer);
    }
}

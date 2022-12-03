package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static io.github.linyimin.plugin.constant.Constant.TABLE_ROW_HEIGHT;

public class LexiconDialog extends JDialog {

    private JPanel contentPane;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton addButton;
    private JTable lexiconTable;

    private final Project project;

    public LexiconDialog(Project project) {

        this.project = project;

        setLocationRelativeTo(null);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(saveButton);

        lexiconTable.setRowHeight(TABLE_ROW_HEIGHT);

        saveButton.addActionListener(e -> saveLexicon());

        cancelButton.addActionListener(e -> dispose());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        this.addButton.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) this.lexiconTable.getModel();
            model.addRow(new String[] {"", ""});
            this.lexiconTable.setModel(model);
        });

    }

    private void saveLexicon() {

        DefaultTableModel model = (DefaultTableModel) this.lexiconTable.getModel();

        Vector<Vector<String>> dataVector = model.getDataVector();

        List<Lexicon> lexicons = new ArrayList<>();

        for (Vector<String> vector : dataVector) {

            String name = vector.get(0);
            String content = vector.get(1);

            if (StringUtils.isAnyBlank(name, content)) {
                continue;
            }

            Lexicon lexicon = new Lexicon(vector.get(0), vector.get(1));
            lexicons.add(lexicon);
        }

        LexiconComponent component = project.getComponent(LexiconComponent.class);
        component.setConfig(lexicons);

    }

    public JTable getLexiconTable() {
        return lexiconTable;
    }
}

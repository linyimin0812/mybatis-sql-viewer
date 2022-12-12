package io.github.linyimin.plugin.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author banzhe
 * @date 2022/12/12 12:04
 **/
public class MouseCursorAdapter extends MouseAdapter {

    private final JComponent component;

    public MouseCursorAdapter(JComponent component) {
        this.component = component;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}

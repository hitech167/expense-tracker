package expensetracker;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** A small on/off slide switch. Click it (or press Space) and the knob slides across. */
public class ToggleSwitch extends JComponent {

    private boolean selected;
    private float position;               // 0 = off (left), 1 = on (right)
    private final Timer animation;
    private final List<ActionListener> listeners = new ArrayList<>();

    public ToggleSwitch(boolean selected) {
        this.selected = selected;
        this.position = selected ? 1f : 0f;
        setPreferredSize(new Dimension(46, 26));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(true);
        setOpaque(false);

        animation = new Timer(12, e -> {
            float target = this.selected ? 1f : 0f;
            position += (target - position) * 0.35f;
            if (Math.abs(target - position) < 0.02f) {
                position = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                toggle();
            }
        });
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "toggle");
        getActionMap().put("toggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggle();
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
    }

    public boolean isSelected() {
        return selected;
    }

    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    private void toggle() {
        selected = !selected;
        animation.start();
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "toggle");
        for (ActionListener l : new ArrayList<>(listeners)) {
            l.actionPerformed(event);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int trackH = 22;
        int trackY = (h - trackH) / 2;

        Color off = UITheme.INPUT_BORDER;
        Color on = UITheme.PRIMARY;
        Color track = new Color(
                Math.round(off.getRed() + (on.getRed() - off.getRed()) * position),
                Math.round(off.getGreen() + (on.getGreen() - off.getGreen()) * position),
                Math.round(off.getBlue() + (on.getBlue() - off.getBlue()) * position));
        g2.setColor(track);
        g2.fillRoundRect(0, trackY, w, trackH, trackH, trackH);

        int knob = trackH - 6;
        int knobX = Math.round(3 + position * (w - knob - 6));
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(knobX, trackY + 4, knob, knob);
        g2.setColor(Color.WHITE);
        g2.fillOval(knobX, trackY + 3, knob, knob);

        if (isFocusOwner()) {
            g2.setColor(UITheme.PRIMARY);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(-1, trackY - 1, w + 1, trackH + 1, trackH + 2, trackH + 2);
        }
        g2.dispose();
    }
}

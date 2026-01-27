package com.hotel.app.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Small vector icons that do not depend on font glyph availability.
 */
public final class Icons {
    private Icons() {
    }

    public static Icon home(int size, Color color) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(1.6f, w / 10f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);

            Path2D house = new Path2D.Float();
            house.moveTo(w * 0.2, h * 0.55);
            house.lineTo(w * 0.5, h * 0.25);
            house.lineTo(w * 0.8, h * 0.55);
            g2.draw(house);

            Shape body = new RoundRectangle2D.Double(w * 0.28, h * 0.52, w * 0.44, h * 0.33, w * 0.12, w * 0.12);
            g2.draw(body);

            Shape door = new RoundRectangle2D.Double(w * 0.47, h * 0.64, w * 0.10, h * 0.21, w * 0.08, w * 0.08);
            g2.draw(door);
        });
    }

    public static Icon booking(int size, Color color) {
        // Calendar icon
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(1.6f, w / 11f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);

            Shape outer = new RoundRectangle2D.Double(w * 0.20, h * 0.24, w * 0.60, h * 0.58, w * 0.18, w * 0.18);
            g2.draw(outer);

            g2.drawLine((int) (w * 0.20), (int) (h * 0.38), (int) (w * 0.80), (int) (h * 0.38));

            // Rings
            g2.drawLine((int) (w * 0.34), (int) (h * 0.18), (int) (w * 0.34), (int) (h * 0.30));
            g2.drawLine((int) (w * 0.66), (int) (h * 0.18), (int) (w * 0.66), (int) (h * 0.30));

            // Small dot
            Shape dot = new Ellipse2D.Double(w * 0.40, h * 0.50, w * 0.08, w * 0.08);
            g2.fill(dot);
        });
    }

    public static Icon checkout(int size, Color color) {
        // Check mark in circle
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(1.8f, w / 9f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);

            Shape circle = new Ellipse2D.Double(w * 0.18, h * 0.18, w * 0.64, h * 0.64);
            g2.draw(circle);

            Path2D tick = new Path2D.Float();
            tick.moveTo(w * 0.34, h * 0.52);
            tick.lineTo(w * 0.46, h * 0.64);
            tick.lineTo(w * 0.68, h * 0.40);
            g2.draw(tick);
        });
    }

    public static Icon services(int size, Color color) {
        // Sliders icon
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(1.8f, w / 10f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);

            // 3 lines
            g2.drawLine((int) (w * 0.22), (int) (h * 0.32), (int) (w * 0.78), (int) (h * 0.32));
            g2.drawLine((int) (w * 0.22), (int) (h * 0.50), (int) (w * 0.78), (int) (h * 0.50));
            g2.drawLine((int) (w * 0.22), (int) (h * 0.68), (int) (w * 0.78), (int) (h * 0.68));

            // knobs
            g2.fill(new Ellipse2D.Double(w * 0.32, h * 0.26, w * 0.12, w * 0.12));
            g2.fill(new Ellipse2D.Double(w * 0.56, h * 0.44, w * 0.12, w * 0.12));
            g2.fill(new Ellipse2D.Double(w * 0.40, h * 0.62, w * 0.12, w * 0.12));
        });
    }

    public static Icon customer(int size, Color color) {
        // User icon
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(1.8f, w / 10f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);

            Shape head = new Ellipse2D.Double(w * 0.34, h * 0.18, w * 0.32, w * 0.32);
            g2.draw(head);

            Shape body = new Arc2D.Double(w * 0.22, h * 0.40, w * 0.56, h * 0.48, 200, 140, Arc2D.OPEN);
            g2.draw(body);
        });
    }

    public static Icon revenue(int size, Color color) {
        // Bar chart icon
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            float stroke = Math.max(1.6f, w / 12f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // baseline
            g2.drawLine((int) (w * 0.22), (int) (h * 0.78), (int) (w * 0.78), (int) (h * 0.78));

            // bars
            Shape b1 = new RoundRectangle2D.Double(w * 0.26, h * 0.50, w * 0.12, h * 0.28, w * 0.08, w * 0.08);
            Shape b2 = new RoundRectangle2D.Double(w * 0.44, h * 0.40, w * 0.12, h * 0.38, w * 0.08, w * 0.08);
            Shape b3 = new RoundRectangle2D.Double(w * 0.62, h * 0.30, w * 0.12, h * 0.48, w * 0.08, w * 0.08);
            g2.fill(b1);
            g2.fill(b2);
            g2.fill(b3);
        });
    }

    public static Icon refresh(int size, Color color) {
        // Circular arrow
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(1.8f, w / 9f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);

            Shape arc = new Arc2D.Double(w * 0.20, h * 0.20, w * 0.60, h * 0.60, 45, 270, Arc2D.OPEN);
            g2.draw(arc);

            Path2D arrow = new Path2D.Float();
            arrow.moveTo(w * 0.70, h * 0.26);
            arrow.lineTo(w * 0.84, h * 0.26);
            arrow.lineTo(w * 0.84, h * 0.40);
            g2.draw(arrow);
        });
    }

    public static Icon search(int size, Color color) {
        // Magnifying glass
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(1.8f, w / 9f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);

            double r = w * 0.28;
            double cx = w * 0.45;
            double cy = h * 0.45;
            g2.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            g2.drawLine((int) (w * 0.62), (int) (h * 0.62), (int) (w * 0.82), (int) (h * 0.82));
        });
    }

    public static Icon plus(int size, Color color) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(2.0f, w / 7f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);
            g2.drawLine(w / 2, (int) (h * 0.22), w / 2, (int) (h * 0.78));
            g2.drawLine((int) (w * 0.22), h / 2, (int) (w * 0.78), h / 2);
        });
    }

    public static Icon close(int size, Color color) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            float stroke = Math.max(2.0f, w / 8f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);
            g2.drawLine((int) (w * 0.25), (int) (h * 0.25), (int) (w * 0.75), (int) (h * 0.75));
            g2.drawLine((int) (w * 0.75), (int) (h * 0.25), (int) (w * 0.25), (int) (h * 0.75));
        });
    }

    public static Icon dot(int diameter, Color color) {
        return new VectorIcon(diameter, diameter, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.fill(new Ellipse2D.Double(0, 0, w, h));
        });
    }

    private interface Painter {
        void paint(Graphics2D g2, int width, int height, Color color);
    }

    private static final class VectorIcon implements Icon {
        private final int width;
        private final int height;
        private final Color color;
        private final Painter painter;

        private VectorIcon(int width, int height, Color color, Painter painter) {
            this.width = width;
            this.height = height;
            this.color = color;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);

                // Ensure stroke scaling behaves nicely
                AffineTransform old = g2.getTransform();
                painter.paint(g2, width, height, color);
                g2.setTransform(old);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}

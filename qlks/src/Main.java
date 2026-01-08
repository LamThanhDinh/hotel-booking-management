import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hello Swing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(360, 220);

            JPanel panel = new JPanel();
            JButton btn = new JButton("Bấm thử");
            btn.addActionListener(e ->
                    JOptionPane.showMessageDialog(frame, "Chạy GUI OK rồi nè!")
            );

            panel.add(btn);
            frame.setContentPane(panel);
            frame.setLocationRelativeTo(null); // giữa màn hình
            frame.setVisible(true);
        });
    }
}

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class QRCodeGeneratorUI extends JFrame {
    private JTextField urlField;
    private JLabel qrLabel;
    private Color foregroundColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;

    public QRCodeGeneratorUI() {
        setTitle("QRコードジェネレーター");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 最上部パネル
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // 入力フィールド
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        urlField = new JTextField(25);
        JButton generateButton = new JButton("QRコード生成");

        inputPanel.add(new JLabel("URL:"));
        inputPanel.add(urlField);
        inputPanel.add(generateButton);

        // 色選択ボタン
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton fgColorButton = new JButton("コードの色");
        JButton bgColorButton = new JButton("背景色");

        colorPanel.add(fgColorButton);
        colorPanel.add(bgColorButton);

        // 最上部のパネルに追加
        topPanel.add(inputPanel);
        topPanel.add(colorPanel);
        add(topPanel, BorderLayout.NORTH);

        // QRコード表示用ラベル
        qrLabel = new JLabel("", SwingConstants.CENTER);
        add(qrLabel, BorderLayout.CENTER);

        // 色ボタンのクリックイベント（選択した色を保存）
        fgColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "コードの色を選択", foregroundColor);
            if (selectedColor != null) {
                foregroundColor = selectedColor;
            }
        });

        bgColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "背景色を選択", backgroundColor);
            if (selectedColor != null) {
                backgroundColor = selectedColor;
            }
        });

        // 生成ボタンのクリックイベント
        generateButton.addActionListener(e -> {
            try {
                String url = urlField.getText();
                if (url.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "URLを入力してください", "エラー", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 選択された色をQRコードの生成に反映
                int qrColor = foregroundColor.getRGB();
                int bgColor = backgroundColor.getRGB();

                BufferedImage qrImage = QRCodeGenerator.generateQRCodeImage(url, 300, 300, qrColor, bgColor);
                qrLabel.setIcon(new ImageIcon(qrImage));

                QRCodeGenerator.saveQRCodeImage(qrImage, "qrcode.png");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "QRコードの生成に失敗しました", "エラー", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QRCodeGeneratorUI::new);
    }
}

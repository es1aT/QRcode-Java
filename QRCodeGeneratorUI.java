import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class QRCodeGeneratorUI extends JFrame {
    private JTextField urlField;
    private JLabel qrLabel;
    private int qrSize = 300;
    private Color foregroundColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    private String iconPath = null;

    public QRCodeGeneratorUI() {
        setTitle("QRコードジェネレーター");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 最上部パネル
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // 入力フィールド
        JPanel inputPanel = new JPanel(new FlowLayout());
        urlField = new JTextField(25);
        JButton generateButton = new JButton("QRコード生成");

        inputPanel.add(new JLabel("URL:"));
        inputPanel.add(urlField);
        inputPanel.add(generateButton);

        // 色選択ボタン
        JPanel colorPanel = new JPanel(new FlowLayout());
        JButton fgColorButton = new JButton("コード色選択");
        JButton bgColorButton = new JButton("背景色選択");
        JPanel fgColorBox = new JPanel();
        fgColorBox.setBackground(foregroundColor);
        fgColorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JPanel bgColorBox = new JPanel();
        bgColorBox.setBackground(backgroundColor);
        bgColorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        colorPanel.add(fgColorButton);
        colorPanel.add(new JLabel("現在の色:"));
        colorPanel.add(fgColorBox);
        colorPanel.add(bgColorButton);
        colorPanel.add(new JLabel("現在の色:"));
        colorPanel.add(bgColorBox);

        // ファイル選択ボタン
        JPanel filePanel = new JPanel(new FlowLayout());
        JButton iconButton = new JButton("ファイルを選択する");
        JLabel fileLabel = new JLabel("なし");

        filePanel.add(new JLabel("選択されたファイル:"));
        filePanel.add(fileLabel);
        filePanel.add(iconButton);

        // ★ アルゴリズム選択用のコンボボックスを追加
        JPanel algoPanel = new JPanel(new FlowLayout());
        // 選択肢は「通常描画」と「丸ドット描画」
        JComboBox<String> algorithmComboBox = new JComboBox<>(new String[] { "通常描画", "丸ドット描画" });
        algoPanel.add(new JLabel("描画アルゴリズム:"));
        algoPanel.add(algorithmComboBox);

        // 各パネルを最上部パネルに追加
        topPanel.add(inputPanel);
        topPanel.add(colorPanel);
        topPanel.add(filePanel);
        topPanel.add(algoPanel);

        add(topPanel, BorderLayout.NORTH);

        // QRコード表示用ラベル
        qrLabel = new JLabel("", SwingConstants.CENTER);
        add(qrLabel, BorderLayout.CENTER);

        // 色ボタンのクリックイベント
        fgColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "コードの色を選択", foregroundColor);
            if (selectedColor != null) {
                foregroundColor = selectedColor;
                fgColorBox.setBackground(selectedColor);
            }
        });

        bgColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "背景色を選択", backgroundColor);
            if (selectedColor != null) {
                backgroundColor = selectedColor;
                bgColorBox.setBackground(selectedColor);
            }
        });

        // ファイル選択ボタンのクリックイベント
        iconButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter iconFilter = new FileNameExtensionFilter(
                    "画像ファイル (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif");
            fileChooser.setFileFilter(iconFilter);

            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File icon = fileChooser.getSelectedFile();
                fileLabel.setText(icon.getName());
                iconPath = icon.getPath();
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

                int qrColor = foregroundColor.getRGB();
                int bgColor = backgroundColor.getRGB();

                BufferedImage qrImage;
                // コンボボックスの選択に応じたアルゴリズム呼び出し
                String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
                if ("丸ドット描画".equals(selectedAlgorithm)) {
                    // ドットサイズを適当に12固定 (お好みで変更可能)
                    int dotSize = 12;
                    qrImage = QRCodeGenerator.otherCircleQRCode(url, dotSize, qrColor, bgColor);
                } else {
                    // 従来の四角QR
                    qrImage = QRCodeGenerator.generateQRCodeImage(url, qrSize, qrSize, qrColor, bgColor);
                }

                // アイコンを重ねる
                if (iconPath != null) {
                    QRCodeGenerator.addIconToQRCode(qrImage, iconPath);
                }

                // 画面に表示
                qrLabel.setIcon(new ImageIcon(qrImage));

                // ファイルに保存（固定ファイル名 "qrcode.png"）
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

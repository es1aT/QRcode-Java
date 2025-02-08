import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class app extends JFrame {
    private JTextField urlField;
    private JLabel qrLabel;

    public app() {
        setTitle("QRコードジェネレーター");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 入力フィールド
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        urlField = new JTextField(25);
        JButton generateButton = new JButton("QRコード生成");

        inputPanel.add(new JLabel("URL:"));
        inputPanel.add(urlField);
        inputPanel.add(generateButton);

        add(inputPanel, BorderLayout.NORTH);

        // QRコード表示用ラベル
        qrLabel = new JLabel("", SwingConstants.CENTER);
        add(qrLabel, BorderLayout.CENTER);

        // ボタンのクリックイベント
        generateButton.addActionListener(e -> generateQRCode());

        setVisible(true);
    }

    private void generateQRCode() {
        try {
            String url = urlField.getText();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(this, "URLを入力してください", "エラー", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // QRコード生成
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);

            // BufferedImageに変換
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Swingのラベルに表示
            ImageIcon icon = new ImageIcon(qrImage);
            qrLabel.setIcon(icon);

            // QRコードをPNGとして保存
            File outputFile = new File("qrcode.png");
            Path outputPath = outputFile.toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, "png", outputPath);

            System.out.println("QRコードを生成しました: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "QRコードの生成に失敗しました", "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(app::new);
    }
}

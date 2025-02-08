package com.example.view;

import com.example.model.QRCodeGenerator;
import com.example.util.ImageUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {
    private JTextField urlField;
    private JButton generateButton;
    private JLabel qrLabel;

    public MainFrame() {
        setTitle("QRコード生成アプリ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout());

        JLabel urlLabel = new JLabel("URL:");
        urlField = new JTextField(20);
        generateButton = new JButton("QRコード生成");

        inputPanel.add(urlLabel);
        inputPanel.add(urlField);
        inputPanel.add(generateButton);

        qrLabel = new JLabel();
        qrLabel.setHorizontalAlignment(JLabel.CENTER);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(qrLabel, BorderLayout.CENTER);
        add(panel);

        generateButton.addActionListener(e -> generateQRCode());
    }

    private void generateQRCode() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "URLを入力してください。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int size = 300; // QRコードのサイズ
            // ZXingでQRコード画像を生成（エラー訂正レベルはHに設定してアイコンの重ね込みに対応）
            BufferedImage qrImage = QRCodeGenerator.generateQRCodeImage(url, size, size);

            // アイコン画像をresourcesフォルダから読み込み
            BufferedImage iconImage = ImageIO.read(new File("resources/icon.png"));
            if (iconImage != null) {
                // アイコンを中央に重ねる
                qrImage = ImageUtil.addCenterIcon(qrImage, iconImage);
            }

            // 生成したQRコード画像を画面に表示
            qrLabel.setIcon(new ImageIcon(qrImage));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "QRコード生成エラー: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

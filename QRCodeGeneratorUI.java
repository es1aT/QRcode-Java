import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
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
    // 入力フィールド
    private JTextField urlField;
    private JTextField sizeField;
    private JLabel qrLabel;
    private int qrSize = 300;
    private Color foregroundColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    private String iconPath = null;

    // 生成されたQRコード画像を保持するための変数
    private BufferedImage currentQRCodeImage = null;

    // ダウンロード用パネル（初期状態では非表示）
    private JPanel downloadPanel;
    private JComboBox<String> downloadFormatComboBox;
    private JButton downloadButton;

    public QRCodeGeneratorUI() {
        setTitle("QRコードジェネレーター");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ── 上部パネル（入力項目等） ──
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // ■ 入力パネル（URLとサイズ）
        JPanel inputPanel = new JPanel(new FlowLayout());
        urlField = new JTextField(25);
        sizeField = new JTextField("300", 4);
        inputPanel.add(new JLabel("URL:"));
        inputPanel.add(urlField);
        inputPanel.add(new JLabel("サイズ:"));
        inputPanel.add(sizeField);
        inputPanel.add(new JLabel("pxの正方形"));

        // ■ 色選択パネル
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

        // ■ アイコン選択パネル
        JPanel filePanel = new JPanel(new FlowLayout());
        JButton iconButton = new JButton("ファイルを選択する");
        JLabel fileLabel = new JLabel("なし");
        filePanel.add(new JLabel("選択されたファイル:"));
        filePanel.add(fileLabel);
        filePanel.add(iconButton);

        // ■ アルゴリズム選択パネル
        JPanel algoPanel = new JPanel(new FlowLayout());
        // 選択肢は「通常描画」と「丸ドット描画」
        JComboBox<String> algorithmComboBox = new JComboBox<>(new String[] { "通常描画", "丸ドット描画" });
        algoPanel.add(new JLabel("描画アルゴリズム:"));
        algoPanel.add(algorithmComboBox);

        // ■ 生成ボタンパネル（各項目の下部に配置）
        JPanel generatePanel = new JPanel(new FlowLayout());
        JButton generateButton = new JButton("QRコード生成");
        generatePanel.add(generateButton);

        // 各パネルを上部パネルに追加
        topPanel.add(inputPanel);
        topPanel.add(colorPanel);
        topPanel.add(filePanel);
        topPanel.add(algoPanel);
        topPanel.add(generatePanel);

        add(topPanel, BorderLayout.NORTH);

        // ── 中央：QRコードプレビュー表示用ラベル ──
        qrLabel = new JLabel("", SwingConstants.CENTER);
        add(qrLabel, BorderLayout.CENTER);

        // ── 下部：ダウンロード用パネル（初期は非表示） ──
        downloadPanel = new JPanel(new FlowLayout());
        downloadFormatComboBox = new JComboBox<>(new String[] { "png", "jpg" });
        downloadButton = new JButton("ダウンロード");
        downloadPanel.add(new JLabel("保存形式:"));
        downloadPanel.add(downloadFormatComboBox);
        downloadPanel.add(downloadButton);
        downloadPanel.setVisible(false);
        add(downloadPanel, BorderLayout.SOUTH);

        // ── 各種イベント ──

        // 色選択ボタン
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

        // アイコン選択ボタン
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

        // 生成ボタン
        generateButton.addActionListener(e -> {
            try {
                String url = urlField.getText();
                if (url.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "URLを入力してください", "エラー", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    qrSize = Integer.parseInt(sizeField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "無効な数値が入力されました", "エラー", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int qrColor = foregroundColor.getRGB();
                int bgColor = backgroundColor.getRGB();
                BufferedImage qrImage;
                // アルゴリズム選択に応じたQRコード生成
                String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
                if ("丸ドット描画".equals(selectedAlgorithm)) {
                    // otherCircleQRCodeは内部的に25セル×25セル固定なので、1セルのサイズを計算
                    int dotSize = Math.max(1, qrSize / 30);
                    qrImage = QRCodeGenerator.otherCircleQRCode(url, dotSize, qrColor, bgColor);
                } else {
                    qrImage = QRCodeGenerator.generateQRCodeImage(url, qrSize, qrSize, qrColor, bgColor);
                }
                // アイコンが選択されている場合、重ね合わせる
                if (iconPath != null) {
                    QRCodeGenerator.addIconToQRCode(qrImage, iconPath);
                }
                // 生成したQRコード画像を保持し、プレビュー表示
                currentQRCodeImage = qrImage;
                qrLabel.setIcon(new ImageIcon(qrImage));
                // プレビュー表示後、ダウンロード用パネルを表示
                downloadPanel.setVisible(true);
                this.revalidate();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "QRコードの生成に失敗しました", "エラー", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ダウンロードボタン
        downloadButton.addActionListener(e -> {
            if (currentQRCodeImage == null) {
                JOptionPane.showMessageDialog(this, "まずQRコードを生成してください", "エラー", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String selectedFormat = (String) downloadFormatComboBox.getSelectedItem();
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    selectedFormat.toUpperCase() + "ファイル", selectedFormat);
            fileChooser.setFileFilter(filter);
            fileChooser.setSelectedFile(new File("qrcode." + selectedFormat));
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();
                String filePath = saveFile.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith("." + selectedFormat)) {
                    filePath += "." + selectedFormat;
                }
                try {
                    BufferedImage imageToSave = currentQRCodeImage;
                    // jpg形式は透過情報が扱えないため、背景色で塗りつぶし
                    if ("jpg".equalsIgnoreCase(selectedFormat)) {
                        BufferedImage rgbImage = new BufferedImage(
                                currentQRCodeImage.getWidth(),
                                currentQRCodeImage.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = rgbImage.createGraphics();
                        g2d.setColor(backgroundColor);
                        g2d.fillRect(0, 0, currentQRCodeImage.getWidth(), currentQRCodeImage.getHeight());
                        g2d.drawImage(currentQRCodeImage, 0, 0, null);
                        g2d.dispose();
                        imageToSave = rgbImage;
                    }
                    QRCodeGenerator.saveQRCodeImage(imageToSave, filePath, selectedFormat);
                    JOptionPane.showMessageDialog(this, "QRコードを保存しました:\n" + filePath, "保存完了",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "QRコードの保存に失敗しました", "エラー", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QRCodeGeneratorUI::new);
    }
}

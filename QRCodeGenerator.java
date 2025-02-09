import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public final class QRCodeGenerator {

    private static final String CHARSET = "UTF-8";
    private static final String IMAGE_FORMAT = "png";

    private QRCodeGenerator() {
        throw new UnsupportedOperationException("インスタンス化はできません");
    }

    public static BufferedImage generateQRCodeImage(String text, int width, int height, int qrColor,
            int backgroundColor) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 高い誤り訂正レベル
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? qrColor : backgroundColor);
            }
        }
        return qrImage;
    }

    public static BufferedImage addIconToQRCode(BufferedImage qrImage, String iconPath) throws IOException {
        File iconFile = new File(iconPath);
        if (!iconFile.exists()) {
            throw new IOException("アイコン画像が見つかりません: " + iconFile.getAbsolutePath());
        }
        BufferedImage icon = ImageIO.read(iconFile);
        if (icon == null) {
            throw new IOException("画像ファイルの読み込みに失敗しました: " + iconFile.getAbsolutePath());
        }

        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();
        // アイコンのサイズをQRコードの1/4に設定（後で微調整可能）
        int iconSize = qrWidth / 4;

        // アイコンのリサイズ処理
        BufferedImage scaledIcon = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledIcon.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(icon, 0, 0, iconSize, iconSize, null);
        g2.dispose();

        // QRコードの中央に配置する座標を計算
        int x = (qrWidth - iconSize) / 2;
        int y = (qrHeight - iconSize) / 2;

        // QRコードにアイコンを重ね合わせる
        Graphics2D g = qrImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.drawImage(scaledIcon, x, y, null);
        g.dispose();

        return qrImage;
    }

    public static void saveQRCodeImage(BufferedImage image, String filePath) throws IOException {
        File file = new File(filePath);
        if (!ImageIO.write(image, IMAGE_FORMAT, file)) {
            throw new IOException("画像の保存に失敗しました: " + filePath);
        }
        System.out.println("QRコードを保存しました: " + file.getAbsolutePath());
    }

    /**
     * コマンドライン引数でアイコン画像のパスを指定できるようにしたmainメソッド。
     * 引数が存在すればそのパスを、なければ"icon.png"をアイコンとして使用します。
     * 生成されたQRコードとアイコンが重なった画像は "qrcode.png" として保存されます。
     */
    public static void main(String[] args) {
        try {
            String qrText = "https://example.com";
            int qrSize = 300;
            // コマンドライン引数でアイコン画像パスを指定可能
            String iconPath = args.length > 0 ? args[0] : "icon.png";
            // 出力ファイル名を "qrcode.png" に設定
            String outputPath = "qrcode.png";

            File iconFile = new File(iconPath);
            if (!iconFile.exists()) {
                System.err.println("警告: 指定されたアイコン画像が見つかりません -> " + iconFile.getAbsolutePath());
                // アイコン無しでQRコードを生成
                BufferedImage qrImage = generateQRCodeImage(qrText, qrSize, qrSize, 0xFF000000, 0xFFFFFFFF);
                saveQRCodeImage(qrImage, outputPath);
                System.out.println("アイコンなしのQRコードを生成しました。");
            } else {
                System.out.println("アイコン画像が見つかりました: " + iconFile.getAbsolutePath());
                BufferedImage qrImage = generateQRCodeImage(qrText, qrSize, qrSize, 0xFF000000, 0xFFFFFFFF);
                BufferedImage qrWithIcon = addIconToQRCode(qrImage, iconPath);
                saveQRCodeImage(qrWithIcon, outputPath);
                System.out.println("QRコードの中央にアイコンを配置しました。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 黒セル部分を「丸ドット＋接続部分」で描画するQRコード。
     * 指定のアルゴリズムに従い、隣接する黒セルがある場合はその端も塗りつぶします。
     *
     * @param text    QRコードに埋め込む文字列
     * @param objSize 1セルを何ピクセル四方で描画するか（例：10）
     * @param qrColor ドットの色（ARGB int）
     * @param bgColor 背景色（ARGB int）
     * @return 生成されたBufferedImage
     * @throws WriterException エンコード失敗時の例外
     */
    public static BufferedImage otherCircleQRCode(String text, int objSize, int qrColor, int bgColor)
            throws WriterException {

        // エンコード設定（誤り訂正レベル Q、余白ゼロ、UTF-8）
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 25, 25, Map.of(
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q,
                EncodeHintType.MARGIN, 0,
                EncodeHintType.CHARACTER_SET, CHARSET));

        int width = matrix.getWidth();
        int height = matrix.getHeight();

        // 画像バッファ（TYPE_INT_RGBで背景色を明示的に描画）
        BufferedImage bufferedImage = new BufferedImage(width * objSize, height * objSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();

        // 背景を塗りつぶす
        g.setColor(new Color(bgColor));
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // アンチエイリアスを有効化
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(qrColor));

        // ドット描画用のサイズ（例：objSize=10なら半分の5を利用）
        int half = objSize / 2;

        // 各セルを走査して描画
        for (int hei = 0; hei < height; hei++) {
            for (int wid = 0; wid < width; wid++) {
                if (matrix.get(wid, hei)) {
                    int posX = wid * objSize;
                    int posY = hei * objSize;
                    // まず●（丸）を描画
                    g.fillArc(posX, posY, objSize, objSize, 0, 360);
                    // 左隣が黒の場合、左側を塗りつぶす
                    if (wid > 0 && matrix.get(wid - 1, hei)) {
                        g.fillRect(posX, posY, half, objSize);
                    }
                    // 右隣が黒の場合、右側を塗りつぶす
                    if (wid < width - 1 && matrix.get(wid + 1, hei)) {
                        g.fillRect(posX + half, posY, half, objSize);
                    }
                    // 上セルが黒の場合、上側を塗りつぶす
                    if (hei > 0 && matrix.get(wid, hei - 1)) {
                        g.fillRect(posX, posY, objSize, half);
                    }
                    // 下セルが黒の場合、下側を塗りつぶす
                    if (hei < height - 1 && matrix.get(wid, hei + 1)) {
                        g.fillRect(posX, posY + half, objSize, half);
                    }
                }
            }
        }
        g.dispose();
        return bufferedImage;
    }
}

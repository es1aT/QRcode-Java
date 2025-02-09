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

    /**
     * 指定のテキストをQRコードに変換し、指定サイズ・色で画像を生成する
     */
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

    /**
     * アイコン画像を読み込み、リサイズしてQRコードの中央に重ね合わせる
     */
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
        // アイコンのサイズをQRコードの1/4に設定（必要に応じて調整可）
        int iconSize = qrWidth / 4;

        // アイコンのリサイズ
        BufferedImage scaledIcon = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledIcon.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(icon, 0, 0, iconSize, iconSize, null);
        g2.dispose();

        // QRコード中央に配置
        int x = (qrWidth - iconSize) / 2;
        int y = (qrHeight - iconSize) / 2;
        Graphics2D g = qrImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.drawImage(scaledIcon, x, y, null);
        g.dispose();

        return qrImage;
    }

    /**
     * 画像を固定形式（png）で保存するメソッド
     */
    public static void saveQRCodeImage(BufferedImage image, String filePath) throws IOException {
        saveQRCodeImage(image, filePath, IMAGE_FORMAT);
    }

    /**
     * 画像形式を指定して保存するメソッド
     *
     * @param image       保存する画像
     * @param filePath    保存先のファイルパス
     * @param imageFormat 画像形式（例："png" または "jpg"）
     * @throws IOException 保存に失敗した場合の例外
     */
    public static void saveQRCodeImage(BufferedImage image, String filePath, String imageFormat) throws IOException {
        File file = new File(filePath);
        if (!ImageIO.write(image, imageFormat, file)) {
            throw new IOException("画像の保存に失敗しました: " + filePath);
        }
        System.out.println("QRコードを保存しました: " + file.getAbsolutePath());
    }

    /**
     * 黒セル部分を「丸ドット＋接続部分」で描画するQRコードを生成する
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

        // 背景を明示的に描画するためTYPE_INT_RGBを使用
        BufferedImage bufferedImage = new BufferedImage(width * objSize, height * objSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();

        // 背景色で塗りつぶす
        g.setColor(new Color(bgColor));
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // アンチエイリアスの有効化
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(qrColor));

        int half = objSize / 2;

        // 各セルを走査して描画
        for (int hei = 0; hei < height; hei++) {
            for (int wid = 0; wid < width; wid++) {
                if (matrix.get(wid, hei)) {
                    int posX = wid * objSize;
                    int posY = hei * objSize;
                    // まず丸（●）を描画
                    g.fillArc(posX, posY, objSize, objSize, 0, 360);
                    // 左側の接続部分
                    if (wid > 0 && matrix.get(wid - 1, hei)) {
                        g.fillRect(posX, posY, half, objSize);
                    }
                    // 右側の接続部分
                    if (wid < width - 1 && matrix.get(wid + 1, hei)) {
                        g.fillRect(posX + half, posY, half, objSize);
                    }
                    // 上側の接続部分
                    if (hei > 0 && matrix.get(wid, hei - 1)) {
                        g.fillRect(posX, posY, objSize, half);
                    }
                    // 下側の接続部分
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

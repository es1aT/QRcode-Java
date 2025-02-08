import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public final class QRCodeGenerator {

    private static final String CHARSET = "UTF-8";
    private static final String IMAGE_FORMAT = "png";

    // ユーティリティクラスのため、インスタンス化を防止
    private QRCodeGenerator() {
        throw new UnsupportedOperationException("インスタンス化はできません");
    }

    /**
     * 従来のシグネチャに対応するためのラッパーメソッド。
     * デフォルトのエラー訂正レベル（H）を利用してQRコード画像を生成します。
     *
     * @param text   QRコードに埋め込むテキスト
     * @param width  画像の幅
     * @param height 画像の高さ
     * @return 生成されたQRコードのBufferedImage
     * @throws WriterException エンコードに失敗した場合
     */
    public static BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        return generateQRCodeImage(text, width, height, ErrorCorrectionLevel.H);
    }

    /**
     * 指定されたテキストから、エラー訂正レベルを指定してQRコード画像を生成します。
     *
     * @param text                 QRコードに埋め込むテキスト
     * @param width                画像の幅
     * @param height               画像の高さ
     * @param errorCorrectionLevel エラー訂正レベル（例: ErrorCorrectionLevel.H）
     * @return 生成されたQRコードのBufferedImage
     * @throws WriterException エンコードに失敗した場合
     */
    public static BufferedImage generateQRCodeImage(String text, int width, int height,
            ErrorCorrectionLevel errorCorrectionLevel) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * カスタムカラー（前景色・背景色）を指定してQRコード画像を生成します。
     *
     * @param text                 埋め込むテキスト
     * @param size                 画像サイズ（正方形）
     * @param errorCorrectionLevel エラー訂正レベル（例: ErrorCorrectionLevel.H）
     * @param qrColor              QRコードの前景色（ARGB形式の整数値）
     * @param backgroundColor      背景色（ARGB形式の整数値）
     * @return 生成されたカスタマイズQRコードのBufferedImage
     * @throws WriterException エンコードに失敗した場合
     */
    public static BufferedImage generateQRCodeImage(String text, int size, ErrorCorrectionLevel errorCorrectionLevel,
            int qrColor, int backgroundColor) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        MatrixToImageConfig config = new MatrixToImageConfig(qrColor, backgroundColor);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
    }

    /**
     * 生成されたQRコード画像の中央にアイコン画像を重ね合わせます。
     *
     * @param qrImage      生成されたQRコード画像
     * @param overlayImage 中央に配置するアイコン画像
     * @return アイコンがオーバーレイされたQRコードのBufferedImage
     */
    public static BufferedImage overlayImage(BufferedImage qrImage, BufferedImage overlayImage) {
        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();
        int overlayWidth = overlayImage.getWidth();
        int overlayHeight = overlayImage.getHeight();

        // アイコンを中央に配置するための座標計算
        int x = (qrWidth - overlayWidth) / 2;
        int y = (qrHeight - overlayHeight) / 2;

        BufferedImage combined = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();
        g.drawImage(qrImage, 0, 0, null);
        g.drawImage(overlayImage, x, y, null);
        g.dispose();

        return combined;
    }

    /**
     * 生成されたQRコード画像を指定されたファイルパスに保存します。
     *
     * @param image    保存するQRコード画像
     * @param filePath 保存先のファイルパス
     * @throws IOException 保存に失敗した場合
     */
    public static void saveQRCodeImage(BufferedImage image, String filePath) throws IOException {
        File file = new File(filePath);
        if (!ImageIO.write(image, IMAGE_FORMAT, file)) {
            throw new IOException("画像の保存に失敗しました: " + filePath);
        }
        System.out.println("QRコードを保存しました: " + file.getAbsolutePath());
    }
}

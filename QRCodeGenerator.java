import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
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
     * 指定された色でQRコード画像を生成します。
     *
     * @param text            QRコードに埋め込むテキスト
     * @param width           画像の幅
     * @param height          画像の高さ
     * @param qrColor         QRコードの前景色（RGB値）
     * @param backgroundColor 背景色（RGB値）
     * @return 生成されたQRコードのBufferedImage
     * @throws WriterException QRコードのエンコードに失敗した場合
     */
    public static BufferedImage generateQRCodeImage(String text, int width, int height, int qrColor,
            int backgroundColor) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        // MatrixToImageConfigを使って前景色と背景色を適用
        MatrixToImageConfig config = new MatrixToImageConfig(qrColor, backgroundColor);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
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

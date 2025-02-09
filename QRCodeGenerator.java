import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
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

        MatrixToImageConfig config = new MatrixToImageConfig(qrColor, backgroundColor);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
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
        int iconSize = qrWidth / 5; // QRコードの1/5サイズのアイコン

        // アイコンのリサイズ
        BufferedImage scaledIcon = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledIcon.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(icon, 0, 0, iconSize, iconSize, null);
        g2.dispose();

        int x = (qrWidth - iconSize) / 2;
        int y = (qrHeight - iconSize) / 2;

        // QRコードの中央にアイコンを配置
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
     */
    public static void main(String[] args) {
        try {
            String qrText = "https://example.com";
            int qrSize = 300;
            // コマンドライン引数でアイコン画像パスを指定できる（例: java QRCodeGenerator myicon.png）
            String iconPath = args.length > 0 ? args[0] : "icon.png";
            String outputPath = "qr_with_icon.png";

            // アイコン画像の存在確認
            File iconFile = new File(iconPath);
            if (!iconFile.exists()) {
                System.err.println("警告: 指定されたアイコン画像が見つかりません -> " + iconFile.getAbsolutePath());
                // アイコン画像が見つからない場合は、アイコン無しでQRコードを生成
                BufferedImage qrImage = generateQRCodeImage(qrText, qrSize, qrSize, 0xFF000000, 0xFFFFFFFF);
                saveQRCodeImage(qrImage, outputPath);
                System.out.println("アイコンなしのQRコードを生成しました。");
            } else {
                System.out.println("アイコン画像が見つかりました: " + iconFile.getAbsolutePath());
                // QRコード生成後、中央に指定アイコンを追加
                BufferedImage qrImage = generateQRCodeImage(qrText, qrSize, qrSize, 0xFF000000, 0xFFFFFFFF);
                BufferedImage qrWithIcon = addIconToQRCode(qrImage, iconPath);
                saveQRCodeImage(qrWithIcon, outputPath);
                System.out.println("QRコードの中央にアイコンを配置しました。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

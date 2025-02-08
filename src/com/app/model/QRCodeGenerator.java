package com.example.model;

import java.awt.image.BufferedImage;
import com.google.zxing.*;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public static BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // エラー訂正レベルをHに設定（中央にアイコンを配置する場合、隠れる部分を補完するため）
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int qrColor = 0xFF000000; // 前景色（黒）
        int bgColor = 0xFFFFFFFF; // 背景色（白）

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? qrColor : bgColor);
            }
        }
        return image;
    }
}

package com.example.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageUtil {
    public static BufferedImage addCenterIcon(BufferedImage qrImage, BufferedImage iconImage) {
        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();

        // アイコンのサイズはQRコードの約20%程度に設定（必要に応じて調整してください）
        int iconWidth = qrWidth / 5;
        int iconHeight = qrHeight / 5;
        int x = (qrWidth - iconWidth) / 2;
        int y = (qrHeight - iconHeight) / 2;

        Graphics2D g = qrImage.createGraphics();
        g.drawImage(iconImage, x, y, iconWidth, iconHeight, null);
        g.dispose();
        return qrImage;
    }
}

package com.zoopick.server.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zoopick.server.exception.InternalServerException;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@NullMarked
public class QrCodeGenerator {
    private static final int QR_SIZE = 200;
    private static final int TOP_PADDING = 16;
    private static final int BOTTOM_PADDING = 16;
    private static final int SIDE_PADDING = 16;
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final String TITLE = "Zoopick";

    public byte[] generate(String content) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            BufferedImage image = createImageWithTitle(qrImage);

            ImageIO.write(image, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException exception) {
            throw new InternalServerException("Failed to encode QR code image", exception);
        } catch (IOException exception) {
            throw new InternalServerException("Failed to write QR code image", exception);
        }
    }

    private BufferedImage createImageWithTitle(BufferedImage qrImage) {
        FontMetrics fontMetrics = createFontMetrics();
        int titleHeight = fontMetrics.getAscent() + fontMetrics.getDescent();
        int gapBetweenTitleAndQr = 12;

        BufferedImage canvas = new BufferedImage(
                qrImage.getWidth() + (SIDE_PADDING * 2),
                qrImage.getHeight() + TOP_PADDING + titleHeight + gapBetweenTitleAndQr + BOTTOM_PADDING,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            graphics.setColor(Color.BLACK);
            graphics.setFont(TITLE_FONT);

            int titleX = (canvas.getWidth() - fontMetrics.stringWidth(TITLE)) / 2;
            int titleY = TOP_PADDING + fontMetrics.getAscent();
            graphics.drawString(TITLE, titleX, titleY);

            int qrY = TOP_PADDING + titleHeight + gapBetweenTitleAndQr;
            graphics.drawImage(qrImage, SIDE_PADDING, qrY, null);
        } finally {
            graphics.dispose();
        }

        return canvas;
    }

    private FontMetrics createFontMetrics() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setFont(TITLE_FONT);
            return graphics.getFontMetrics();
        } finally {
            graphics.dispose();
        }
    }
}

package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class QRCodeGenerator {

    public static Image generateQRCodeImage(String text, int width, int height) throws WriterException {
        // 1. Créer le bit-matrix grâce à ZXing
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        // 2. Convertir en BufferedImage
        BufferedImage buffered = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // 3. Convertir en Image JavaFX
        return SwingFXUtils.toFXImage(buffered, null);
    }
}

package steganography;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class SteganographyDecoder {

    // Allowed formats
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("png", "gif", "bmp", "tif", "tiff");
    // Unsafe formats
    private static final List<String> UNSAFE_FORMATS = Arrays.asList("jpg", "jpeg", "webp", "heic", "heif");

    // Decode message and validate password
    public static String decode(String imagePath, String password) throws IOException {
        File inputFile = new File(imagePath);
        String formatName = getFileExtension(imagePath);

        if (formatName == null) {
            JOptionPane.showMessageDialog(null,
                    "Cannot detect image format.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        formatName = formatName.toLowerCase();

        // Check if format is allowed
        if (!ALLOWED_FORMATS.contains(formatName)) {
            JOptionPane.showMessageDialog(null,
                    "This image format (" + formatName + ") is not supported for steganography.\n" +
                            "Use PNG, GIF, BMP, or uncompressed TIFF.",
                    "Unsupported Format", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // Load image
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) throw new IOException("Cannot read image: " + imagePath);

        // Read first 4 pixels to get message length
        int msgLength = 0;
        for (int i = 0; i < 4; i++) {
            int rgb = image.getRGB(i, 0);
            int value = rgb & 0xFF;
            msgLength = (msgLength << 8) | value;
        }

        byte[] msgBytes = new byte[msgLength];
        int msgIndex = 0;

        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = (y == 0 ? 4 : 0); x < image.getWidth(); x++) {
                if (msgIndex >= msgLength) break outer;
                int rgb = image.getRGB(x, y);
                msgBytes[msgIndex++] = (byte) (rgb & 0xFF);
            }
        }

        String combined = new String(msgBytes, StandardCharsets.UTF_8);

        // Split password and message
        String[] parts = combined.split("::", 2);
        if (parts.length < 2) {
            throw new IOException("Invalid encoded message format.");
        }

        String storedPassword = parts[0];
        String message = parts[1];

        if (!storedPassword.equals(password)) {
            throw new IOException("Incorrect password.");
        }

        return message;
    }

    // Helper to get file extension
    private static String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < path.length() - 1) {
            return path.substring(dotIndex + 1);
        }
        return null;
    }
}

package steganography;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class SteganographyEncoder {

    // Allowed formats
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("png", "gif", "bmp", "tif", "tiff");
    // Unsafe formats
    private static final List<String> UNSAFE_FORMATS = Arrays.asList("jpg", "jpeg", "webp", "heic", "heif");

    // Encode message with password
    public static void encode(String message, String password, String imagePath, String outputPath) throws IOException {
        File inputFile = new File(imagePath);
        String formatName = getFileExtension(imagePath);

        if (formatName == null) {
            JOptionPane.showMessageDialog(null,
                    "Cannot detect image format.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        formatName = formatName.toLowerCase();

        // Check if format is allowed
        if (!ALLOWED_FORMATS.contains(formatName)) {
            JOptionPane.showMessageDialog(null,
                    "This image format (" + formatName + ") is not supported for steganography.\n" +
                            "Use PNG, GIF, BMP, or uncompressed TIFF.",
                    "Unsupported Format", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Combine password and message
        String combined = password + "::" + message;

        // Load original image
        BufferedImage src = ImageIO.read(inputFile);
        if (src == null) throw new IOException("Cannot read image: " + imagePath);

        // Create ARGB image to preserve pixel data
        BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(src, 0, 0, null);

        byte[] msgBytes = combined.getBytes(StandardCharsets.UTF_8);
        int msgLength = msgBytes.length;

        if (image.getWidth() * image.getHeight() < msgLength + 4) {
            throw new IllegalArgumentException("Image too small to encode this message.");
        }

        // Store message length in first 4 pixels
        for (int i = 0; i < 4; i++) {
            int value = (msgLength >> (24 - i * 8)) & 0xFF;
            int rgb = image.getRGB(i, 0);
            rgb = (rgb & 0xFFFFFF00) | value;
            image.setRGB(i, 0, rgb);
        }

        // Write message bytes into low byte of each pixel starting at (4,0)
        int msgIndex = 0;
        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = (y == 0 ? 4 : 0); x < image.getWidth(); x++) {
                if (msgIndex >= msgBytes.length) break outer;
                int rgb = image.getRGB(x, y);
                int newRgb = (rgb & 0xFFFFFF00) | (msgBytes[msgIndex++] & 0xFF);
                image.setRGB(x, y, newRgb);
            }
        }

        // Save encoded image in same format
        File outFile = new File(outputPath);
        if (!outFile.getName().toLowerCase().endsWith("." + formatName)) {
            outFile = new File(outFile.getAbsolutePath() + "." + formatName);
        }

        if (!ImageIO.write(image, formatName, outFile)) {
            throw new IOException("Failed to save encoded image in format: " + formatName);
        }

        JOptionPane.showMessageDialog(null,
                "Message encoded successfully into: " + outFile.getAbsolutePath(),
                "Success", JOptionPane.INFORMATION_MESSAGE);

        System.out.println("Message encoded into: " + outFile.getAbsolutePath());
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

/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import gtanks.utils.ResourceUtils;
import javax.imageio.ImageIO;

public class Captcha {
    private static final ArrayList<BufferedImage> bufferedImages = new ArrayList();
    private static final Color[] colorsText = new Color[]{Color.BLACK};

    public static void loadCatches() {
        for (File file : Objects.requireNonNull(new File(ResourceUtils.data("captcha/img")).listFiles())) {
            try {
                bufferedImages.add(ImageIO.read(file));
            } catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public static int getRandomIni(int min, int max) {
        max -= min;
        return (int)(Math.random() * (double)(++max)) + min;
    }

    private static BufferedImage deepCopy(BufferedImage bufferedImage) {
        ColorModel colorModel = bufferedImage.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = bufferedImage.copyData(null);
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    public static byte[] getCaptcha(String text) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try {
            BufferedImage image = Captcha.deepCopy(bufferedImages.get(Captcha.getRandomIni(0, bufferedImages.size() - 1)));
            Graphics2D g = (Graphics2D)image.getGraphics();
            File fontFile = new File(ResourceUtils.data("captcha/font/Khula-Regular.ttf"));
            Font khulaFont = Font.createFont(0, fontFile).deriveFont(0, Captcha.getRandomIni(45, 65));
            g.setFont(khulaFont);
            double angle = Math.toRadians(Captcha.getRandomIni(-1, 1));
            AffineTransform transform = new AffineTransform();
            transform.rotate(angle, image.getWidth() / 2, image.getHeight() / 2);
            g.setTransform(transform);
            int length = text.length() * Captcha.getRandomIni(13, 14);
            for (char c : text.toCharArray()) {
                g.setColor(Color.BLACK);
                g.drawString(Character.toString(c), length += Captcha.getRandomIni(17, 21), 40);
            }
            int stickLength = Captcha.getRandomIni(200, 200);
            int stickWidth = Captcha.getRandomIni(3, 3);
            int x1 = Captcha.getRandomIni(0, image.getWidth() - stickLength);
            int y1 = Captcha.getRandomIni(0, image.getHeight() - stickWidth);
            int x2 = x1 + stickLength + Captcha.getRandomIni(200, 200);
            int y2 = Captcha.getRandomIni(0, image.getHeight() - stickWidth);
            if (x2 + stickLength > image.getWidth()) {
                x2 = Captcha.getRandomIni(0, image.getWidth() - stickLength);
            }
            double stick1Angle = Math.toRadians(Captcha.getRandomIni(-5, 5));
            double stick2Angle = Math.toRadians(Captcha.getRandomIni(-5, 5));
            AffineTransform stick1Transform = new AffineTransform();
            stick1Transform.rotate(stick1Angle, x1, y1 + stickWidth / 2);
            g.setTransform(stick1Transform);
            g.fillRect(x1, y1, stickLength, stickWidth);
            AffineTransform stick2Transform = new AffineTransform();
            stick2Transform.rotate(stick2Angle, x2, y2 + stickWidth / 2);
            g.setTransform(stick2Transform);
            g.fillRect(x2, y2, stickLength, stickWidth);
            g.setTransform(new AffineTransform());
            g.setColor(Color.BLACK);
            g.dispose();
            ImageIO.write((RenderedImage)image, "png", byteArray);
        } catch (IOException e) {
            return null;
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        }
        return byteArray.toByteArray();
    }
}


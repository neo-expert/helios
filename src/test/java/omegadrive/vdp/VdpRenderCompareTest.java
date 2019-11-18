/*
 * VdpRenderTest
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 11/10/19 14:30
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package omegadrive.vdp;

import omegadrive.automated.SavestateGameLoader;
import omegadrive.util.FileUtil;
import omegadrive.util.ImageUtil;
import omegadrive.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Ignore
public class VdpRenderCompareTest extends VdpRenderTest {

    private static final boolean SHOW_IMAGES_ON_FAILURE = true;
    public static String IMG_EXT = "bmp";
    public static String DOT_EXT = "." + IMG_EXT + ".zip";
    private static Path compareFolderPath = Paths.get(saveStateFolder, "compare");
    protected static String compareFolder = compareFolderPath.toAbsolutePath().toString();
    private BufferedImage diffImage;

    public static BufferedImage convertToBufferedImage(Image image) {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public static void main(String[] args) {
        File[] files = compareFolderPath.toFile().listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            System.out.println("Testing: " + file);
            Image img = ImageUtil.loadImageFromFile(file);
            Path zipFilePath = FileUtil.compressAndSaveToZipFile(file.toPath(), img, "bmp");
            System.out.println("Written: " + zipFilePath);
        }
    }

    @Before
    public void beforeTest() {
        System.setProperty("helios.headless", "true");
        System.setProperty("md.show.vdp.debug.viewer", "false");
    }

    @Test
    public void testCompareAll() {
        File[] files = Paths.get(saveStateFolder).toFile().listFiles();
        boolean showingFailures = false;
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            System.out.println("Testing: " + file);
            boolean res = testCompareOne(file.getName());
            if (res) {
                System.out.println("Error: " + file);
            }
            showingFailures |= res;
        }
        if (showingFailures) {
            Util.waitForever();
        }
    }

    @Test
    public void testCompare() {
        boolean overwrite = false;
        String name = "mickeym";
        if (overwrite) {
            testOverwriteBaselineImage(name + ".gs0");
        }
        boolean showingFailures = testCompareOne(name + ".gs0");
        if (showingFailures) {
            Util.waitForever();
        }
        Util.waitForever();
    }

    private boolean compareImage(BufferedImage baseline, BufferedImage actual) {
        boolean ok = true;
        try {
            Dimension d1 = baseline.getData().getBounds().getSize();
            Dimension d2 = actual.getData().getBounds().getSize();

            Assert.assertEquals("Image size doesn't match", d1, d2);

            diffImage = convertToBufferedImage(baseline);
            for (int i = 0; i < d1.width; i++) {
                for (int j = 0; j < d1.height; j++) {
                    int r1 = baseline.getRGB(i, j);
                    int r2 = actual.getRGB(i, j);
                    double ratio = 1.0 * r1 / r2;
                    if (ratio - 1.0 != 0.0) {
//                        System.out.println(i + "," + j + ": " + r1 + "," + r2 + "," + ratio);
                    }
                    diffImage.setRGB(i, j, 0xFF_FF_FF - Math.abs(r1 - r2));
                    ok &= Math.abs(1.0 - ratio) < 0.01;
                }
            }
        } catch (AssertionError ae) {
            ae.printStackTrace();
            ok = false;
        }
        return ok;
    }

    private void testOverwriteBaselineImage(String saveName) {
        Path saveFile = Paths.get(saveStateFolder, saveName);
        Image i = testSavestateViewerSingle(saveFile, SavestateGameLoader.saveStates.get(saveName));
        saveToFile(saveName, i);
    }

    private boolean testCompareOne(String saveName) {
        Path saveFile = Paths.get(saveStateFolder, saveName);
        Path baselineZipImageFile = Paths.get(compareFolder, saveName + DOT_EXT);
        Image i = testSavestateViewerSingle(saveFile, SavestateGameLoader.saveStates.get(saveName));
        BufferedImage actual = convertToBufferedImage(i);
        Image base = FileUtil.decompressAndLoadFromZipFile(baselineZipImageFile, saveName + "." + IMG_EXT, IMG_EXT);
        BufferedImage baseLine = convertToBufferedImage(base);
        boolean match = compareImage(baseLine, actual);
        if (!match) {
            if (SHOW_IMAGES_ON_FAILURE) {
                JFrame f1 = showImageFrame(scaleImage(baseLine, 4), "BASELINE_" + saveName + DOT_EXT);
                JFrame f2 = showImageFrame(scaleImage(actual, 4), saveName);
                JFrame f3 = showImageFrame(scaleImage(diffImage, 4), "DIFF_" + saveName + " (Diffs are non white pixels)");
                return true;
            }
        }
        return false;
    }

    private void saveToFile(String saveName, Image i) {
        Path folder = Paths.get(compareFolder);
        Path res = FileUtil.compressAndSaveToZipFile(saveName + "." + IMG_EXT, folder, i, IMG_EXT);
        System.out.println("Image saved: " + res.toAbsolutePath().toString());
    }
}

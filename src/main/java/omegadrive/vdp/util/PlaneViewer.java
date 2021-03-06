package omegadrive.vdp.util;

import omegadrive.util.ImageUtil;
import omegadrive.vdp.VdpRenderDump;
import omegadrive.vdp.model.GenesisVdpProvider;
import omegadrive.vdp.model.RenderType;
import omegadrive.vdp.model.VdpMemoryInterface;
import omegadrive.vdp.model.VdpRenderHandler;
import omegadrive.LogManager;
import omegadrive.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * PlaneViewer
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2019
 */
public class PlaneViewer implements UpdatableViewer {

    private static final Logger LOG = LogManager.getLogger(PlaneViewer.class.getSimpleName());

    private static final int PANEL_TEXT_HEIGHT = 20;
    private static final int PANEL_HEIGHT = 256 + PANEL_TEXT_HEIGHT;
    private static final int PANEL_WIDTH = 320;

    private static final Dimension layerDim = new Dimension(320, 256);

    private VdpRenderHandler renderHandler;
    private static final int CRAM_MASK = GenesisVdpProvider.VDP_CRAM_SIZE - 1;
    private JPanel panel;
    private JFrame frame;
    private JPanel[] panelList = new JPanel[RenderType.values().length];
    private BufferedImage[] imageList = new BufferedImage[RenderType.values().length];
    private int[] javaPalette;
    private Dimension fullFrameDimension = layerDim;
    private int[] colorData = new int[0];

    private PlaneViewer(VdpMemoryInterface memoryInterface, VdpRenderHandler renderHandler) {
        this.renderHandler = renderHandler;
        this.frame = new JFrame();
        this.panel = new JPanel();
        this.javaPalette = memoryInterface.getJavaColorPalette();
        initPanel();
    }

    public static PlaneViewer createInstance(VdpMemoryInterface memoryInterface, VdpRenderHandler renderHandler) {
        return new PlaneViewer(memoryInterface, renderHandler);
    }

    private static int[] getPixels(BufferedImage img) {
        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }

    private void initFrame() {
        int numCols = 3;
        int numRows = 2;
        initPanel();
        SwingUtilities.invokeLater(() -> {
            this.frame = new JFrame();
            frame.add(panel);
            frame.setMinimumSize(new Dimension(PANEL_WIDTH * numCols, PANEL_HEIGHT * numRows));
            frame.setTitle("Plane Viewer");
            frame.pack();
            frame.setVisible(true);
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    private void initPanel() {
        int numCols = 3;
        int numRows = 2;
        SwingUtilities.invokeLater(() -> {
            this.frame = new JFrame();
            this.panel = new JPanel(new GridLayout(numRows, numCols));
            int k = 0;
            for (RenderType type : RenderType.values()) {
                JPanel cpanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (g instanceof Graphics2D) {
                            Graphics2D g2 = (Graphics2D) g;
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                            g2.drawString(type.name(), 70, 10);
                            g2.drawImage(imageList[type.ordinal()], 0, PANEL_TEXT_HEIGHT, this);
                        }
                    }
                };
                cpanel.setBackground(Color.BLACK);
                cpanel.setForeground(Color.WHITE);
                cpanel.setName(type.name());
                cpanel.setSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
                panelList[k] = cpanel;
                k++;
            }
            Arrays.stream(panelList).forEach(panel::add);
            panel.setSize(new Dimension(PANEL_WIDTH * numCols, PANEL_HEIGHT * numRows));
        });
        Arrays.stream(RenderType.values()).forEach(t -> imageList[t.ordinal()] = ImageUtil.createImage(VdpRenderDump.gd, layerDim));
    }

    @Override
    public void update() {
        adjustFullFrameSize();
        BufferedImage img = imageList[RenderType.FULL.ordinal()];
        int[] imgData = getPixels(img);
        int[] data = renderHandler.getScreenDataLinear();
        System.arraycopy(data, 0, imgData, 0, data.length);
        panel.repaint();
    }

    private void adjustFullFrameSize() {
        Dimension d = renderHandler.getVideoMode().getDimension();
        if (!d.equals(fullFrameDimension)) {
            imageList[RenderType.FULL.ordinal()] = ImageUtil.createImage(VdpRenderDump.gd, d);
            fullFrameDimension = d;
        }
    }

    @Override
    public void updateLine(int line) {
        if (line >= layerDim.height) {
            return;
        }
        int pos = layerDim.width * line;

        for (RenderType type : RenderType.values()) {
            if (type == RenderType.FULL) {
                continue;
            }
            int[] data = renderHandler.getPlaneData(type);
            if (colorData.length != data.length) {
                colorData = new int[data.length];
            }
            toJavaColor(data, colorData);
            BufferedImage img = imageList[type.ordinal()];
            int[] imgData = getPixels(img);
            System.arraycopy(colorData, 0, imgData, pos, colorData.length);
        }
    }

    private int[] toJavaColor(final int[] in, int[] out) {
        for (int i = 0; i < in.length; i++) {
            out[i] = javaPalette[(in[i] & CRAM_MASK) >> 1];
        }
        return out;
    }
}

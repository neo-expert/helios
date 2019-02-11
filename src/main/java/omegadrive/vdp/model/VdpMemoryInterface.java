package omegadrive.vdp.model;

import omegadrive.util.Size;
import omegadrive.vdp.VdpProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ${FILE}
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2018
 */
public interface VdpMemoryInterface {

    Logger LOG = LogManager.getLogger(VdpMemoryInterface.class.getSimpleName());

    void writeVramByte(int address, int data);

    void writeVsramByte(int address, int data);

    void writeCramByte(int address, int data);

    int readVramByte(int address);

    int readCramByte(int address);

    int readVsramByte(int address);

    int readVramWord(int address);

    int readCramWord(int address);

    int readVsramWord(int address);

    void writeVideoRamWord(VdpProvider.VdpRamType vramType, int data, int address);

    int[] getCram();

    int[] getVram();

    default int readVideoRamWord(VdpProvider.VdpRamType vramType, int address) {
        switch (vramType) {
            case VRAM:
                return readVramWord(address);
            case VSRAM:
                return readVsramWord(address);
            case CRAM:
                return readCramWord(address);
            default:
                LOG.warn("Unexpected videoRam read: " + vramType);
        }
        return 0;
    }

    default void writeVideoRamWord(VdpProvider.VramMode mode, int data, int address) {
        if (mode == null) {
            LOG.warn("writeDataPort when vramMode is not set, address {} , size {}", address, Size.WORD);
            return;
        }
        writeVideoRamWord(mode.getRamType(), data, address);
    }

    default int readVideoRamWord(VdpProvider.VramMode mode, int address) {
        if (mode == null) {
            LOG.warn("readDataPort when vramMode is not set, address {} , size {}", address, Size.WORD);
            return 0;
        }
        return readVideoRamWord(mode.getRamType(), address);
    }

}

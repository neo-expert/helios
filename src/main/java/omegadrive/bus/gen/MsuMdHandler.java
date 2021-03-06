package omegadrive.bus.gen;

import omegadrive.util.Size;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.util.Optional;
import omegadrive.LogManager;
import omegadrive.Logger;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public interface MsuMdHandler {

    Logger LOG = LogManager.getLogger(GenesisBus.class.getSimpleName());

    int CLOCK_ADDR = 0xa1201f;
    int CMD_ADDR = 0xa12010;
    int CMD_ARG_ADDR = CMD_ADDR + 1;
    int MCD_STATUS_ADDR = 0xA12020;

    int MCD_GATE_ARRAY_START = 0xa12001;

    AudioFormat CDDA_FORMAT = new AudioFormat(44100f,
            16, 2, true, false);

    MsuMdHandler NO_OP_HANDLER = new MsuMdHandler() {
        @Override
        public int handleMsuMdRead(int address, Size size) {
            return 0;
        }

        @Override
        public void handleMsuMdWrite(int address, int data, Size size) {
            //Do nothing
        }
    };

    int handleMsuMdRead(int address, Size size);

    void handleMsuMdWrite(int address, int data, Size size);

    default void close() {
        //do nothing
    }

    enum MsuCommand {
        PLAY(0x11),
        PLAY_LOOP(0x12),
        PAUSE(0x13),
        RESUME(0x14),
        VOL(0x15);

        private int val;

        MsuCommand(int val) {
            this.val = val;
        }

        public static MsuCommand getMsuCommand(int val) {
            for (MsuCommand c : MsuCommand.values()) {
                if (c.val == val) {
                    return c;
                }
            }
            return null;
        }
    }

    enum CueFileDataType {
        BINARY,
        WAVE,
        OGG,
        UNKNOWN;

        static MsuMdHandlerImpl.CueFileDataType getFileType(String type) {
            for (MsuMdHandlerImpl.CueFileDataType c : MsuMdHandlerImpl.CueFileDataType.values()) {
                if (c.name().equalsIgnoreCase(type)) {
                    return c;
                }
            }
            return UNKNOWN;
        }
    }

    class MsuCommandArg {
        MsuCommand command;
        int arg;
    }

    class TrackDataHolder {
        MsuMdHandlerImpl.CueFileDataType type;
        Optional<File> waveFile = Optional.empty();
        Optional<Integer> numBytes = Optional.empty();
        Optional<Integer> startFrame = Optional.empty();
    }
}

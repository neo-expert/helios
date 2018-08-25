package omegadrive.vdp;

import omegadrive.Genesis;
import omegadrive.util.VideoMode;
import omegadrive.vdp.model.VdpHLineProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

/**
 * ${FILE}
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2018
 * <p>
 */
public class VdpInterruptHandler {

    /**
     * Relevant Games:
     * Kawasaki
     * Outrun
     * Gunstar Heroes
     * Lotus II
     */
    private static Logger LOG = LogManager.getLogger(VdpInterruptHandler.class.getSimpleName());

    public static int PAL_SCANLINES = 313;
    public static int NTSC_SCANLINES = 262;
    public static int H32_PIXELS = 342;
    public static int H40_PIXELS = 422;
    public static int COUNTER_LIMIT = 0x1FF;

    private int hCounterInternal;
    private int vCounterInternal = 0;
    private int hLinePassed = 0;
    private int baseHLinePassed = 0;

    private VideoMode videoMode;
    private VdpCounterMode vdpCounterMode;
    private VdpHLineProvider vdpHLineProvider;

    private boolean vBlankSet;
    private boolean hBlankSet;
    private boolean vIntPending;
    private boolean hIntPending;

    private static boolean verbose = Genesis.verbose && false;


    enum VdpCounterMode {
        PAL_H32_V28(VideoMode.PAL_H32_V28,
                H32_PIXELS, 296,  //hcount, hjumptrigger
                PAL_SCANLINES, 259, //vcount, vjumptrigger
                0x93 << 1, 0x05 << 1, //hblankset, hblankclear
                0xE0, 0x85 << 1  //vblankset, vCounterIncrementOn
        ),
        PAL_H32_V30(VideoMode.PAL_H32_V30,
                H32_PIXELS, 296,
                PAL_SCANLINES, 267,
                0x93 << 1, 0x05 << 1, 0xF0, 0x85 << 1
        ),
        PAL_H40_V28(VideoMode.PAL_H40_V28,
                H40_PIXELS, 366,
                PAL_SCANLINES, 259,
                0xB3 << 1, 0x06 << 1, 0xE0, 0xA5 << 1
        ),
        PAL_H40_V30(VideoMode.PAL_H40_V30,
                H40_PIXELS, 366,
                PAL_SCANLINES, 267,
                0xB3 << 1, 0x06 << 1, 0xF0, 0xA5 << 1
        ),
        NTSCJ_H32_V28(VideoMode.NTSCJ_H32_V28,
                H32_PIXELS, 296,
                NTSC_SCANLINES, 235,
                0x93 << 1, 0x05 << 1, 0xE0, 0x85 << 1
        ),
        NTSCU_H32_V28(VideoMode.NTSCU_H32_V28,
                H32_PIXELS, 296,
                NTSC_SCANLINES, 235,
                0x93 << 1, 0x05 << 1, 0xE0, 0x85 << 1
        ),

        NTSCJ_H32_V30(VideoMode.NTSCJ_H32_V30,
                H32_PIXELS, 296,
                NTSC_SCANLINES, -1,
                0x93 << 1, 0x05 << 1, 0xF0, 0x85 << 1
        ),
        NTSCU_H32_V30(VideoMode.NTSCU_H32_V30,
                H32_PIXELS, 296,
                NTSC_SCANLINES, -1,
                0x93 << 1, 0x05 << 1, 0xF0, 0x85 << 1
        ),

        NTSCJ_H40_V28(VideoMode.NTSCJ_H40_V28,
                H40_PIXELS, 366,
                NTSC_SCANLINES, 235,
                0xB3 << 1, 0x06 << 1, 0xE0, 0xA5 << 1
        ),
        NTSCU_H40_V28(VideoMode.NTSCU_H40_V28,
                H40_PIXELS, 366,
                NTSC_SCANLINES, 235,
                0xB3 << 1, 0x06 << 1, 0xE0, 0xA5 << 1
        ),

        NTSCJ_H40_V30(VideoMode.NTSCJ_H40_V30,
                H40_PIXELS, 366,
                NTSC_SCANLINES, -1,
                0xB3 << 1, 0x06 << 1, 0xF0, 0xA5 << 1
        ),
        NTSCU_H40_V30(VideoMode.NTSCU_H40_V30,
                H40_PIXELS, 366,
                NTSC_SCANLINES, -1,
                0xB3 << 1, 0x06 << 1, 0xF0, 0xA5 << 1
        ),;

        private static EnumSet<VdpCounterMode> values = EnumSet.allOf(VdpCounterMode.class);

        int hTotalCount;
        int hJumpTrigger;
        int hBlankSet;
        int hBlankClear;
        int vTotalCount;
        int vJumpTrigger;
        int vBlankSet;
        int vCounterIncrementOn;
        VideoMode videoMode;

        VdpCounterMode(VideoMode videoMode,
                       int hTotalCount, int hJumpTrigger,
                       int vTotalCount, int vJumpTrigger,
                       int hBlankSet, int hBlankClear,
                       int vBlankSet, int vCounterIncrementOn) {
            this.hTotalCount = hTotalCount;
            this.hJumpTrigger = hJumpTrigger;
            this.hBlankSet = hBlankSet;
            this.hBlankClear = hBlankClear;
            this.vTotalCount = vTotalCount;
            this.vJumpTrigger = vJumpTrigger;
            this.vBlankSet = vBlankSet;
            this.videoMode = videoMode;
            this.vCounterIncrementOn = vCounterIncrementOn;
        }

        public static VdpCounterMode getCounterMode(VideoMode videoMode) {
            for (VdpCounterMode v : VdpCounterMode.values) {
                if (v.videoMode == videoMode) {
                    return v;
                }
            }
            LOG.error("Unable to find counter mode for videoMode: " + videoMode);
            return null;
        }
    }

    public static VdpInterruptHandler createInstance(VdpHLineProvider vdpHLineProvider) {
        VdpInterruptHandler handler = new VdpInterruptHandler();
        handler.vdpHLineProvider = vdpHLineProvider;
        return handler;
    }

    public void setMode(VideoMode videoMode) {
        if (this.videoMode != videoMode) {
            this.videoMode = videoMode;
            this.vdpCounterMode = VdpCounterMode.getCounterMode(videoMode);
            reset();
        }
    }

    private void reset() {
        hCounterInternal = vCounterInternal = 0;
        hBlankSet = false;
        vBlankSet = false;
        vIntPending = false;
    }

    private int updateCounterValue(int counterInternal, int jumpTrigger, int totalCount) {
        counterInternal++;
        counterInternal &= COUNTER_LIMIT;

        if (counterInternal == jumpTrigger) {
            counterInternal = 1 + COUNTER_LIMIT +
                    jumpTrigger - totalCount;
        }
        return counterInternal;
    }

    private int increaseVCounter() {
        return increaseVCounterInternal();
    }

    private int increaseVCounterInternal() {

        vCounterInternal = updateCounterValue(vCounterInternal, vdpCounterMode.vJumpTrigger,
                vdpCounterMode.vTotalCount);

        if (vCounterInternal == vdpCounterMode.vBlankSet) {
            vBlankSet = true;
        }
        if (vCounterInternal == COUNTER_LIMIT) {
            vBlankSet = false;
        }
        return vCounterInternal;
    }

    public int increaseHCounter() {
        return increaseHCounterInternal();
    }

    private int increaseHCounterInternal() {
        hCounterInternal = updateCounterValue(hCounterInternal, vdpCounterMode.hJumpTrigger,
                vdpCounterMode.hTotalCount);
        handleHLinesCounter();
        if (hCounterInternal == vdpCounterMode.hBlankSet) {
            hBlankSet = true;
        }

        if (hCounterInternal == vdpCounterMode.hBlankClear) {
            hBlankSet = false;
        }

        if (hCounterInternal == vdpCounterMode.vCounterIncrementOn) {
            increaseVCounter();
        }
        if (hCounterInternal == 0x02 && vCounterInternal == vdpCounterMode.vBlankSet) {
            vIntPending = true;
            printState("Set VIP: true");
        }
        return hCounterInternal;
    }

    private void handleHLinesCounter() {
        //Vcounter is incremented just before HINT pending flag is set,
        if (hCounterInternal == vdpCounterMode.vCounterIncrementOn + 2) {
            //it is decremented on each lines between line 0 and line $E0
            if (vCounterInternal <= vdpCounterMode.vBlankSet) {
                hLinePassed--;
            }
            boolean isValidVCounterForHip = vCounterInternal > 0x00; //Lotus II
            boolean triggerHip = isValidVCounterForHip && hLinePassed == -1; //aka triggerHippy
            if (triggerHip) {
                hIntPending = true;
                printState("Set HIP: true, hLinePassed: %s", hLinePassed);
            }
            //reload on line = 0 and vblank
            boolean isForceResetVCounter = vCounterInternal == 0x00 || vCounterInternal > vdpCounterMode.vBlankSet;
            if (isForceResetVCounter || triggerHip) {
                resetHLinesCounter(vdpHLineProvider.getHLinesCounter());
            }
        }
    }

    public boolean isvBlankSet() {
        return vBlankSet;
    }

    public boolean ishBlankSet() {
        return hBlankSet;
    }

    public int getvCounter() {
        return vCounterInternal;
    }

    public int getVCounterExternal() {
        return vCounterInternal & 0xFF;
    }

    public int getHCounterExternal() {
        return (hCounterInternal >> 1) & 0xFF;
    }

    public boolean isvIntPending() {
        return vIntPending;
    }

    public void setvIntPending(boolean vIntPending) {
        this.vIntPending = vIntPending;
        printState("Set VIP: %s", vIntPending);
    }

    public boolean isHIntPending() {
        return hIntPending;
    }

    public void setHIntPending(boolean hIntPending) {
        printState("Set HIP: %s, hLinePassed: %s", hIntPending, hLinePassed);
        this.hIntPending = hIntPending;
    }

    public boolean isLastHCounter() {
        return hCounterInternal == COUNTER_LIMIT;
    }

    public boolean isDrawFrameCounter() {
        return isLastHCounter() && vCounterInternal == COUNTER_LIMIT;
    }

    public void resetHLinesCounter(int value) {
        this.hLinePassed = value;
        this.baseHLinePassed = value;
        printState("Reset hLinePassed: %s", value);
    }

    public static void main(String[] args) {
        VdpInterruptHandler.verbose = true;
        VdpInterruptHandler h = createInstance(() -> {
            return 0;
        });
        h.setMode(VideoMode.NTSCJ_H32_V30);
        do {
            h.increaseHCounter();
            h.printState("", null);
        } while (!h.isDrawFrameCounter());

    }

    public void printState(String str, Object... args) {
        if (verbose && LOG.isEnabled(Level.INFO)) {
            printStateString(String.format(str, args));
        }
    }


    private void printStateString(String head) {
        LOG.info(head + ", hce=" + Integer.toHexString((hCounterInternal >> 1) & 0xFF) +
                "(" + Integer.toHexString(this.hCounterInternal) + "), vce=" + Integer.toHexString(vCounterInternal & 0xFF)
                + "(" + Integer.toHexString(this.vCounterInternal) + ")" + ", hBlankSet=" + hBlankSet + ",vBlankSet=" + vBlankSet
                + ", vIntPending=" + vIntPending
        );
    }
}

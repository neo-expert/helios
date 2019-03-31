package omegadrive.bus.gen;

import omegadrive.system.SystemProvider;
import omegadrive.m68k.M68kProvider;
import omegadrive.m68k.MC68000Wrapper;
import omegadrive.vdp.VdpTestUtil;
import omegadrive.vdp.model.GenesisVdpProvider;
import omegadrive.vdp.model.VdpCounterMode;
import omegadrive.z80.Z80CoreWrapper;
import omegadrive.z80.Z80Provider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ${FILE}
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2018
 */
public class BusArbiterTest {

    private boolean verbose = false;
    private GenesisVdpProvider vdp;
    private GenesisBusProvider bus;
    private BusArbiter busArbiter;
    private M68kProvider cpu;

    int hCounterRaise = -1;
    int vCounterRaise = -1;
    int hCounterPending = -1;
    int vCounterPending = -1;

    @Before
    public void setup() {
        SystemProvider emu = VdpTestUtil.createTestGenesisProvider();
        bus = GenesisBusProvider.createBus();
        vdp = GenesisVdpProvider.createVdp(bus);
        Z80Provider z80 = Z80CoreWrapper.createGenesisInstance(bus);


        cpu = new MC68000Wrapper(bus) {
            @Override
            public boolean raiseInterrupt(int level) {
                hCounterRaise = vdp.getHCounter();
                vCounterRaise = vdp.getVCounter();
                return true;
            }
        };
        busArbiter = BusArbiter.createInstance(vdp, cpu, z80);

        bus.attachDevice(vdp).attachDevice(cpu).attachDevice(busArbiter).attachDevice(emu);
    }

    private void setupZ80() {
        Z80Provider z80 = new Z80CoreWrapper() {
            @Override
            public boolean interrupt(boolean value) {
                hCounterRaise = vdp.getHCounter();
                vCounterRaise = vdp.getVCounter();
                return true;
            }
        };
        busArbiter = BusArbiter.createInstance(vdp, cpu, z80);

        bus.attachDevice(busArbiter);
    }

    //    You can execute at least one instruction after enabling VINT in the VDP with a pending VINT.
//    Emulating this is required to make Sesame Street Counting Cafe to work.
    @Test
    public void testSesameStreet() {
        vdp.writeControlPort(0x8C00);
        vdp.writeControlPort(0x8174);
        vdp.resetVideoMode(true);
        do {
            VdpTestUtil.runVdpSlot(vdp);
            if (busArbiter.isVdpVInt() && hCounterPending < 0) {
                hCounterPending = vdp.getHCounter();
                vCounterPending = vdp.getVCounter();
            }
            bus.handleVdpInterrupts68k();

        } while (hCounterRaise < 0);
        Assert.assertEquals(vCounterRaise, vCounterPending);
        //this should be at least 1
        Assert.assertTrue(hCounterRaise > hCounterPending);
    }

    /**
     * Lotus 2, hip shouldnt trigger on line 0
     * <p>
     * Ack VDP VINT - , hce=6(c), vce=e0(e0), hBlankSet=false,vBlankSet=true, vIntPending=false, hIntPending=false, hLinePassed=243
     * IntMask from: 6 to: 7
     * HCOUNTER_VALUE changed from: ff, to: 0 -- , hce=26(4c), vce=e0(e0), hBlankSet=false,vBlankSet=true, vIntPending=false, hIntPending=false, hLinePassed=243
     * MODE_2 changed from: 24, to: 64 -- , hce=28(50), vce=e0(e0), hBlankSet=false,vBlankSet=true, vIntPending=false, hIntPending=false, hLinePassed=243
     * IntMask from: 7 to: 3
     * IntMask from: 3 to: 7
     * IntMask from: 7 to: 3
     * Set HIP: true, hLinePassed: -1, hce=85(10a), vce=0(0), hBlankSet=false,vBlankSet=false, vIntPending=false, hIntPending=true, hLinePassed=-1
     */
    @Test
    public void testLotus2() {
        vdp.writeControlPort(0x8C00);
        //disable hint
        vdp.writeControlPort(0x8AFF);
        vdp.writeControlPort(0x8004);
        vdp.writeControlPort(0x8144); //enable display
        VdpTestUtil.runVdpUntilFifoEmpty(vdp);
        vdp.resetVideoMode(true);
        do {
            boolean wasVblank = VdpTestUtil.isVBlank(vdp);
            VdpTestUtil.runVdpSlot(vdp);
            boolean vBlankTrigger = !wasVblank && VdpTestUtil.isVBlank(vdp);
            if (vBlankTrigger) {
                vdp.setHip(false);
                vdp.setVip(false);
                //enable hint after vblank period
                vdp.writeControlPort(0x8A00);
                vdp.writeControlPort(0x8014);
                VdpTestUtil.runVdpUntilFifoEmpty(vdp);
                Assert.assertFalse("HINT should not be pending", vdp.getHip());
            }
            bus.handleVdpInterrupts68k();

        } while (hCounterRaise < 0);
        //this should be at least 1, ie. no HINT triggered on line 0
        Assert.assertEquals(1, vCounterRaise);
    }

    @Test
    public void testZ80Interrupt() {
        setupZ80();
        //disable hint
        vdp.writeControlPort(0x8AFF);
        vdp.writeControlPort(0x8004);
        //enable VINT
        vdp.writeControlPort(0x8164);
        VdpTestUtil.runVdpUntilFifoEmpty(vdp);
        vdp.resetVideoMode(true);
        VdpCounterMode mode = VdpCounterMode.getCounterMode(vdp.getVideoMode());
        do {
            VdpTestUtil.runVdpSlot(vdp);
            bus.handleVdpInterrupts68k();
        } while (vCounterRaise < 0);
        Assert.assertEquals(mode.vBlankSet, vCounterRaise);
        vCounterRaise = -1;
        do {
            VdpTestUtil.runVdpSlot(vdp);
            bus.handleVdpInterrupts68k();
        } while (vCounterRaise < 0);
        Assert.assertEquals(mode.vBlankSet, vCounterRaise);
    }

    @Test
    public void testVBlankFlagWhenDisplayDisabled() {
        vdp.writeControlPort(0x8AFF);
        vdp.writeControlPort(0x8004);
        vdp.writeControlPort(0x8144); //enable display
        VdpTestUtil.runVdpUntilFifoEmpty(vdp);
        vdp.resetVideoMode(true);
        do {
            VdpTestUtil.runVdpSlot(vdp);
        } while (VdpTestUtil.isVBlank(vdp));

        //disable display -> vblank on
        vdp.writeControlPort(0x8104);
        VdpTestUtil.runVdpSlot(vdp);
        Assert.assertTrue(VdpTestUtil.isVBlank(vdp));

        //enable display
        vdp.writeControlPort(0x8144);
        VdpTestUtil.runVdpSlot(vdp);
        Assert.assertFalse(VdpTestUtil.isVBlank(vdp));
    }

    @Test
    public void testHBlankFlagWhenDisplayDisabled() {
        vdp.writeControlPort(0x8AFF);
        vdp.writeControlPort(0x8004);
        vdp.writeControlPort(0x8144); //enable display
        VdpTestUtil.runVdpUntilFifoEmpty(vdp);
        vdp.resetVideoMode(true);
        do {
            VdpTestUtil.runVdpSlot(vdp);
        } while (VdpTestUtil.isHBlank(vdp));

        //disable display -> hblank doesnt change
        vdp.writeControlPort(0x8104);
        VdpTestUtil.runVdpSlot(vdp);
        Assert.assertFalse(VdpTestUtil.isHBlank(vdp));
    }

    /**
     * TODO
     * Interrupts are know acknowledged based on what the
     * VDP thinks its asserting rather than what the 68K actually is ack-ing - Fixes Fatal Rewind
     */
    public void testFatalRewind() {

    }
}

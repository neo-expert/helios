/*
 * GenesisBusProvider
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 17/10/19 11:16
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

package omegadrive.bus.gen;

import omegadrive.bus.BaseBusProvider;
import omegadrive.sound.fm.FmProvider;
import omegadrive.sound.psg.PsgProvider;
import omegadrive.system.SystemProvider;
import omegadrive.vdp.model.GenesisVdpProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static omegadrive.memory.MemoryProvider.M68K_RAM_SIZE;

public interface GenesisBusProvider extends BaseBusProvider {

    //http://gendev.spritesmind.net/forum/viewtopic.php?f=25&t=1283
    int Z80_ADDRESS_SPACE_START = 0xA00000;
    int Z80_ADDRESS_SPACE_END = 0xA0FFFF;
    int IO_ADDRESS_SPACE_START = 0xA10000;
    int IO_ADDRESS_SPACE_END = 0xA10FFF;
    int INTERNAL_REG_ADDRESS_SPACE_START = 0xA11000;
    int INTERNAL_REG_ADDRESS_SPACE_END = 0xBFFFFF;
    int MEMORY_MODE_START = 0xA11000; //DRAM control reg.
    int MEMORY_MODE_END = 0xA110FF;
    int Z80_BUS_REQ_CONTROL_START = 0xA11100;
    int Z80_BUS_REQ_CONTROL_END = 0xA111FF;
    int Z80_RESET_CONTROL_START = 0xA11200;
    int Z80_RESET_CONTROL_END = 0xA112FF;
    int MEGA_CD_EXP_START = 0xA12000;
    int MEGA_CD_EXP_END = 0xA120FF;
    int TIME_LINE_START = 0xA13000;
    int TIME_LINE_END = 0xA130FF;
    int TMSS_AREA1_START = 0xA14000;
    int TMSS_AREA1_END = 0xA14003;
    int TMSS_AREA2_START = 0xA14100;
    int TMSS_AREA2_END = 0xA14101;
    int VDP_ADDRESS_SPACE_START = 0xC00000;
    int VDP_ADDRESS_SPACE_END = 0xDFFFFF;
    int ADDRESS_UPPER_LIMIT = 0xFFFFFF;
    int ADDRESS_RAM_MAP_START = 0xE00000;
    int M68K_TO_Z80_MEMORY_MASK = 0x7FFF;
    int VDP_VALID_ADDRESS_MASK = 0xE700E0;

    long DEFAULT_ROM_END_ADDRESS = 0x3F_FFFF;

    int M68K_RAM_MASK = M68K_RAM_SIZE - 1;

    int FIFO_FULL_MASK = 0x01;
    int DMA_IN_PROGRESS_MASK = 0x02;

    Logger LOG = LogManager.getLogger(GenesisBusProvider.class.getSimpleName());

    static GenesisBusProvider createBus() {
        return new GenesisBus();
    }

    void handleVdpInterrupts68k();

    void handleVdpInterruptsZ80();


    /**
     * VRES is fed to 68000 for 128 VCLKs (16.7us); ZRES is fed
     * to the z80 and ym2612, and remains asserted until the 68000 does something to
     * deassert it; VDP and IO chip are unaffected.
     */
    void resetFrom68k();

    boolean is68kRunning();

    //VDP setting this
    void setStop68k(int mask);

    boolean isZ80Running();

    boolean isZ80ResetState();

    boolean isZ80BusRequested();

    void setZ80ResetState(boolean z80ResetState);

    void setZ80BusRequested(boolean z80BusRequested);

    PsgProvider getPsg();

    FmProvider getFm();

    SystemProvider getSystem();

    GenesisVdpProvider getVdp();

    BusArbiter getBusArbiter();

    //Z80 for genesis doesnt do IO
    @Override
    default int readIoPort(int port) {
        //TF4 calls this by mistake
        LOG.debug("inPort: {}", port);
        return 0xFF;
    }

    //Z80 for genesis doesnt do IO
    @Override
    default void writeIoPort(int port, int value) {
        LOG.warn("outPort: " + port + ", data: " + value);
        return;
    }
}

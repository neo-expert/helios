/*
 * Copyright (c) 2018-2019 Federico Berti
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

package omegadrive.vdp.gen;

import omegadrive.bus.gen.GenesisBusProvider;
import omegadrive.util.RegionDetector;
import omegadrive.vdp.MdVdpTestUtil;
import omegadrive.vdp.model.GenesisVdpProvider;
import omegadrive.vdp.model.VdpDmaHandler;
import omegadrive.vdp.model.VdpMemoryInterface;
import omegadrive.LogManager;
import omegadrive.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.stream.IntStream;

@Ignore
public class BaseVdpDmaHandlerTest {

    private static Logger LOG = LogManager.getLogger(BaseVdpDmaHandlerTest.class.getSimpleName());

    GenesisVdpProvider vdpProvider;
    VdpMemoryInterface memoryInterface;
    VdpDmaHandler dmaHandler;

    @Before
    public void setup() {
        GenesisBusProvider busProvider = GenesisBusProvider.createBus();
        memoryInterface = GenesisVdpMemoryInterface.createInstance();
        dmaHandler = new VdpDmaHandlerImpl();

        vdpProvider = GenesisVdp.createInstance(busProvider, memoryInterface, dmaHandler, RegionDetector.Region.EUROPE);

        ((VdpDmaHandlerImpl) dmaHandler).vdpProvider = vdpProvider;
        ((VdpDmaHandlerImpl) dmaHandler).memoryInterface = memoryInterface;
        ((VdpDmaHandlerImpl) dmaHandler).busProvider = busProvider;
        vdpProvider.updateRegisterData(1, 4); //mode5
    }

    protected void testDMAFillInternal(long dmaFillLong, int increment,
                                       int[] expected) {
        testDMAFillInternal(dmaFillLong, 0x8000, increment, 0x68ac, expected);
    }

    protected void testDMAFillInternal2(long dmaFillLong, int baseAddress, int increment, int fillValueWord,
                                        int[] expected) {
        int toAddress = baseAddress + expected.length;

        vdpProvider.writeControlPort(0x8F00 + increment);
        vdpProvider.writeControlPort(0x8154);
        vdpProvider.writeControlPort(0x9300);
        vdpProvider.writeControlPort(0x9400);
        vdpProvider.writeControlPort(0x9780);

        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeControlPort(dmaFillLong >> 16);
        vdpProvider.writeControlPort(dmaFillLong & 0xFFFF);

        MdVdpTestUtil.runVdpSlot(vdpProvider);

        vdpProvider.writeDataPort(fillValueWord);

        MdVdpTestUtil.runVdpUntilDmaDone(vdpProvider);

        String[] actual = IntStream.range(baseAddress, toAddress).
                mapToObj(memoryInterface::readVramByte).map(Integer::toHexString).toArray(String[]::new);

        String[] exp = Arrays.stream(expected).mapToObj(Integer::toHexString).toArray(String[]::new);

//        System.out.println("Expected: " + Arrays.toString(exp));
//        System.out.println("Actual: " + Arrays.toString(actual));

        Assert.assertArrayEquals(exp, actual);
    }

    protected void testDMAFillInternal(long dmaFillLong, int baseAddress, int increment, int fillValueWord,
                                       int[] expected) {
        int toAddress = baseAddress + expected.length;
        vdpProvider.writeControlPort(0x8F02);
        vdpProvider.writeControlPort(16384);
        vdpProvider.writeControlPort(2);
        vdpProvider.writeDataPort(750);
        vdpProvider.writeDataPort(1260);
        vdpProvider.writeDataPort(1770);
        vdpProvider.writeDataPort(2280);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeDataPort(2790);
        vdpProvider.writeDataPort(3300);
        vdpProvider.writeDataPort(3810);
        vdpProvider.writeDataPort(736);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeDataPort(1230);
        vdpProvider.writeDataPort(1740);
        vdpProvider.writeDataPort(2250);
        vdpProvider.writeDataPort(2760);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeDataPort(3270);
        vdpProvider.writeDataPort(3780);
        vdpProvider.writeDataPort(706);
        vdpProvider.writeDataPort(1216);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        String str = MdVdpTestUtil.printVdpMemory(memoryInterface, GenesisVdpProvider.VdpRamType.VRAM, baseAddress, toAddress);
        System.out.println(str);

        vdpProvider.writeControlPort(0x8F00 + increment);
        vdpProvider.writeControlPort(0x8154);
        vdpProvider.writeControlPort(0x9305);
        vdpProvider.writeControlPort(0x9400);
        vdpProvider.writeControlPort(0x9500);
        vdpProvider.writeControlPort(0x9600);
        vdpProvider.writeControlPort(0x9780);

        vdpProvider.writeControlPort(dmaFillLong >> 16);
        vdpProvider.writeControlPort(dmaFillLong & 0xFFFF);

//        System.out.println("DestAddress: " + Integer.toHexString(vdpProvider.getAddressRegisterValue()));

        vdpProvider.writeDataPort(fillValueWord);

//        System.out.println("DestAddress: " + Integer.toHexString(vdpProvider.getAddressRegisterValue()));

        str = MdVdpTestUtil.printVdpMemory(memoryInterface, GenesisVdpProvider.VdpRamType.VRAM, baseAddress, toAddress);
        System.out.println(str);

        MdVdpTestUtil.runVdpUntilDmaDone(vdpProvider);

        str = MdVdpTestUtil.printVdpMemory(memoryInterface, GenesisVdpProvider.VdpRamType.VRAM, baseAddress, toAddress);
        System.out.println(str);

        String[] actual = IntStream.range(baseAddress, toAddress).
                mapToObj(memoryInterface::readVramByte).map(Integer::toHexString).toArray(String[]::new);

        String[] exp = Arrays.stream(expected).mapToObj(Integer::toHexString).toArray(String[]::new);


        System.out.println("Expected: " + Arrays.toString(exp));
        System.out.println("Actual: " + Arrays.toString(actual));

        Assert.assertArrayEquals(exp, actual);
    }


    protected void testDMACopyInternal(int increment, int[] expected) {
        vdpProvider.writeControlPort(0x8F02);
        vdpProvider.writeControlPort(0x4000);
        vdpProvider.writeControlPort(2);
        vdpProvider.writeDataPort(0xf00d);
        vdpProvider.writeDataPort(0xf00d);
        vdpProvider.writeDataPort(0xf00d);
        vdpProvider.writeDataPort(0xf00d);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeDataPort(0xf00d);
        vdpProvider.writeDataPort(0xf00d);
        vdpProvider.writeDataPort(0xf00d);
        vdpProvider.writeDataPort(0xf00d);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeControlPort(0x5000);
        vdpProvider.writeControlPort(2);
        vdpProvider.writeDataPort(0x1122);
        vdpProvider.writeDataPort(0x3344);
        vdpProvider.writeDataPort(0x5566);
        vdpProvider.writeDataPort(0x7788);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeDataPort(0x99aa);
        vdpProvider.writeDataPort(0xbbcc);
        vdpProvider.writeDataPort(0xddee);
        vdpProvider.writeDataPort(0xff00);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        String str = MdVdpTestUtil.printVdpMemory(memoryInterface, GenesisVdpProvider.VdpRamType.VRAM, 0x8000, 0x8016);
        System.out.println(str);

        str = MdVdpTestUtil.printVdpMemory(memoryInterface, GenesisVdpProvider.VdpRamType.VRAM, 0x9000, 0x9016);
        System.out.println(str);

        vdpProvider.writeControlPort(0x8F00 + increment);
        vdpProvider.writeControlPort(0x8154);
        vdpProvider.writeControlPort(0x9303);
        vdpProvider.writeControlPort(0x9400);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeControlPort(0x9500);
        vdpProvider.writeControlPort(0x9690);
        vdpProvider.writeControlPort(0x97C0);

        vdpProvider.writeControlPort(0);
        MdVdpTestUtil.runVdpUntilFifoEmpty(vdpProvider);

        vdpProvider.writeControlPort(0xc2);

        MdVdpTestUtil.runVdpUntilDmaDone(vdpProvider);

        str = MdVdpTestUtil.printVdpMemory(memoryInterface, GenesisVdpProvider.VdpRamType.VRAM, 0x8000, 0x8016);
        System.out.println(str);

        String[] exp = Arrays.stream(expected).mapToObj(Integer::toHexString).toArray(String[]::new);
        String[] actual = IntStream.range(0x8000, 0x8000 + expected.length).
                mapToObj(memoryInterface::readVramByte).map(Integer::toHexString).toArray(String[]::new);

        System.out.println("Expected: " + Arrays.toString(exp));
        System.out.println("Actual:   " + Arrays.toString(actual));

        Assert.assertArrayEquals(exp, actual);
    }

}

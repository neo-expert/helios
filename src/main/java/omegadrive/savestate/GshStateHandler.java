/*
 * GstStateHandler
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 17/10/19 20:41
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

package omegadrive.savestate;

import com.google.common.io.Files;
import omegadrive.bus.gen.GenesisBusProvider;
import omegadrive.sound.fm.FmProvider;
import omegadrive.sound.fm.ym2612.nukeykt.Ym2612Nuke;
import omegadrive.util.FileLoader;
import omegadrive.util.Util;
import omegadrive.z80.Z80Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.IntStream;

public class GshStateHandler extends GstStateHandler {

    private static Logger LOG = LogManager.getLogger(GshStateHandler.class.getSimpleName());

    protected static final String MAGIC_WORD = "GSH";
    protected static final String MAGIC_WORD_GST = "GST";
    protected static final String FM_MAGIC_WORD_NUKE = "NUKE";
    protected static final String fileExtension = "gsh";

    private static int SSF2_MAPPER_REG_OFFSET = 0x440;

    protected GshStateHandler() {
    }

    protected static String handleFileExtension(String fileName) {
        boolean hasExtension = fileName.toLowerCase().contains(".gs");
        return fileName + (!hasExtension ? "." + fileExtension : "");
    }

    protected void init(String fileNameEx) {
        this.fileName = handleFileExtension(fileNameEx);

        if (type == Type.SAVE) {
            buffer = ByteBuffer.allocate(GstStateHandler.FILE_SIZE);
            buffer.put(MAGIC_WORD.getBytes());
            //special Genecyst stuff
            buffer.put(6, (byte) 0xE0).put(7, (byte) 0x40);
        } else {
            String ext = Files.getFileExtension(fileNameEx);
            buffer = ByteBuffer.wrap(FileLoader.readBinaryFile(Paths.get(fileName), ext));
        }
    }

    protected GenesisStateHandler detectStateFileType() {
        String fileType = Util.toStringValue(buffer.get(), buffer.get(), buffer.get());
        boolean isSupported = MAGIC_WORD.equalsIgnoreCase(fileType) || MAGIC_WORD_GST.equalsIgnoreCase(fileType);
        if (!isSupported || buffer.capacity() < FILE_SIZE) {
            LOG.error("Unable to load save state of type: {}, size: {}", fileType, buffer.capacity());
            return GenesisStateHandler.EMPTY_STATE;
        }
        version = buffer.get(0x50) & 0xFF;
        softwareId = buffer.get(0x51) & 0xFF;
        LOG.info("Savestate type {}, version: {}, softwareId: {}", fileType, version, softwareId);
        if (MAGIC_WORD_GST.equalsIgnoreCase(fileType)) {
            LOG.warn("Loading a {} savestate, fm sound may not work correctly!", fileType);
        }
        return this;
    }

    @Override
    public void loadFmState(FmProvider fm) {
        int fmLen = buffer.capacity() - FILE_SIZE - FM_MAGIC_WORD_NUKE.length();
        if (fmLen > 500 && fm instanceof Ym2612Nuke) {
            Ym2612Nuke nukeFm = (Ym2612Nuke) fm;
            int pos = buffer.position();
            buffer.position(FILE_SIZE);
            String fmType = Util.toStringValue(buffer.get(), buffer.get(), buffer.get(), buffer.get());
            if (FM_MAGIC_WORD_NUKE.equalsIgnoreCase(fmType)) {
                buffer.position(FILE_SIZE + FM_MAGIC_WORD_NUKE.length());
                Serializable res = Util.deserializeObject(buffer.array(), FILE_SIZE + FM_MAGIC_WORD_NUKE.length(), fmLen);
                nukeFm.setState((Ym2612Nuke.Ym3438Context) res);
            }
            buffer.position(pos);
        } else {
            super.loadFmState(fm); //load FM registers
        }
    }


    @Override
    public void saveFm(FmProvider fm) {
        super.saveFm(fm); //save FM registers
        if (fm instanceof Ym2612Nuke) {
            int pos = buffer.position();
            Ym2612Nuke.Ym3438Context chip = ((Ym2612Nuke) fm).getState();
            byte[] chipData = Util.serializeObject(chip);
            buffer = extendBuffer(buffer, chipData.length);
            try {
                buffer.put(FM_MAGIC_WORD_NUKE.getBytes());
                buffer.put(chipData);
            } catch (Exception e) {
                LOG.error("Unable to save Nuke FM data");
            } finally {
                buffer.position(pos);
            }
        }
    }

    @Override
    public void saveZ80(Z80Provider z80, GenesisBusProvider bus) {
        super.saveZ80(z80, bus);
        int[] data = bus.getMapperData();
        if (data.length > 0) {
            buffer.position(SSF2_MAPPER_REG_OFFSET);
            Arrays.stream(data).forEach(v -> buffer.put((byte) v));
        }
    }

    @Override
    public void loadZ80(Z80Provider z80, GenesisBusProvider bus) {
        super.loadZ80(z80, bus);
        buffer.position(SSF2_MAPPER_REG_OFFSET);
        int[] data = new int[GenesisBusProvider.NUM_MAPPER_BANKS];
        IntStream.range(0, data.length).forEach(i -> data[i] = buffer.get() & 0xFF);
        bus.setMapperData(data);
    }

    private ByteBuffer extendBuffer(ByteBuffer current, int increaseDelta) {
        ByteBuffer extBuffer = ByteBuffer.allocate(FILE_SIZE + increaseDelta + 4);
        current.position(0);
        extBuffer.put(current);
        extBuffer.position(FILE_SIZE);
        return extBuffer;
    }
}

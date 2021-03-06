/*
 * AbstractSoundManager
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 27/10/19 13:12
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

package omegadrive.sound.javasound;

import omegadrive.Device;
import omegadrive.SystemLoader;
import omegadrive.sound.SoundProvider;
import omegadrive.sound.fm.FmProvider;
import omegadrive.sound.fm.MdFmProvider;
import omegadrive.sound.fm.ym2413.Ym2413Provider;
import omegadrive.sound.persist.FileSoundPersister;
import omegadrive.sound.persist.SoundPersister;
import omegadrive.sound.psg.PsgProvider;
import omegadrive.system.Sms;
//import omegadrive.system.gb.GbSoundWrapper;
import omegadrive.util.PriorityThreadFactory;
import omegadrive.util.RegionDetector;
import omegadrive.util.SoundUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import omegadrive.LogManager;
import omegadrive.Logger;

public abstract class AbstractSoundManager implements SoundProvider {
    private static final Logger LOG = LogManager.getLogger(JavaSoundManager.class.getSimpleName());

    protected static SoundPersister.SoundType DEFAULT_SOUND_TYPE = SoundPersister.SoundType.BOTH;

    private ExecutorService executorService;

    private static int OUTPUT_SAMPLE_SIZE = 16;
    private static int OUTPUT_CHANNELS = 2;
    public volatile boolean close;
    protected volatile PsgProvider psg;
    protected volatile FmProvider fm;
    protected SoundPersister soundPersister;
    int fmSize;
    int psgSize;
    public static AudioFormat audioFormat = new AudioFormat(SoundProvider.SAMPLE_RATE_HZ, OUTPUT_SAMPLE_SIZE, OUTPUT_CHANNELS, true, false);
    protected SourceDataLine dataLine;
    private boolean mute = false;
    private SystemLoader.SystemType type;
    protected RegionDetector.Region region;

    protected volatile boolean hasFm;
    protected volatile boolean hasPsg;

    public static SoundProvider createSoundProvider(SystemLoader.SystemType systemType, RegionDetector.Region region) {
        if (!ENABLE_SOUND) {
            LOG.warn("Sound disabled");
            return NO_SOUND;
        }
        AbstractSoundManager jsm = new JavaSoundManager();
        jsm.setFm(jsm.getFmProvider(systemType, region));
        jsm.setPsg(jsm.getPsgProvider(systemType, region));
        jsm.setSystemType(systemType);
        jsm.init(region);
        return jsm;
    }

    PsgProvider getPsgProvider(SystemLoader.SystemType systemType, RegionDetector.Region region) {
        PsgProvider psgProvider = PsgProvider.NO_SOUND;
        switch (systemType) {
            case MSX:
                psgProvider = PsgProvider.createAyInstance(region, SAMPLE_RATE_HZ);
                break;
            case NES:
            case GB:
                //no PSG, external audio set as FM
                break;
            default:
                psgProvider = PsgProvider.createSnInstance(region, SAMPLE_RATE_HZ);
                break;
        }
        hasPsg = getPsg() != PsgProvider.NO_SOUND;
        return psgProvider;
    }

    protected abstract Runnable getRunnable(SourceDataLine dataLine, RegionDetector.Region region);

    FmProvider getFmProvider(SystemLoader.SystemType systemType, RegionDetector.Region region) {
        FmProvider fmProvider = FmProvider.NO_SOUND;
        switch (systemType) {
            case GENESIS:
                fmProvider = MdFmProvider.createInstance(region, AbstractSoundManager.audioFormat);
                break;
            case SMS:
                if (Sms.ENABLE_FM) {
                    fmProvider = Ym2413Provider.createInstance(AbstractSoundManager.audioFormat);
                }
                break;
            default:
                break;
        }
        hasFm = getFm() != FmProvider.NO_SOUND;
        return fmProvider;
    }

    protected void init(RegionDetector.Region region) {
        this.region = region;
        dataLine = SoundUtil.createDataLine(audioFormat);
        soundPersister = new FileSoundPersister();
        fmSize = SoundProvider.getFmBufferIntSize(audioFormat);
        psgSize = SoundProvider.getPsgBufferByteSize(audioFormat);
        executorService = Executors.newSingleThreadExecutor(new PriorityThreadFactory(Thread.MAX_PRIORITY, JavaSoundManager.class.getSimpleName()));
        executorService.submit(getRunnable(dataLine, region));
        LOG.info("Output audioFormat: " + audioFormat + ", bufferSize: " + fmSize);
    }

    public void setSystemType(SystemLoader.SystemType type) {
        this.type = type;
    }

    @Override
    public PsgProvider getPsg() {
        return psg;
    }

    public void setPsg(PsgProvider psg) {
        this.psg = psg;
    }

    @Override
    public FmProvider getFm() {
        return fm;
    }

    public void setFm(FmProvider fm) {
        this.fm = fm;
    }

    @Override
    public void reset() {
        LOG.info("Resetting sound");
        close = true;
        List<Runnable> list = executorService.shutdownNow();
        SoundUtil.close(dataLine);
        setRecording(false);
        LOG.info("Closing sound, stopping background tasks: #" + list.size());
    }

    @Override
    public void close() {
        reset();
    }

    @Override
    public boolean isRecording() {
        return soundPersister.isRecording();
    }

    @Override
    public void setRecording(boolean recording) {
        if (isRecording() && !recording) {
            soundPersister.stopRecording();
        } else if (!isRecording() && recording) {
            soundPersister.startRecording(DEFAULT_SOUND_TYPE);
        }
    }

    @Override
    public boolean isMute() {
        return mute;
    }

    @Override
    public void setEnabled(boolean mute) {
        this.mute = mute;
        LOG.info("Set mute: " + mute);
    }

    @Override
    public void setEnabled(Device device, boolean enabled) {
        if (fm == device) {
            boolean isEnabled = fm != FmProvider.NO_SOUND;
            if (isEnabled != enabled) {
                this.fm = enabled ? getFmProvider(type, region) : FmProvider.NO_SOUND;
                LOG.info("FM enabled: {}", enabled);
            }
        } else if (psg == device) {
            boolean isEnabled = psg != PsgProvider.NO_SOUND;
            if (isEnabled != enabled) {
                this.psg = enabled ? getPsgProvider(type, region) : PsgProvider.NO_SOUND;
                LOG.info("PSG enabled: {}", enabled);
            }
        }
    }
}

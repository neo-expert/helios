package omegadrive.sound.psg;

import omegadrive.util.RegionDetector;

import static omegadrive.sound.SoundProvider.LOG;
import static omegadrive.sound.SoundProvider.getPsgSoundClock;

/**
 * ${FILE}
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2018
 */
public interface PsgProvider {

    int PSG_OUTPUT_SAMPLE_SIZE = 8;
    int PSG_OUTPUT_CHANNELS = 1;

    static PsgProvider createInstance(RegionDetector.Region region, int sampleRate) {
        double clock = getPsgSoundClock(region);
        LOG.info("PSG instance, clock: " + clock + ", sampleRate: " + sampleRate);
        PsgProvider psgProvider = new SN76496(clock, sampleRate);
        psgProvider.init();
        return psgProvider;
    }

    void init();

    void write(int data);

    void output(byte[] output);

    void reset();

    PsgProvider NO_SOUND = new PsgProvider() {

        @Override
        public void init() {

        }

        @Override
        public void write(int data) {

        }

        @Override
        public void output(byte[] ouput) {

        }

        @Override
        public void reset() {

        }
    };
}

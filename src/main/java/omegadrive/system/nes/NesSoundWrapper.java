package omegadrive.system.nes;

import com.grapeshot.halfnes.audio.AudioOutInterface;
import omegadrive.sound.fm.FmProvider;
import omegadrive.util.RegionDetector;
import omegadrive.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jctools.queues.atomic.SpscAtomicArrayQueue;

import javax.sound.sampled.AudioFormat;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NesSoundWrapper
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class NesSoundWrapper implements AudioOutInterface, FmProvider {

    private static final Logger LOG = LogManager.getLogger(NesSoundWrapper.class.getSimpleName());

    static final double VOLUME = 13107 / 16384.;

    private Queue<Integer> sampleQueue;
    private AtomicInteger queueLen = new AtomicInteger();

    public NesSoundWrapper(RegionDetector.Region region, AudioFormat audioFormat) {
        sampleQueue = new SpscAtomicArrayQueue<>(((int) audioFormat.getSampleRate()) << 1);
    }

    @Override
    public int update(int[] buf_lr, int offset, int count) {
        offset <<= 1;
        int end = (count << 1) + offset;

        int res = queueLen.get();
//        LOG.info("update " + res);
        int sampleNum = 0;
        int k = 0, i = 0;
        for (k = offset; k < end && i < res; k += 2, i++) {
            Integer sample = sampleQueue.peek();
            if (sample != null) {
                sampleQueue.poll();
                sampleNum++;
                buf_lr[k] = sample;
                buf_lr[k + 1] = buf_lr[k];
            }
        }
        queueLen.addAndGet(-sampleNum);
        return sampleNum;
    }

    @Override
    public void outputSample(int sample) {
        sample *= VOLUME;
        if (sample < -32768) {
            sample = -32768;
            //System.err.println("clip");
        }
        if (sample > 32767) {
            sample = 32767;
            //System.err.println("clop");
        }
        //mono
        boolean res = sampleQueue.offer(Util.getFromIntegerCache(sample));
        if (res) {
            queueLen.getAndIncrement();
        } else {
            LOG.info("Sample dropped");
        }
    }

    @Override
    public void flushFrame(boolean waitIfBufferFull) {
        //DO NOTHING
//        LOG.info("flush, waitIfFull: {}, samples: {}" ,waitIfBufferFull, queueLen.get());
    }

    @Override
    public boolean bufferHasLessThan(int samples) {
//        LOG.info("bufferHasLessThan: {}, actual: {}", samples, queueLen.get());
        return queueLen.get() < samples;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void destroy() {
        reset();
    }

    @Override
    public void init(int clock, int rate) {
        reset();
    }

    @Override
    public int readRegister(int type, int regNumber) {
        return 0;
    }

    @Override
    public void tick(double microsPerTick) {

    }

    @Override
    public void reset() {
        sampleQueue.clear();
        queueLen.set(0);
    }
}

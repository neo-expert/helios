package omegadrive.z80;

import z80core.Z80State;

/**
 * ${FILE}
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2018
 */
public interface Z80Provider {

    void initialize();

    int executeInstruction();

    void requestBus();

    void unrequestBus();

    boolean isBusRequested();

    void reset();

    boolean isReset();

    void disableReset();

    boolean isRunning();

    boolean isHalted();

    boolean interrupt();

    int readMemory(int address);

    void writeMemory(int address, int data);

    Z80BusProvider getZ80BusProvider();

    void loadZ80State(Z80State z80State);

    Z80State getZ80State();
}

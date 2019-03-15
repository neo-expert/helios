package omegadrive.sound.fm.nukeykt;

/**
 * ${FILE}
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2019
 */
public interface IYm3438 {

    void OPN2_Reset(IYm3438_Type chip);

    void OPN2_SetChipType(/*32 bit unsigned */ int type);

    void OPN2_Clock(IYm3438_Type chip, /* 16 bit signed */ int[] buffer);

    void OPN2_Write(IYm3438_Type chip, /*32 bit unsigned */ int port, /* 8 bit unsigned */ int data);

    void OPN2_SetTestPin(IYm3438_Type chip, /*32 bit unsigned */ int value);

    boolean OPN2_ReadTestPin(IYm3438_Type chip);

    boolean OPN2_ReadIRQPin(IYm3438_Type chip);

    int OPN2_Read(IYm3438_Type chip, int port);


    int ym3438_mode_ym2612 = 0x01;      /* Enables YM2612 emulation (MD1, MD2 VA2) */
    int ym3438_mode_readmode = 0x02;     /* Enables status read on any port (TeraDrive, MD1 VA7, MD2, etc) */


    int eg_num_attack = 0;
    int eg_num_decay = 1;
    int eg_num_sustain = 2;
    int eg_num_release = 3;


    class IYm3438_Type {
        int cycles;   //32 bit unsigned
        int channel;  //32 bit unsigned
        /* 16 bit signed */ int mol, mor;
        /* IO */
        /* 16 bit unsigned */ int write_data;
        /* 8 bit unsigned */ int write_a;
        /* 8 bit unsigned */ int write_d;
        /* 8 bit unsigned */ boolean write_a_en;
        /* 8 bit unsigned */ boolean write_d_en;
        /* 8 bit unsigned */ int write_busy;
        /* 8 bit unsigned */ int write_busy_cnt;
        /* 8 bit unsigned */ boolean write_fm_address;
        /* 8 bit unsigned */ int write_fm_data;
        /* 8 bit unsigned */ int write_fm_mode_a;
        /* 16 bit unsigned */ int address;
        /* 8 bit unsigned */ int data;
        /* 8 bit unsigned */ int pin_test_in;
        /* 8 bit unsigned */ int pin_irq;
        /* 8 bit unsigned */ int busy;
        /* LFO */
        /* 8 bit unsigned */ int lfo_en;
        /* 8 bit unsigned */ int lfo_freq;
        /* 8 bit unsigned */ int lfo_pm;
        /* 8 bit unsigned */ int lfo_am;
        /* 8 bit unsigned */ int lfo_cnt;
        /* 8 bit unsigned */ int lfo_inc;
        /* 8 bit unsigned */ int lfo_quotient;
        /* Phase generator */
        /* 16 bit unsigned */ int pg_fnum;
        /* 8 bit unsigned */ int pg_block;
        /* 8 bit unsigned */ int pg_kcode;
        /*32 bit unsigned */ int[] pg_inc = new int[24];
        /*32 bit unsigned */ int[] pg_phase = new int[24];
        /* 8 bit unsigned */ boolean[] pg_reset = new boolean[24];
        /*32 bit unsigned */ int pg_read;
        /* Envelope generator */
        /* 8 bit unsigned */ int eg_cycle;
        /* 8 bit unsigned */ int eg_cycle_stop;
        /* 8 bit unsigned */ int eg_shift;
        /* 8 bit unsigned */ int eg_shift_lock;
        /* 8 bit unsigned */ int eg_timer_low_lock;
        /* 16 bit unsigned */ int eg_timer;
        /* 8 bit unsigned */ int eg_timer_inc;
        /* 16 bit unsigned */ int eg_quotient;
        /* 8 bit unsigned */ boolean eg_custom_timer;
        /* 8 bit unsigned */ int eg_rate;
        /* 8 bit unsigned */ int eg_ksv;
        /* 8 bit unsigned */ int eg_inc;
        /* 8 bit unsigned */ int eg_ratemax;
        /* 8 bit unsigned */ int[] eg_sl = new int[2];
        /* 8 bit unsigned */ int eg_lfo_am;
        /* 8 bit unsigned */ int[] eg_tl = new int[2];
        /* 8 bit unsigned */ int[] eg_state = new int[24];
        /* 16 bit unsigned */ int[] eg_level = new int[24];
        /* 16 bit unsigned */ int[] eg_out = new int[24];
        /* 8 bit unsigned */ int[] eg_kon = new int[24];
        /* 8 bit unsigned */ int[] eg_kon_csm = new int[24];
        /* 8 bit unsigned */ int[] eg_kon_latch = new int[24];
        /* 8 bit unsigned */ int[] eg_csm_mode = new int[24];
        /* 8 bit unsigned */ boolean[] eg_ssg_enable = new boolean[24];
        /* 8 bit unsigned */ int[] eg_ssg_pgrst_latch = new int[24];
        /* 8 bit unsigned */ int[] eg_ssg_repeat_latch = new int[24];
        /* 8 bit unsigned */ int[] eg_ssg_hold_up_latch = new int[24];
        /* 8 bit unsigned */ int[] eg_ssg_dir = new int[24];
        /* 8 bit unsigned */ int[] eg_ssg_inv = new int[24];
        /*32 bit unsigned */ int[] eg_read = new int[2];
        /* 8 bit unsigned */ int eg_read_inc;
        /* FM */
        /* 16 bit signed */ int[][] fm_op1 = new int[6][2];
        /* 16 bit signed */ int[] fm_op2 = new int[6];
        /* 16 bit signed */ int[] fm_out = new int[24];
        /* 16 bit unsigned */ int[] fm_mod = new int[24];
        /* Channel */
        /* 16 bit signed */ int[] ch_acc = new int[6];
        /* 16 bit signed */ int[] ch_out = new int[6];
        /* 16 bit signed */ int ch_lock;
        /* 8 bit unsigned */ int ch_lock_l;
        /* 8 bit unsigned */ int ch_lock_r;
        /* 16 bit signed */ int ch_read;
        /* Timer */
        /* 16 bit unsigned */ int timer_a_cnt;
        /* 16 bit unsigned */ int timer_a_reg;
        /* 8 bit unsigned */ boolean timer_a_load_lock;
        /* 8 bit unsigned */ boolean timer_a_load;
        /* 8 bit unsigned */ boolean timer_a_enable;
        /* 8 bit unsigned */ boolean timer_a_reset;
        /* 8 bit unsigned */ boolean timer_a_load_latch;
        /* 8 bit unsigned */ boolean timer_a_overflow_flag;
        /* 8 bit unsigned */ int timer_a_overflow;

        /* 16 bit unsigned */ int timer_b_cnt;
        /* 8 bit unsigned */ int timer_b_subcnt;
        /* 16 bit unsigned */ int timer_b_reg;
        /* 8 bit unsigned */ boolean timer_b_load_lock;
        /* 8 bit unsigned */ boolean timer_b_load;
        /* 8 bit unsigned */ boolean timer_b_enable;
        /* 8 bit unsigned */ boolean timer_b_reset;
        /* 8 bit unsigned */ boolean timer_b_load_latch;
        /* 8 bit unsigned */ boolean timer_b_overflow_flag;
        /* 8 bit unsigned */ int timer_b_overflow;

        /* Register set */
        /* 8 bit unsigned */ int[] mode_test_21 = new int[8];
        /* 8 bit unsigned */ int[] mode_test_2c = new int[8];
        /* 8 bit unsigned */ int mode_ch3;
        /* 8 bit unsigned */ int mode_kon_channel;
        /* 8 bit unsigned */ int[] mode_kon_operator = new int[4];
        /* 8 bit unsigned */ int[] mode_kon = new int[24];
        /* 8 bit unsigned */ boolean mode_csm;
        /* 8 bit unsigned */ boolean mode_kon_csm;
        /* 8 bit unsigned */ int dacen;
        /* 16 bit signed */ int dacdata;

        /* 8 bit unsigned */ int[] ks = new int[24];
        /* 8 bit unsigned */ int[] ar = new int[24];
        /* 8 bit unsigned */ int[] sr = new int[24];
        /* 8 bit unsigned */ int[] dt = new int[24];
        /* 8 bit unsigned */ int[] multi = new int[24];
        /* 8 bit unsigned */ int[] sl = new int[24];
        /* 8 bit unsigned */ int[] rr = new int[24];
        /* 8 bit unsigned */ int[] dr = new int[24];
        /* 8 bit unsigned */ int[] am = new int[24];
        /* 8 bit unsigned */ int[] tl = new int[24];
        /* 8 bit unsigned */ int[] ssg_eg = new int[24];

        /* 16 bit unsigned */ int[] fnum = new int[6];
        /* 8 bit unsigned */ int[] block = new int[6];
        /* 8 bit unsigned */ int[] kcode = new int[6];
        /* 16 bit unsigned */ int[] fnum_3ch = new int[6];
        /* 8 bit unsigned */ int[] block_3ch = new int[6];
        /* 8 bit unsigned */ int[] kcode_3ch = new int[6];
        /* 8 bit unsigned */ int reg_a4;
        /* 8 bit unsigned */ int reg_ac;
        /* 8 bit unsigned */ int[] connect = new int[6];
        /* 8 bit unsigned */ int[] fb = new int[6];
        /* 8 bit unsigned */ int[] pan_l = new int[6], pan_r = new int[6];
        /* 8 bit unsigned */ int[] ams = new int[6];
        /* 8 bit unsigned */ int[] pms = new int[6];
        /* 8 bit unsigned */ int status;
        /*32 bit unsigned */ int status_time;
    }
}
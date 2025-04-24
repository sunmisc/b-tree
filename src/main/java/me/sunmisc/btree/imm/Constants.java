package me.sunmisc.btree.imm;

public final class Constants {
    public static final int MIN_ROOT_CHILDREN = 2;
    public static final int ORDER = 256;
    public static final int SHIFT_LEN = 6;

    public static final int LEAF_MIN_CHILDREN = Math.ceilDiv(ORDER, 2) - 1;
    public static final int LEAF_MAX_CHILDREN = ORDER - 1;
    public static final int INTERNAL_MIN_CHILDREN = Math.ceilDiv(ORDER, 2);
    public static final int INTERNAL_MAX_CHILDREN = ORDER;
}
package com.chiranjeev.lld.fixgateway.fix;

public final class FixTags {
    private FixTags() {
    }

    public static final int BEGIN_STRING = 8;
    public static final int BODY_LENGTH = 9;
    public static final int CHECK_SUM = 10;
    public static final int MSG_TYPE = 35;
    public static final int MSG_SEQ_NUM = 34;
    public static final int SENDER_COMP_ID = 49;
    public static final int TARGET_COMP_ID = 56;
    public static final int SENDING_TIME = 52;

    public static final int REF_SEQ_NUM = 45;
    public static final int REF_TAG_ID = 371;
    public static final int REF_MSG_TYPE = 372;
    public static final int SESSION_REJECT_REASON = 373;
    public static final int TEXT = 58;
    public static final int ENCRYPT_METHOD = 98;
    public static final int HEART_BT_INT = 108;
    public static final int TEST_REQ_ID = 112;

    public static final int CL_ORD_ID = 11;
    public static final int ORDER_ID = 37;
    public static final int EXEC_ID = 17;
    public static final int EXEC_TYPE = 150;
    public static final int ORD_STATUS = 39;
    public static final int SYMBOL = 55;
    public static final int SIDE = 54;
    public static final int ORDER_QTY = 38;
    public static final int ORD_TYPE = 40;
    public static final int PRICE = 44;
    public static final int TIME_IN_FORCE = 59;

    public static final String MSG_TYPE_HEARTBEAT = "0";
    public static final String MSG_TYPE_SESSION_REJECT = "3";
    public static final String MSG_TYPE_LOGON = "A";
    public static final String MSG_TYPE_NEW_ORDER_SINGLE = "D";
    public static final String MSG_TYPE_EXECUTION_REPORT = "8";
}


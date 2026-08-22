package com.chiranjeev.lld.fixsession;
public record FixMessage(int sequence,MessageType type,boolean possibleDuplicate,int beginSequence,int endSequence,String payload) {
    public FixMessage { if(sequence<=0)throw new IllegalArgumentException("sequence must be positive");if(type==null)throw new NullPointerException("type");payload=payload==null?"":payload; }
    public static FixMessage inbound(int sequence,MessageType type){return new FixMessage(sequence,type,false,0,0,"");}
    public static FixMessage resendRequest(int sequence,int begin,int end){return new FixMessage(sequence,MessageType.RESEND_REQUEST,false,begin,end,"");}
    public FixMessage asDuplicate(){return new FixMessage(sequence,type,true,beginSequence,endSequence,payload);}
}

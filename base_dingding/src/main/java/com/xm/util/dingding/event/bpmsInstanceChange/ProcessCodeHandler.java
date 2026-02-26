package com.xm.util.dingding.event.bpmsInstanceChange;

public interface ProcessCodeHandler {
    String getProcessCode();
    String getProcessName();
    void handle(BpmsInstanceChangeEvent event);
}

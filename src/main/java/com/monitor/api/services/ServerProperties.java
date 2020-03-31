package com.monitor.api.services;

import lombok.Data;

/**
 * @Author: Joylice
 * @Date: 2020/3/30 10:11
 */

public class ServerProperties {
    private String host;
    private String userCpu;
    private String combinedCpu;
    //接收到总字节数，单位KB
    private String rxBytesCounts;
    //发送的总字节数，单位KB
    private String txBytesCounts;
    private String usedMem;
    private String freeMem;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserCpu() {
        return userCpu;
    }

    public void setUserCpu(String userCpu) {
        this.userCpu = userCpu;
    }

    public String getCombinedCpu() {
        return combinedCpu;
    }

    public void setCombinedCpu(String combinedCpu) {
        this.combinedCpu = combinedCpu;
    }

    public String getRxBytesCounts() {
        return rxBytesCounts;
    }

    public void setRxBytesCounts(String rxBytesCounts) {
        this.rxBytesCounts = rxBytesCounts;
    }

    public String getTxBytesCounts() {
        return txBytesCounts;
    }

    public void setTxBytesCounts(String txBytesCounts) {
        this.txBytesCounts = txBytesCounts;
    }

    public String getUsedMem() {
        return usedMem;
    }

    public void setUsedMem(String usedMem) {
        this.usedMem = usedMem;
    }

    public String getFreeMem() {
        return freeMem;
    }

    public void setFreeMem(String freeMem) {
        this.freeMem = freeMem;
    }
}

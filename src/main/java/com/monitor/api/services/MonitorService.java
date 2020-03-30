package com.monitor.api.services;


import org.apache.dubbo.config.annotation.Service;
import org.hyperic.sigar.*;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.StringTokenizer;

/**
 * @Author: Joylice
 * @Date: 2020/3/27 17:24
 */
@Service
public class MonitorService {

    public static void main(String[] args) throws Exception {

        //获取本地Ip
        System.out.println("主机IP " + InetAddress.getLocalHost().getHostAddress());
        //获取CPU状态
        cpu();
        //获取IO吞吐量
        net();
        //获取内存占有率
        memory();


    }

    private static void cpu() throws SigarException {
        Sigar sigar = new Sigar();
        CpuInfo[] cpuInfo = sigar.getCpuInfoList();
        CpuPerc[] cpuPerc = null;
        cpuPerc = sigar.getCpuPercList();
        for (int i = 0; i < cpuInfo.length; i++) {

            System.out.println("第" + i + "块CPU信息");
            System.out.println("CPU用户使用率" + CpuPerc.format(cpuPerc[i].getUser()));
            System.out.println("CPU总使用率" + CpuPerc.format(cpuPerc[i].getCombined()));

        }
    }

    private static void net() throws Exception {
        Sigar sigar = new Sigar();
        String ifNames[] = sigar.getNetInterfaceList();
        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            // 网络设备名
            System.out.println("网络设备名:    " + name);
            // IP地址
            System.out.println("IP地址:    " + ifconfig.getAddress());
            // 网络装置是否正常启用
            if ((ifconfig.getFlags() & 1L) <= 0L) {
                System.out.println("!IFF_UP...skipping getNetInterfaceStat");
                continue;
            }
            NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);
            // 接收的总包裹数
            System.out.println(name + "接收的总包裹数:" + ifstat.getRxPackets());
            // 发送的总包裹数
            System.out.println(name + "发送的总包裹数:" + ifstat.getTxPackets());
            // 接收到的总字节数
            System.out.println(name + "接收到的总字节数:" + ifstat.getRxBytes());
            // 发送的总字节数
            System.out.println(name + "发送的总字节数:" + ifstat.getTxBytes());
            // 接收到的错误包数
            System.out.println(name + "接收到的错误包数:" + ifstat.getRxErrors());
            // 发送数据包时的错误数
            System.out.println(name + "发送数据包时的错误数:" + ifstat.getTxErrors());
            // 接收时丢弃的包数
            System.out.println(name + "接收时丢弃的包数:" + ifstat.getRxDropped());
            // 发送时丢弃的包数
            System.out.println(name + "发送时丢弃的包数:" + ifstat.getTxDropped());
        }
    }

    private static void memory() throws SigarException {
        Sigar sigar = new Sigar();
        Mem mem = sigar.getMem();
        // 内存总量
        System.out.println("内存总量:    " + mem.getTotal() / 1024L + "K av");
        // 当前内存使用量
        System.out.println("当前内存使用量:    " + mem.getUsed() / 1024L + "K used");
        // 当前内存剩余量
        System.out.println("当前内存剩余量:    " + mem.getFree() / 1024L + "K free");
    }

}

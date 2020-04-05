package com.monitor.provider.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.dubbo.common.api.MonitorServiceInterface;
import com.dubbo.common.entity.FileSystemProperties;
import com.dubbo.common.entity.ServerProperties;
import org.hyperic.sigar.*;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Joylice
 * @Date: 2020/3/27 17:24
 */
@Service
public class MonitorService implements MonitorServiceInterface {

    @Override
    public ServerProperties getPCState() {
        ServerProperties serverProperties = new ServerProperties();
        try {
            ServerProperties cpu;
            ServerProperties net;
            ServerProperties memory;
            List<FileSystemProperties> fileSystemProperties;
            cpu = cpu();
            net = net();
            memory = memory();
            fileSystemProperties = file();
            serverProperties.setHost(InetAddress.getLocalHost().getHostAddress());
            serverProperties.setUserCpu(cpu.getUserCpu());
            serverProperties.setCombinedCpu(cpu.getCombinedCpu());
            serverProperties.setRxBytesCounts(net.getRxBytesCounts());
            serverProperties.setTxBytesCounts(net.getTxBytesCounts());
            serverProperties.setFileSystems(fileSystemProperties);
            serverProperties.setUsedMem(memory.getUsedMem());
            serverProperties.setFreeMem(memory.getFreeMem());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return serverProperties;
    }

    private ServerProperties cpu() throws SigarException {
        Sigar sigar = new Sigar();
        ServerProperties serverProperties = new ServerProperties();
        CpuInfo[] cpuInfo = sigar.getCpuInfoList();
        CpuPerc[] cpuPerc = null;
        double userCpu = 0.0D;
        double combinedCpu = 0.0D;
        cpuPerc = sigar.getCpuPercList();
        for (int i = 0; i < cpuInfo.length; i++) {
            userCpu += cpuPerc[i].getUser();
            combinedCpu += cpuPerc[i].getCombined();
        }
        serverProperties.setUserCpu(CpuPerc.format(userCpu / cpuInfo.length));
        serverProperties.setCombinedCpu(CpuPerc.format(combinedCpu / cpuInfo.length));
        return serverProperties;
    }


    private ServerProperties net() throws Exception {

        Sigar sigar = new Sigar();
        ServerProperties serverProperties = new ServerProperties();
        //网络接收的总字节数
        long rxBytesCounts = 0L;
        //网络发送的总字节数
        long txBytesCounts = 0L;
        String ifNames[] = sigar.getNetInterfaceList();
        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            // 网络装置是否正常启用
            if ((ifconfig.getFlags() & 1L) <= 0L) {
                continue;
            }
            NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);
            // 接收到的总字节数
            rxBytesCounts += ifstat.getRxBytes();
            // 发送的总字节数
            txBytesCounts += ifstat.getTxBytes();
        }
        //单位KB
        serverProperties.setRxBytesCounts((rxBytesCounts / 1024L) + "");
        serverProperties.setTxBytesCounts(txBytesCounts / 1024L + "");

        return serverProperties;
    }

    private ServerProperties memory() throws SigarException {
        Sigar sigar = new Sigar();
        Mem mem = sigar.getMem();
        ServerProperties serverProperties = new ServerProperties();
        double usedMem = Long.valueOf(mem.getUsed()).doubleValue() / mem.getTotal();
        serverProperties.setUsedMem(CpuPerc.format(usedMem));
        serverProperties.setFreeMem(CpuPerc.format(1 - usedMem));
        return serverProperties;
    }

    private static List<FileSystemProperties> file() throws SigarException {
        Sigar sigar = new Sigar();
        List<FileSystemProperties> fileSystems = new ArrayList<>();
        FileSystem fileSystem[] = sigar.getFileSystemList();
        for (int i = 0; i < fileSystem.length; i++) {
            FileSystem fs = fileSystem[i];
            FileSystemUsage usage = null;
            usage = sigar.getFileSystemUsage(fs.getDirName());
            FileSystemProperties fileSystemProperties = new FileSystemProperties();
            fileSystemProperties.setDevName(fs.getDevName());
            fileSystemProperties.setDevTotalCounts(usage.getTotal());
            fileSystemProperties.setDevFreeCounts(usage.getFree());
            fileSystemProperties.setDevUsedCounts(usage.getUsed());
            fileSystems.add(fileSystemProperties);
        }
        return fileSystems;
    }
}
package com.monitor.provider.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.dubbo.common.api.MonitorServiceInterface;
import com.dubbo.common.entity.FileSystemProperties;
import com.dubbo.common.entity.HardwareProperties;
import com.dubbo.common.entity.ServerProperties;
import org.hyperic.sigar.*;
import org.springframework.beans.factory.annotation.Value;


import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @Author: Joylice
 * @Date: 2020/3/27 17:24
 */
@Service(version = "${demo.service.version}", interfaceClass = MonitorServiceInterface.class)
public class MonitorService implements MonitorServiceInterface {

    @Value("${host.model}")
    private String model;
    @Value("${host.cpu.mode}")
    private String cpuModel;
    @Value("${host.cpu.Ghz}")
    private String cpuGhz;
    @Value("${host.mem.count}")
    private String memCounts;
    @Value("${host.fileSystem.count}")
    private String fileCounts;


    @Override
    public ServerProperties getPCState() {
        ServerProperties serverProperties = new ServerProperties();
        try {
            ServerProperties cpu;
            ServerProperties net;
            ServerProperties memory;
            List<FileSystemProperties> fileSystemProperties;
            String os = System.getProperty("os.name");
            cpu = cpu();
            net = net();
            memory = memory();
            fileSystemProperties = file();
            if (os.toLowerCase().startsWith("win")) {
                serverProperties.setHost(InetAddress.getLocalHost().getHostAddress());
            } else {
                String ip = getLocalLinuxIP();
                serverProperties.setHost(ip);
            }
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

    @Override
    public HardwareProperties getHardwarePro() {
        try {
            return getHardValues();
        } catch (Exception e) {
            return new HardwareProperties();
        }
    }

    private HardwareProperties getHardValues() throws SigarException, UnknownHostException {
        HardwareProperties hardwareProperties = new HardwareProperties();
        Sigar sigar = new Sigar();
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            hardwareProperties.setHost(InetAddress.getLocalHost().getHostAddress());
        } else {
            hardwareProperties.setHost(getLocalLinuxIP());
        }
        hardwareProperties.setModel(model);
        CpuInfo[] cpuInfo = sigar.getCpuInfoList();
        hardwareProperties.setCpuCounts(cpuInfo.length + "");
        hardwareProperties.setCpuModel(cpuModel);
        hardwareProperties.setCpuGHz(cpuGhz);
        hardwareProperties.setMemCounts(memCounts);
        hardwareProperties.setFileCounts(fileCounts);
        return hardwareProperties;
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

    private List<FileSystemProperties> file() throws SigarException {
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

    private String getLocalLinuxIP() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.getName().startsWith("eth0")) continue;
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }


}

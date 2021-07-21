package spider.monitor;

import com.entity.SysInfo;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class SysMonitor {

    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public SysInfo getSysInfo() {
        SysInfo sysInfo = new SysInfo();
        sysInfo.setOs(osBean.getName());
        sysInfo.setCpuArch(osBean.getArch());
        sysInfo.setTotalMem(osBean.getTotalPhysicalMemorySize() / 1024 / 1024 / 1024);
        sysInfo.setUsedMem(Runtime.getRuntime().totalMemory() / (1024.0 * 1024));
        return sysInfo;
    }
}

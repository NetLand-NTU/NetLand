package Widgets;


import java.io.InputStreamReader;
import java.io.LineNumberReader;

import java.lang.management.ManagementFactory;
//import sun.management.*;

import com.sun.management.OperatingSystemMXBean;
import java.io.*;
import java.util.StringTokenizer;


public class MonitorServiceImpl  {

	private static final int CPUTIME = 30;
	private static final int PERCENT = 100;
	private static final int FAULTLENGTH = 10;
	private static final File versionFile = new File("/proc/version");
	private static String linuxVersion = null;

	private long totalMemory;
	private long freeMemory;
	private long maxMemory;

	private String osName;
	private long totalMemorySize;
	private long freePhysicalMemorySize;
	private long usedMemory;
	private int totalThread;
	private long totalFreeMemory;

	
	public MonitorServiceImpl() {
		int kb = 1024;
		int mb = 1024*1024;

		Runtime.getRuntime().gc();
		Thread.yield();
		totalMemory = Runtime.getRuntime().totalMemory() / mb;
		freeMemory = Runtime.getRuntime().freeMemory() / mb;
		maxMemory = Runtime.getRuntime().maxMemory() / mb;

		OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

		
//		osName = System.getProperty("os.name");
//		totalMemorySize = osmxb.getTotalPhysicalMemorySize() / kb;
//		freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / kb;
//		usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / kb;
		usedMemory = totalMemory - freeMemory;
		totalFreeMemory = maxMemory - usedMemory;

		
		ThreadGroup parentThread;
		for (parentThread = Thread.currentThread().getThreadGroup(); parentThread.getParent() != null; parentThread = parentThread.getParent())
			;
		totalThread = parentThread.activeCount();

	}
	

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	public long getFreePhysicalMemorySize() {
		return freePhysicalMemorySize;
	}

	public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
		this.freePhysicalMemorySize = freePhysicalMemorySize;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(long maxMemory) {
		this.maxMemory = maxMemory;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public long getTotalMemorySize() {
		return totalMemorySize;
	}

	public void setTotalMemorySize(long totalMemorySize) {
		this.totalMemorySize = totalMemorySize;
	}

	public int getTotalThread() {
		return totalThread;
	}

	public void setTotalThread(int totalThread) {
		this.totalThread = totalThread;
	}

	public long getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}


	public long getTotalFreeMemory() {
		return totalFreeMemory;
	}


	public void setTotalFreeMemory(long totalFreeMemory) {
		this.totalFreeMemory = totalFreeMemory;
	}

}
package ch.epfl.gsn.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Hashtable;

public class MemoryMonitor implements Monitorable {

	private MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

	/**
	 * Retrieves the statistics of the memory usage.
	 * 
	 * @return A Hashtable containing the memory statistics.
	 */
	public Hashtable<String, Object> getStatistics() {
		Hashtable<String, Object> stat = new Hashtable<String, Object>();
		stat.put("core.memory.heap.gauge", mbean.getHeapMemoryUsage().getUsed());
		stat.put("core.memory.nonHeap.gauge", mbean.getNonHeapMemoryUsage().getUsed());
		stat.put("core.memory.pendingFinalizationCount.gauge", mbean.getObjectPendingFinalizationCount());
		return stat;
	}

}

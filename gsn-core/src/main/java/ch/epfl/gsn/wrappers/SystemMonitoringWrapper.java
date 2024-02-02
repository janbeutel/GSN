package ch.epfl.gsn.wrappers;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.utils.ParamParser;

import org.slf4j.Logger;

public class SystemMonitoringWrapper extends AbstractWrapper {

   private static final int DEFAULT_SAMPLING_RATE = 1000;

   private int samplingRate = DEFAULT_SAMPLING_RATE;

   private final transient Logger logger = LoggerFactory.getLogger(SystemMonitoringWrapper.class);

   private static int threadCounter = 0;

   private transient DataField[] outputStructureCache = new DataField[] {
         new DataField(FIELD_NAME_HEAP, "bigint", "Heap memory usage."),
         new DataField(FIELD_NAME_MAX_HEAP, "bigint",
               "Maximum amount of HEAP memory in bytes that can be used for memory management"),
         new DataField(FIELD_NAME_NON_HEAP, "bigint", "Nonheap memory usage."),
         new DataField(FIELD_NAME_PENDING_FINALIZATION_COUNT, "int",
               "The number of objects with pending finalization."),
         new DataField(FIELD_NAME_SYSTEM_LOAD_AVERAGE, "double", "System load average for the last minute"),
         new DataField(FIELD_NAME_THREAD_COUNT, "int", "Thread Count"),
         new DataField(FIELD_NAME_PEAK_THREAD_COUNT, "int",
               "peak live thread count since the Java virtual machine started"),
         new DataField(FIELD_NAME_UPTIME, "bigint", "uptime of the Java virtual machine in milliseconds"),
         new DataField(FIELD_NAME_BLOCKED_THREADS, "int", "blocked threads counter"),
         new DataField(FIELD_NAME_NEW_THREADS, "int", "new threads counter"),
         new DataField(FIELD_NAME_RUNNABLE_THREADS, "int", "runnable threads counter"),
         new DataField(FIELD_NAME_WAITING_THREADS, "int", "waiting threads counter"),
         new DataField(FIELD_NAME_TERMINATED_THREADS, "int", "terminated threads counter")
   };

   private static final String FIELD_NAME_HEAP = "HEAP";
   private static final String FIELD_NAME_MAX_HEAP = "MAX_HEAP";

   private static final String FIELD_NAME_NON_HEAP = "NON_HEAP";

   private static final String FIELD_NAME_PENDING_FINALIZATION_COUNT = "PENDING_FINALIZATION_COUNT";

   private static final String FIELD_NAME_SYSTEM_LOAD_AVERAGE = "SYSTEM_LOAD_AVERAGE";

   private static final String FIELD_NAME_THREAD_COUNT = "THREAD_COUNT";
   private static final String FIELD_NAME_PEAK_THREAD_COUNT = "PEAK_THREAD_COUNT";

   private static final String FIELD_NAME_UPTIME = "UPTIME";

   private static final String FIELD_NAME_BLOCKED_THREADS = "BLOCKED_THREADS";
   private static final String FIELD_NAME_NEW_THREADS = "NEW_THREADS";
   private static final String FIELD_NAME_RUNNABLE_THREADS = "RUNNABLE_THREADS";
   private static final String FIELD_NAME_WAITING_THREADS = "WAITING_THREADS";
   private static final String FIELD_NAME_TERMINATED_THREADS = "TERMINATED_THREADS";

   private static final String[] FIELD_NAMES = new String[] { FIELD_NAME_HEAP, FIELD_NAME_MAX_HEAP, FIELD_NAME_NON_HEAP,
         FIELD_NAME_PENDING_FINALIZATION_COUNT, FIELD_NAME_SYSTEM_LOAD_AVERAGE, FIELD_NAME_THREAD_COUNT,
         FIELD_NAME_PEAK_THREAD_COUNT, FIELD_NAME_UPTIME, FIELD_NAME_BLOCKED_THREADS, FIELD_NAME_NEW_THREADS,
         FIELD_NAME_RUNNABLE_THREADS, FIELD_NAME_WAITING_THREADS, FIELD_NAME_TERMINATED_THREADS };

   private static final MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

   private static final ThreadMXBean tbean = ManagementFactory.getThreadMXBean();

   private static final OperatingSystemMXBean osbean = ManagementFactory.getOperatingSystemMXBean();

   private static final RuntimeMXBean rbean = ManagementFactory.getRuntimeMXBean();

   /**
    * Initializes the SystemMonitoringWrapper.
    * Retrieves the sampling rate from the active AddressBean and sets it as the
    * samplingRate.
    * If the sampling rate is not specified or is less than or equal to 0, the
    * default sampling rate is used.
    * 
    * @return true if the initialization is successful, false otherwise.
    */
   public boolean initialize() {
      AddressBean addressBean = getActiveAddressBean();
      if (addressBean.getPredicateValue("sampling-rate") != null) {
         samplingRate = ParamParser.getInteger(addressBean.getPredicateValue("sampling-rate"), DEFAULT_SAMPLING_RATE);
         if (samplingRate <= 0) {
            logger.warn(
                  "The specified >sampling-rate< parameter for the >SystemMonitoringWrapper< should be a positive number.\nGSN uses the default rate ("
                        + DEFAULT_SAMPLING_RATE + "ms ).");
            samplingRate = DEFAULT_SAMPLING_RATE;
         }
      }
      return true;
   }

   /**
    * Executes the monitoring process in a loop until the isActive flag is set to
    * false.
    * The monitoring process collects various system metrics such as memory usage,
    * thread count, and system load average.
    * It then creates a StreamElement object with the collected metrics and posts
    * it to the stream.
    */
   public void run() {
      while (isActive()) {
         try {
            Thread.sleep(samplingRate);
         } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
         }
         long heapMemoryUsage = mbean.getHeapMemoryUsage().getUsed();
         long maxHeapMemory = mbean.getHeapMemoryUsage().getMax();

         long nonHeapMemoryUsage = mbean.getNonHeapMemoryUsage().getUsed();

         int pendingFinalizationCount = mbean.getObjectPendingFinalizationCount();
         double systemLoadAverage = osbean.getSystemLoadAverage();
         int threadCount = tbean.getThreadCount();
         int peakThreadCount = tbean.getPeakThreadCount();
         long uptime = rbean.getUptime();
         long[] threadIds = tbean.getAllThreadIds();
         ThreadInfo[] threads = tbean.getThreadInfo(threadIds);
         int blocked_thread_counter = 0;
         int new_thread_counter = 0;
         int runnable_thread_counter = 0;
         int terminated_thread_counter = 0;
         int waiting_thread_counter = 0;
         for (ThreadInfo threadInfo : threads) {
            if (threadInfo != null) {
               switch (threadInfo.getThreadState()) {
                  case BLOCKED:
                     blocked_thread_counter++;
                     break;
                  case NEW:
                     new_thread_counter++;
                     break;
                  case RUNNABLE:
                     runnable_thread_counter++;
                     break;
                  case WAITING:
                     waiting_thread_counter++;
                     break;
                  case TERMINATED:
                     terminated_thread_counter++;
                     break;
                  default:
                     break;
               }
            }
         }

         StreamElement streamElement = new StreamElement(FIELD_NAMES, new Byte[] { DataTypes.BIGINT,
               DataTypes.BIGINT,
               DataTypes.BIGINT,
               DataTypes.INTEGER,
               DataTypes.DOUBLE,
               DataTypes.INTEGER,
               DataTypes.INTEGER,
               DataTypes.BIGINT,
               DataTypes.INTEGER,
               DataTypes.INTEGER,
               DataTypes.INTEGER,
               DataTypes.INTEGER,
               DataTypes.INTEGER },
               new Serializable[] { heapMemoryUsage,
                     maxHeapMemory,
                     nonHeapMemoryUsage,
                     pendingFinalizationCount,
                     systemLoadAverage,
                     threadCount,
                     peakThreadCount,
                     uptime,
                     blocked_thread_counter,
                     new_thread_counter,
                     runnable_thread_counter,
                     waiting_thread_counter,
                     terminated_thread_counter,
               },
               System.currentTimeMillis());

         postStreamElement(streamElement);
      }
   }

   public void dispose() {
      threadCounter--;
   }

   /**
    * The output fields exported by this virtual sensor.
    * 
    * @return The strutcture of the output.
    */

   public final DataField[] getOutputFormat() {
      return outputStructureCache;
   }

   public String getWrapperName() {
      return "System Monitoring";
   }
}

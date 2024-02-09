package ch.epfl.gsn.vsensor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.Map.Entry;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * This class represents a Stream Merging Virtual Sensor that extends the BridgeVirtualSensorPermasense class.
 * It merges multiple streams of data based on specified parameters and stores the merged data in a buffer.
 * The merged data can be accessed and processed later.
 */
public class StreamMergingVirtualSensor extends BridgeVirtualSensorPermasense {

	private static final String BUFFER_SIZE_IN_DAYS = "maximum_buffered_stream_age_in_days";
	private static final String NUMBER_OF_STREAMS_PER_MERGE_BUCKET = "streams_needed_to_merge";
	private static final String TIMELINE = "timeline";
	private static final String MERGE_BUCKET_SIZE_IN_MINUTES = "merge_bucket_size_in_minutes";
	private static final String MERGE_BUCKET_EDGE_TYPE = "merge_bucket_edge_type";
	private static final String MATCHING_FIELD1 = "matching_field1";
	private static final String MATCHING_FIELD2 = "matching_field2";
	private static final String DEFAULT_MERGE_OPERATOR = "default_merge_operator";
	private static final String FILTER_DUPLICATES = "filter_duplicates";
	private static final String FILTER_INPUTSTREAMNAME_DUPLICATES = "filter_data_points_from_same_source";
	private static final String DUPLICATES_IGNORE_FIELDS = "duplicates_ignore_field";

	private static final Long CLEANUP_TIMER_PERIOD = 86400000L;

	private static enum BucketEdgeType {
		STATIC, DYNAMIC
	}

	private static Map<String, BucketEdgeType> BUCKET_EdgeType = new HashMap<String, BucketEdgeType>();
	static {
		BUCKET_EdgeType.put("static", BucketEdgeType.STATIC);
		BUCKET_EdgeType.put("dynamic", BucketEdgeType.DYNAMIC);
	}

	private static enum Operator {
		NEW, OLD, ADD, AVG, MIN, MAX
	}

	private static Map<String, Operator> MERGE_Operator = new HashMap<String, Operator>();
	static {
		MERGE_Operator.put("new", Operator.NEW);
		MERGE_Operator.put("old", Operator.OLD);
		MERGE_Operator.put("add", Operator.ADD);
		MERGE_Operator.put("avg", Operator.AVG);
		MERGE_Operator.put("min", Operator.MIN);
		MERGE_Operator.put("max", Operator.MAX);
	}

	private static final transient Logger logger = LoggerFactory.getLogger(StreamMergingVirtualSensor.class);

	private Map<Serializable, Map<Serializable, ArrayList<StreamElementContainer>>> streamElementBuffer = new HashMap<Serializable, Map<Serializable, ArrayList<StreamElementContainer>>>();
	private Map<String, Operator> FieldNameToOperatorMap = new HashMap<String, Operator>();
	private Long bufferSizeInMs;
	private Long bufferNow = null;
	private Long newestData = 0L;
	private Integer bucketSpace;
	private String timeline;
	private Long bucketSizeInMs = null;
	private BucketEdgeType bucketEdgeType = null;
	private String matchingFieldName1 = null;
	private String matchingFieldName2 = null;
	private Operator defaultMergeOperator = null;
	private boolean filterDuplicates = false;
	private boolean filterInputStreamDuplicates = false;
	private String[] duplicatesIgnoreFields = null;
	private DataField[] mergedDataFields;
	private Long totalBufferedStreams = 0L;
	private Semaphore cleanupLock = new Semaphore(1);
	Timer cleanupTimer = new Timer();

	private static final DataField[] statisticsDataFields = {
			new DataField("MERGED_STREAMS", "INTEGER"),
			new DataField("TOTAL_BUFFERED_STREAMS", "BIGINT")
	};

	/**
	 * Initializes the virtual sensor by extracting and processing configuration parameters 
	 * from the Virtual Sensor Configuration file.
	 * 
	 *
	 * @return true if initialization is successful, false otherwise.
	 */

	@Override
	public boolean initialize() {
		boolean ret = super.initialize();
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		TreeMap<String, String> params = vsensor.getMainClassInitialParams();
		ArrayList<DataField> mergedFieldList = new ArrayList<DataField>();
		String bucketEdgeTypeStr = null;
		ArrayList<String> duplIgnoreFields = new ArrayList<String>();

		for (Entry<String, String> entry : params.entrySet()) {
			String paramName = entry.getKey().trim().toLowerCase();
			String value = entry.getValue().trim().toLowerCase();
			if (paramName.compareToIgnoreCase(BUFFER_SIZE_IN_DAYS) == 0) {
				try {
					bufferSizeInMs = Long.decode(value) * 86400000L;
				} catch (NumberFormatException e) {
					logger.error(BUFFER_SIZE_IN_DAYS + " has to be an integer");
					return false;
				}
			} else if (paramName.compareToIgnoreCase(MERGE_BUCKET_SIZE_IN_MINUTES) == 0) {
				try {
					bucketSizeInMs = Long.decode(value) * 60000L;
				} catch (NumberFormatException e) {
					logger.error(MERGE_BUCKET_SIZE_IN_MINUTES + " has to be an integer");
					return false;
				}
			} else if (paramName.compareToIgnoreCase(NUMBER_OF_STREAMS_PER_MERGE_BUCKET) == 0) {
				try {
					this.bucketSpace = Integer.decode(value);
				} catch (NumberFormatException e) {
					logger.error(NUMBER_OF_STREAMS_PER_MERGE_BUCKET + " has to be an integer");
					return false;
				}

			} else if (paramName.compareToIgnoreCase(MERGE_BUCKET_EDGE_TYPE) == 0) {
				if (BUCKET_EdgeType.get(value) == null) {
					logger.error("merge bucket edge type (" + paramName + "=" + value + ") unknown");
					return false;
				}
				bucketEdgeTypeStr = value;
				bucketEdgeType = BUCKET_EdgeType.get(value);
			} else if (paramName.compareToIgnoreCase(TIMELINE) == 0) {
				if (!isInOutputStructure(value)) {
					logger.error(TIMELINE + " (" + value + ") can not be found in output structure");
					return false;
				}
				timeline = value;
			} else if (paramName.compareToIgnoreCase(MATCHING_FIELD1) == 0) {
				if (!isInOutputStructure(value)) {
					logger.error(MATCHING_FIELD1 + " (" + value + ") can not be found in output structure");
					return false;
				}
				matchingFieldName1 = value;
			} else if (paramName.compareToIgnoreCase(MATCHING_FIELD2) == 0) {
				if (!isInOutputStructure(value)) {
					logger.error(MATCHING_FIELD2 + " (" + value + ") can not be found in output structure");
					return false;
				}
				matchingFieldName2 = value;
			} else if (paramName.compareToIgnoreCase(FILTER_DUPLICATES) == 0) {
				filterDuplicates = Boolean.parseBoolean(value);
			} else if (paramName.compareToIgnoreCase(FILTER_INPUTSTREAMNAME_DUPLICATES) == 0) {
				filterInputStreamDuplicates = Boolean.parseBoolean(value);
			} else if (paramName.toLowerCase().startsWith(DUPLICATES_IGNORE_FIELDS)) {
				String postfix = paramName.replaceFirst(DUPLICATES_IGNORE_FIELDS, "");
				if (!isInOutputStructure(value)) {
					logger.error(DUPLICATES_IGNORE_FIELDS + postfix + " (" + value
							+ ") can not be found in output structure");
					return false;
				}
				duplIgnoreFields.add(value);
			} else if (paramName.compareToIgnoreCase(DEFAULT_MERGE_OPERATOR) == 0) {
				if (MERGE_Operator.get(value) == null) {
					logger.error("default merge operator (" + paramName + "=" + value + ") unknown");
					return false;
				}
				defaultMergeOperator = MERGE_Operator.get(value);
			} else {
				if (!isInOutputStructure(paramName)) {
					logger.error(paramName + " can not be found in output structure");
					return false;
				}
				if (MERGE_Operator.get(value) == null) {
					logger.error("merge operator (" + paramName + "=" + value + ") unknown");
					return false;
				}
				FieldNameToOperatorMap.put(paramName, MERGE_Operator.get(value));
			}
		}

		if (bufferSizeInMs == null) {
			logger.error(BUFFER_SIZE_IN_DAYS + " parameter has to be spezified");
			return false;
		}
		if (bucketSpace == null) {
			logger.error(NUMBER_OF_STREAMS_PER_MERGE_BUCKET + " parameter has to be spezified");
			return false;
		}
		if (timeline == null) {
			logger.error(TIMELINE + " parameter has to be spezified");
			return false;
		}

		if (!(bucketSizeInMs == null && bucketEdgeType == null) && (bucketSizeInMs == null || bucketEdgeType == null)) {
			logger.error(
					MERGE_BUCKET_EDGE_TYPE + " and " + MERGE_BUCKET_SIZE_IN_MINUTES + " can not be used seperately");
			return false;
		}

		// check if Operator is available and matches field type
		for (DataField d : getVirtualSensorConfiguration().getOutputStructure()) {
			boolean cont = false;
			for (DataField df : statisticsDataFields) {
				if (d.getName().compareToIgnoreCase(df.getName()) == 0) {
					cont = true;
					break;
				}
			}
			if (cont) {
				continue;
			}

			if (d.getName().compareToIgnoreCase(timeline) == 0) {
				if (d.getDataTypeID() != DataTypes.BIGINT) {
					logger.error(TIMELINE + " (" + timeline + ") field type (" + d.getType() + ") must be BIGINT");
					return false;
				}
			} else if (matchingFieldName1 != null && d.getName().compareToIgnoreCase(matchingFieldName1) == 0) {
				if (d.getDataTypeID() == DataTypes.BINARY) {
					logger.error("binary matching is not supported (" + MATCHING_FIELD1 + ")");
					return false;
				}
			} else if (matchingFieldName2 != null && d.getName().compareToIgnoreCase(matchingFieldName2) == 0) {
				if (d.getDataTypeID() == DataTypes.BINARY) {
					logger.error("binary matching is not supported (" + MATCHING_FIELD2 + ")");
					return false;
				}
			}

			Operator op = getOperator(d.getName());
			if (op == null) {
				logger.error("No operator specified for output field " + d.getName() + " (you may use "
						+ DEFAULT_MERGE_OPERATOR + " parameter)");
				return false;
			}

			boolean match = false;
			switch (op) {
				case ADD:
					if (d.getDataTypeID() == DataTypes.VARCHAR ||
							d.getDataTypeID() == DataTypes.CHAR ||
							d.getDataTypeID() == DataTypes.BINARY) {
						match = true;
					}

				case AVG:
				case MIN:
				case MAX:
					switch (d.getDataTypeID()) {
						case DataTypes.DOUBLE:
						case DataTypes.BIGINT:
						case DataTypes.INTEGER:
						case DataTypes.SMALLINT:
						case DataTypes.TINYINT:
							match = true;
							break;
						default:
							break;
					}
					break;
				case NEW:
				case OLD:
					match = true;
					break;
				default:
					break;
			}

			if (!match) {
				String opName = null;
				for (Entry<String, Operator> entry : MERGE_Operator.entrySet()) {
					if (entry.getValue() == op) {
						opName = entry.getKey();
						break;
					}
				}
				logger.error("merge operator (" + opName + ") can not be used with field " + d.getName()
						+ " (field type: " + d.getType() + ")");
				return false;
			}

			mergedFieldList.add(d);
		}
		mergedDataFields = new DataField[mergedFieldList.size()];
		mergedFieldList.toArray(mergedDataFields);

		logger.info("Maximum buffered stream age: " + bufferSizeInMs + "ms");
		logger.info("Number of streams needed to merge: " + bucketSpace);
		logger.info("Timeline: " + timeline);
		if (bucketSizeInMs == null) {
			logger.info("Timed bucket sorting not used");
		} else {
			logger.info("Bucket size: " + bucketSizeInMs + "ms");
			logger.info("Bucket edge type: " + bucketEdgeTypeStr);
		}

		if (defaultMergeOperator == null) {
			logger.info("No default merge operator specified");
		} else {
			logger.info("Default merge operator: " + defaultMergeOperator);
		}

		if (matchingFieldName1 == null) {
			logger.info("No matching field 1 specified");
		} else {
			logger.info("Matching field 1: " + matchingFieldName1);
		}

		if (matchingFieldName2 == null) {
			logger.info("No matching field 2 specified");
		} else {
			logger.info("Matching field 2: " + matchingFieldName2);
		}

		if (filterDuplicates) {
			logger.info("Duplicated streams will be filtered");
		} else {
			logger.info("Duplicated streams will not be filtered");
		}

		if (filterInputStreamDuplicates) {
			logger.info("Duplicated entries coming from the same source (InputStreamName) will be filtered");
		} else {
			logger.info("Duplicated entries coming from the same source (InputStreamName) will not be filtered");
		}

		if (!duplIgnoreFields.isEmpty()) {
			if (filterDuplicates) {
				duplicatesIgnoreFields = new String[duplIgnoreFields.size()];
				duplIgnoreFields.toArray(duplicatesIgnoreFields);
				for (String s : duplIgnoreFields) {
					logger.info("field " + s + " will be ignorded during duplicate filtering");
				}
			} else {
				logger.warn(DUPLICATES_IGNORE_FIELDS + " has no effect without enabling " + FILTER_DUPLICATES);
			}

		}

		logger.info("Merging list:");
		for (DataField df : mergedFieldList) {
			logger.info("	" + df.getName() + ": " + getOperator(df.getName()));
		}

		// start clean-up timer
		CleanupTimerTask timerTask = new CleanupTimerTask();
		cleanupTimer.scheduleAtFixedRate(timerTask, CLEANUP_TIMER_PERIOD, CLEANUP_TIMER_PERIOD);

		return ret;
	}

	/**
	 * Checks if the given value exists in the output structure of the virtual sensor.
	 *
	 * @param value The value to be checked against the output structure.
	 * @return {@code true} if the value exists in the output structure, {@code false} otherwise.
	 */
	private boolean isInOutputStructure(String value) {
		for (DataField d : getVirtualSensorConfiguration().getOutputStructure()) {
			if (d.getName().compareToIgnoreCase(value) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the operator associated with the given field name.
	 *
	 * @param fieldName The name of the field for which to retrieve the operator.
	 * @return The operator associated with the given field name. If no specific operator is found, the default merge operator is returned.
	 */
	private Operator getOperator(String fieldName) {
		Operator op = FieldNameToOperatorMap.get(fieldName);
		if (op == null) {
			op = defaultMergeOperator;
		}
		return op;
	}
	/**
	 * Called when new data is available from a specific input stream.
	 *
	 * @param inputStreamName The name of the input stream.
	 * @param data The StreamElement object containing the new data.
	 */
	@Override
	public void dataAvailable(String inputStreamName, StreamElement data) {
		Serializable match1 = null;
		StreamElement se = data;
		if (matchingFieldName1 != null) {
			match1 = se.getData(matchingFieldName1);
		}
		try {
			cleanupLock.acquire();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			// add missing fields to stream element
			se = addMissingFields(se);

			if (!streamElementBuffer.containsKey(match1)) {
				streamElementBuffer.put(match1,
						Collections.synchronizedMap(new HashMap<Serializable, ArrayList<StreamElementContainer>>()));
				if (logger.isDebugEnabled() && match1 != null) {
					logger.debug("New StreamElement buffer created for " + matchingFieldName1 + " " + match1);
				}

			}

			Long dataTime = (Long) se.getData(timeline);
			if (bufferNow == null) {
				bufferNow = dataTime;
			} else if (dataTime.compareTo(bufferNow) > 0 && dataTime.compareTo(bufferNow + bufferSizeInMs) <= 0) {
				bufferNow = dataTime;
			}
			if (dataTime.compareTo(newestData) > 0) {
				newestData = dataTime;
			}

			processPerDeviceData(inputStreamName, se, streamElementBuffer.get(match1));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		cleanupLock.release();
	}

	/**
	 * Adds missing fields to the given StreamElement object.
	 *
	 * @param data The StreamElement object to which missing fields will be added.
	 * @return A new StreamElement object with the missing fields added.
	 */
	private StreamElement addMissingFields(StreamElement data) {
		ArrayList<DataField> dataFields = new ArrayList<DataField>();
		for (DataField df : mergedDataFields) {
			if (data.getType(df.getName()) == null) {
				dataFields.add(df);
			}

		}

		if (dataFields.isEmpty()) {
			return data;
		}

		DataField[] dfArray = new DataField[dataFields.size()];
		dataFields.toArray(dfArray);
		Serializable[] d = new Serializable[dataFields.size()];

		return new StreamElement(data, dfArray, d);
	}

	/**
	 * Processes the data for a specific input stream and merges it with other streams based on matching criteria.
	 *
	 * @param inputStreamName The name of the input stream.
	 * @param data The StreamElement object containing the new data.
	 * @param streamElementContainerMap The map storing the StreamElementContainer objects for each matching criteria.
	 * @throws Exception If an error occurs during the processing.
	 */
	private void processPerDeviceData(String inputStreamName, StreamElement data,
			Map<Serializable, ArrayList<StreamElementContainer>> streamElementContainerMap) throws Exception {
		Serializable match2 = null;
		if (matchingFieldName2 != null) {
			match2 = data.getData(matchingFieldName2);
		}

		ArrayList<StreamElementContainer> streamElementContainerList = streamElementContainerMap.get(match2);

		if (streamElementContainerList == null) {
			streamElementContainerList = new ArrayList<StreamElementContainer>();
			if (logger.isDebugEnabled() && matchingFieldName2 != null) {
				logger.debug("New StreamElementContainer list created for " + matchingFieldName2 + " " + match2);
			}
		}

		Iterator<StreamElementContainer> iter = streamElementContainerList.iterator();
		while (iter.hasNext()) {
			StreamElementContainer sec = iter.next();

			if (sec.checkTime((Long) data.getData(timeline))) {
				if (sec.putStreamElement(inputStreamName, data)) {
					int mergedStreams = sec.getNumberOfStreams();
					StreamElement newSE = sec.getMergedStreamElement();
					iter.remove();
					if (streamElementContainerMap.isEmpty()) {
						streamElementContainerMap.remove(match2);
					} else {
						streamElementContainerMap.put(match2, streamElementContainerList);
					}

					super.dataAvailable("mergedStream",
							new StreamElement(newSE, statisticsDataFields, generateStats(mergedStreams, newSE)));
				} else {
					streamElementContainerMap.put(match2, streamElementContainerList);
				}
				return;
			}
		}

		totalBufferedStreams++;
		StreamElementContainer sec = new StreamElementContainer(inputStreamName, data);
		streamElementContainerList.add(sec);
		streamElementContainerMap.put(match2, streamElementContainerList);
	}

	@Override
	public synchronized void dispose() {
		cleanupTimer.cancel();
		cleanupBuffers(true);
		super.dispose();
	}

	/**
	 * Cleans up the buffers used for storing stream data, removing outdated elements based on the cleanup criteria.
	 *
	 * @param cleanupAll If true, all elements in the buffers will be cleaned up. 
	 * 					If false, only elements older than the buffer timestamp 
	 * 					minus the buffer size will be cleaned up.
	 */
	private void cleanupBuffers(boolean cleanupAll) {
		try {
			cleanupLock.acquire();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("cleanupBuffers: start");
			}
			if (bufferNow != null) {
				Long bNow = bufferNow;
				Long cleanups = 0L;
				Iterator<Map<Serializable, ArrayList<StreamElementContainer>>> iter1 = streamElementBuffer.values()
						.iterator();
				while (iter1.hasNext()) {
					Map<Serializable, ArrayList<StreamElementContainer>> buf = iter1.next();
					Iterator<ArrayList<StreamElementContainer>> iter2 = buf.values().iterator();
					while (iter2.hasNext()) {
						ArrayList<StreamElementContainer> secList = iter2.next();
						Iterator<StreamElementContainer> iter3 = secList.iterator();
						while (iter3.hasNext()) {
							StreamElementContainer sec = iter3.next();
							if (cleanupAll || (sec.getNewestTimestamp().compareTo((bNow - bufferSizeInMs))) < 0) {
								StreamElement mergedSE = sec.getMergedStreamElement();
								int numberOfStreams = sec.getNumberOfStreams();
								iter3.remove();
								cleanups++;
								super.dataAvailable("mergedStream", new StreamElement(mergedSE, statisticsDataFields,
										generateStats(numberOfStreams, mergedSE)));
							}
						}
						if (secList.isEmpty()) {
							iter2.remove();
						}

					}
					if (buf.isEmpty()) {
						iter1.remove();
					}

				}

				if (bNow.compareTo(newestData - bufferSizeInMs) < 0) {
					bufferNow = newestData;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("cleanupBuffers: cleanups=" + cleanups);
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		cleanupLock.release();
	}

	/**
	 * Generates statistics based on the number of merged streams and the total number of buffered streams.
	 *
	 * @param mergedStreams The number of streams that have been merged.
	 * @param se The StreamElement object representing the merged stream.
	 * @return An array of Serializable objects containing the generated statistics. 
	 * The first element represents the number of merged streams, 
	 * and the second element represents the total number 
	 * of streams remaining in the buffer after the merge.
	 */
	private Serializable[] generateStats(Integer mergedStreams, StreamElement se) {
		totalBufferedStreams -= mergedStreams;

		return new Serializable[] { mergedStreams, totalBufferedStreams };
	}

	class StreamElementContainer {
		private ArrayList<StreamElementInputStreamNameTuple> streamElementTuples;
		private StreamElement newSE;
		private StreamElement oldSE;
		private Long bucketStartTime = null;
		private Long bucketEndTime = null;

		/**
		 * Constructs a new StreamElementContainer object with the provided input stream name and stream element.
		 *
		 * @param inputStreamName The name of the input stream associated with the stream element.
		 * @param streamElement The StreamElement object to be stored in the container.
		 */
		protected StreamElementContainer(String inputStreamName, StreamElement streamElement) {
			streamElementTuples = new ArrayList<StreamElementInputStreamNameTuple>(bucketSpace);
			streamElementTuples.add(new StreamElementInputStreamNameTuple(inputStreamName, streamElement));
			newSE = streamElement;
			oldSE = streamElement;

			if (bucketSizeInMs != null) {
				Long time = (Long) streamElement.getData(timeline);
				switch (bucketEdgeType) {
					case STATIC:
						bucketStartTime = time - (time % bucketSizeInMs);
						bucketEndTime = bucketStartTime + bucketSizeInMs;
						break;
					case DYNAMIC:
						bucketStartTime = time - (bucketSizeInMs / 2);
						bucketEndTime = bucketStartTime + bucketSizeInMs;
						break;
					default:
						break;
				}
			}
		}
		/**
		 * Checks whether a given timestamp falls within the current time range.
		 *
		 * @param incomingTime The timestamp to be checked.
		 * @return True if the timestamp falls within the current time range, false otherwise.
		 */
		protected boolean checkTime(Long incomingTime) {
			return (bucketStartTime == null) || ((incomingTime >= bucketStartTime) && (incomingTime < bucketEndTime));
		}

		/**
		 * Adds a new StreamElement to the streamElementTuples list, while performing checks to filter out duplicates based on the specified flags.
		 *
		 * @param inputStreamName The name of the input stream associated with the StreamElement.
		 * @param streamElement The StreamElement object to be added.
		 * @return True if the StreamElement is successfully added, false if it is considered a duplicate and should be discarded.
		 * @throws Exception If the streamElementTuples list is already full.
		 */
		protected boolean putStreamElement(String inputStreamName, StreamElement streamElement) throws Exception {
			if (streamElementTuples.size() == bucketSpace) {
				throw new Exception("StreamElementContainer already full!");
			}

			if (filterDuplicates || filterInputStreamDuplicates) {
				// discard duplicates
				for (StreamElementInputStreamNameTuple se : streamElementTuples) {
					if (filterDuplicates && se.getStreamElement().equalsIgnoreTimedAndFields(streamElement,
							duplicatesIgnoreFields)) {
						
						if(logger.isDebugEnabled()){
							logger.debug("discard duplicate in streamElementTuples container: (incoming)["
								+ streamElement.toString() + "], (existing)[" + se.getStreamElement().toString() + "]");
						}
						return false;
					}
					if (filterInputStreamDuplicates && inputStreamName.compareTo(se.getInputStreamName()) == 0) {
						logger.warn(
								"discard duplicate Source (InputStreamName) in streamElementTuples container: (incoming)Source="
										+ inputStreamName + ", StreamElement=[" + streamElement.toString() + "]");
						return false;
					}
				}
			}

			totalBufferedStreams++;
			streamElementTuples.add(new StreamElementInputStreamNameTuple(inputStreamName, streamElement));

			if (bucketEdgeType != null && bucketEdgeType == BucketEdgeType.DYNAMIC) {
				long time = 0;
				for (StreamElementInputStreamNameTuple se : streamElementTuples) {
					time += (Long) se.getStreamElement().getData(timeline);
				}

				time = time / streamElementTuples.size();
				bucketStartTime = time - (bucketSizeInMs / 2);
				bucketEndTime = bucketStartTime + bucketSizeInMs;
			}

			if (((Long) streamElement.getData(timeline)).compareTo((Long) newSE.getData(timeline)) > 0) {
				newSE = streamElement;
			}

			if (((Long) streamElement.getData(timeline)).compareTo((Long) oldSE.getData(timeline)) < 0) {
				oldSE = streamElement;
			}

			return streamElementTuples.size() == bucketSpace;
		}

		protected int getNumberOfStreams() {
			return streamElementTuples.size();
		}

		protected Long getNewestTimestamp() {
			return (Long) newSE.getData(timeline);
		}

		/**
		 * Generates a merged StreamElement by performing specific operations on the data fields of the virtual sensor.
		 *
		 * @return The merged StreamElement object.
		 */
		protected StreamElement getMergedStreamElement() {
			Serializable[] mergedData = new Serializable[mergedDataFields.length];

			for (int i = 0; i < mergedDataFields.length; i++) {
				switch (getOperator(mergedDataFields[i].getName())) {
					case NEW:
						mergedData[i] = newFnc(mergedDataFields[i]);
						break;
					case OLD:
						mergedData[i] = old(mergedDataFields[i]);
						break;
					case ADD:
						mergedData[i] = add(mergedDataFields[i]);
						break;
					case AVG:
						mergedData[i] = avg(mergedDataFields[i]);
						break;
					case MIN:
						mergedData[i] = min(mergedDataFields[i]);
						break;
					case MAX:
						mergedData[i] = max(mergedDataFields[i]);
						break;
					default:
						break;
				}
			}

			return new StreamElement(mergedDataFields, mergedData);
		}

		private Serializable newFnc(DataField dataField) {
			return newSE.getData(dataField.getName());
		}

		private Serializable old(DataField dataField) {
			return oldSE.getData(dataField.getName());
		}

		/**
		 * Performs addition operations on the data fields of the virtual sensor.
		 *
		 * @param d The data field on which the addition operation is performed.
		 * @return The result of the addition operation.
		 */
		private Serializable add(DataField d) {
			Serializable toReturn = null;
			switch (d.getDataTypeID()) {
				case DataTypes.DOUBLE:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							if (toReturn == null) {
								toReturn = (Double) srl;
							} else {
								toReturn = (Double) toReturn + (Double) srl;
							}
						}
					}
					break;
				case DataTypes.BIGINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							if (toReturn == null) {
								toReturn = (Long) srl;
							} else {
								toReturn = (Long) toReturn + (Long) srl;
							}
						}
					}
					break;
				case DataTypes.INTEGER:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							if (toReturn == null) {
								toReturn = (Integer) srl;
							} else {
								toReturn = (Integer) toReturn + (Integer) srl;
							}
						}
					}
					break;
				case DataTypes.SMALLINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							if (toReturn == null) {
								toReturn = srl;
							} else {
								toReturn = (short) ((Short) toReturn + (Short) srl);
							}
						}
					}
					break;
				case DataTypes.TINYINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							if (toReturn == null) {
								toReturn = (Byte) srl;
							} else {
								toReturn = (byte) ((Byte) toReturn + (Byte) srl);
							}
						}
					}
					break;
				case DataTypes.CHAR:
				case DataTypes.VARCHAR:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							if (toReturn == null) {
								toReturn = (String) srl;
							} else {
								toReturn = ((String) toReturn).concat((String) srl);
							}
						}
					}
					break;
				case DataTypes.BINARY:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							if (toReturn == null) {
								toReturn = (byte[]) srl;
							} else {
								int mergedDataLength = ((byte[]) toReturn).length;
								int srlLength = ((byte[]) srl).length;
								byte[] concat = new byte[mergedDataLength + srlLength];
								System.arraycopy(toReturn, 0, concat, 0, mergedDataLength);
								System.arraycopy(srl, 0, concat, mergedDataLength, srlLength);
								toReturn = concat;
							}
						}
					}
					break;
				default:
					logger.error("type id " + d.getDataTypeID() + " not allowed with ADD operator");
					break;
			}
			return toReturn;
		}

		/**
		 * Calculates the average of the data fields of the virtual sensor.
		 *
		 * @param d The data field on which the average calculation is performed.
		 * @return The calculated average.
		 */
		private Serializable avg(DataField d) {
			Serializable toReturn = null;
			int divider = 0;
			switch (d.getDataTypeID()) {
				case DataTypes.DOUBLE:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							divider++;
							if (toReturn == null) {
								toReturn = (Double) srl;
							} else {
								toReturn = (Double) toReturn + (Double) srl;
							}
						}
					}
					if (toReturn != null) {
						toReturn = (Double) toReturn / new Double(divider);
					}
					break;
				case DataTypes.BIGINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							divider++;
							if (toReturn == null) {
								toReturn = (Long) srl;
							} else {
								toReturn = (Long) toReturn + (Long) srl;
							}

						}
					}
					if (toReturn != null) {
						toReturn = (Long) ((Long) toReturn / new Long(divider));
					}
					break;
				case DataTypes.INTEGER:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							divider++;
							if (toReturn == null) {
								toReturn = new Long((Integer) srl);
							} else {
								toReturn = (Long) toReturn + new Long((Integer) srl);
							}
						}
					}
					if (toReturn != null) {
						toReturn = (Integer) ((Long) ((Long) toReturn / new Long(divider))).intValue();
					}
					break;
				case DataTypes.SMALLINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							divider++;
							if (toReturn == null) {
								toReturn = new Long((Short) srl);
							} else {
								toReturn = (Long) toReturn + new Long((Short) srl);
							}

						}
					}
					if (toReturn != null) {
						toReturn = (Short) ((Long) ((Long) toReturn / new Long(divider))).shortValue();
					}
					break;
				case DataTypes.TINYINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null) {
							divider++;
							if (toReturn == null) {
								toReturn = new Long((Byte) srl);
							} else {
								toReturn = (Long) toReturn + new Long((Byte) srl);
							}

						}
					}
					if (toReturn != null) {
						toReturn = (Byte) ((Long) ((Long) toReturn / new Long(divider))).byteValue();
					}

					break;
				default:
					logger.error("type id " + d.getDataTypeID() + " not allowed with AVG operator");
					break;
			}
			return toReturn;
		}

		/**
		 * Finds the minimum value among the data fields of the virtual sensor.
		 *
		 * @param d The data field on which the minimum value calculation is performed.
		 * @return The minimum value found among the data fields.
		 */
		private Serializable min(DataField d) {
			Serializable toReturn = null;
			switch (d.getDataTypeID()) {
				case DataTypes.DOUBLE:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Double) srl < (Double) toReturn)) {
							toReturn = (Double) srl;
						}

					}
					break;
				case DataTypes.BIGINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Long) srl < (Long) toReturn)) {
							toReturn = (Long) srl;
						}
					}
					break;
				case DataTypes.INTEGER:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Integer) srl < (Integer) toReturn)) {
							toReturn = (Integer) srl;
						}

					}
					break;
				case DataTypes.SMALLINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Short) srl < (Short) toReturn)) {
							toReturn = srl;
						}

					}
					break;
				case DataTypes.TINYINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Byte) srl < (Byte) toReturn)) {
							toReturn = (Byte) srl;
						}
					}
					break;
				default:
					logger.error("type id " + d.getDataTypeID() + " not allowed with MIN operator");
					break;
			}
			return toReturn;
		}

		/**
		 * Finds the maximum value among the data fields of the virtual sensor.
		 *
		 * @param d The data field on which the maximum value calculation is performed.
		 * @return The maximum value found among the data fields.
		 */
		private Serializable max(DataField d) {
			Serializable toReturn = null;
			switch (d.getDataTypeID()) {
				case DataTypes.DOUBLE:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Double) srl > (Double) toReturn)) {
							toReturn = (Double) srl;
						}

					}
					break;
				case DataTypes.BIGINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Long) srl > (Long) toReturn)) {
							toReturn = (Long) srl;
						}
					}
					break;
				case DataTypes.INTEGER:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Integer) srl > (Integer) toReturn)) {
							toReturn = (Integer) srl;
						}
					}
					break;
				case DataTypes.SMALLINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Short) srl > (Short) toReturn)) {
							toReturn = srl;
						}
					}
					break;
				case DataTypes.TINYINT:
					for (StreamElementInputStreamNameTuple se : streamElementTuples) {
						Serializable srl = se.getStreamElement().getData(d.getName());
						if (srl != null && (toReturn == null || (Byte) srl > (Byte) toReturn)) {
							toReturn = (Byte) srl;
						}
					}
					break;
				default:
					logger.error("type id " + d.getDataTypeID() + " not allowed with MAX operator");
					break;
			}
			return toReturn;
		}
	}

	class StreamElementInputStreamNameTuple {
		private String inputStreamName;
		private StreamElement streamElement;
		/**
		 * Constructs a new instance of the StreamElementInputStreamNameTuple class.
		 *
		 * @param inputStreamName The name of the input stream associated with the tuple.
		 * @param streamElement   The StreamElement object representing the data element.
		 */
		protected StreamElementInputStreamNameTuple(String inputStreamName, StreamElement streamElement) {
			this.inputStreamName = inputStreamName;
			this.streamElement = streamElement;
		}

		protected String getInputStreamName() {
			return inputStreamName;
		}

		protected StreamElement getStreamElement() {
			return streamElement;
		}
	}

	class CleanupTimerTask extends TimerTask {
		@Override
		public void run() {
			cleanupBuffers(false);
		}

	}

}

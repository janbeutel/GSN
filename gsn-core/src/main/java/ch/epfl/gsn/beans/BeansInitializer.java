package ch.epfl.gsn.beans;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.collections.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.epfl.gsn.utils.KeyValueImp;
import ch.epfl.gsn.config.*;

// TOD: this class was refactored in the process of cutting ropes with scala
// However i don't know why they used optionals when they fall back to null

public class BeansInitializer {
	private transient static final Logger logger = LoggerFactory.getLogger(BeansInitializer.class);

	/**
	 * Constructs a ContainerConfig based on the provided GsnConf configuration.
	 * If sliding configuration is defined in GsnConf, it sets up the sliding
	 * configuration.
	 * Otherwise, the sliding configuration is set to null.
	 *
	 * @param gsn The GsnConf configuration object.
	 * @return A ContainerConfig representing the configuration for the container.
	 */
	public static ContainerConfig container(GsnConf gsn) {
		SlidingConfig sliding = new SlidingConfig();
		// TODO: check why it needs to be set to null - destroying the use case of optional
		if (gsn.slidingConf().isPresent()) {
			sliding.setStorage(storage(gsn.slidingConf().get()));
		} else {
			sliding = null;
		}
		ContainerConfig con = new ContainerConfig(
				gsn.monitorPort(), gsn.timeFormat(),
				gsn.zmqConf().enabled(), gsn.zmqConf().proxyPort(), gsn.zmqConf().metaPort(),
				storage(gsn.storageConf()), sliding, gsn.maxDBConnections(), gsn.maxSlidingDBConnections(), gsn.backlogCommandsConf().enabled(), gsn.backlogCommandsConf().backlogCommandsPort());

		return con;
	}

	/**
	 * Constructs a StorageConfig based on the provided StorageConf configuration.
	 * If the identifier is defined in StorageConf, it sets up the identifier.
	 * Otherwise, the identifier is set to null.
	 *
	 * @param st The StorageConf configuration object.
	 * @return A StorageConfig representing the configuration for storage.
	 */
	public static StorageConfig storage(StorageConf st) {
		StorageConfig con = new StorageConfig();
		con.setIdentifier(st.identifier().orElse(null));
		con.setJdbcDriver(st.driver());
		con.setJdbcURL(st.url());
		con.setJdbcUsername(st.user());
		con.setJdbcPassword(st.pass());
		return con;
	}

	/**
	 * Constructs a DataField based on the provided FieldConf configuration.
	 * Sets the name, type, description, index, and unit attributes of the
	 * DataField.
	 * If the index or unit is not defined in FieldConf, it sets the corresponding
	 * attribute to null.
	 *
	 * @param fc The FieldConf configuration object.
	 * @return A DataField representing the configuration for a data field.
	 */
	public static DataField dataField(FieldConf fc) {
		DataField f = new DataField();
		f.setName(fc.name().toLowerCase());
		f.setType(fc.dataType());
		f.setDescription(fc.description());
		f.setIndex(fc.index().orElse("false"));
		f.setUnit(fc.unit().orElse(null)); // TODO: check why the opted to use null although they used optional initially
		return f;
	}

	/**
	 * Constructs a WebInput based on the provided WebInputCommand configuration.
	 * Sets the parameters and name attributes of the WebInput.
	 *
	 * @param wi The WebInputCommand configuration object.
	 * @return A WebInput representing the configuration for a web input.
	 */
	public static WebInput webInput(WebInputCommand wi) {
		WebInput w = new WebInput();
		DataField[] par = new DataField[(wi.params().size())];
		for (int i = 0; i < par.length; i++) {
			par[i] = dataField(wi.params().get(i));
		}
		w.setParameters(par);
		w.setName(wi.name());
		return w;
	}

	/**
	 * Constructs a DataField based on the provided FieldConf configuration.
	 * Sets the name, type, description, index, and unit attributes of the
	 * DataField.
	 * If the index or unit is not defined in FieldConf, it sets the corresponding
	 * attribute to null.
	 *
	 * @param fc The FieldConf configuration object.
	 * @return A DataField representing the configuration for a data field.
	 */
	public static StreamSource source(SourceConf sc) {
		StreamSource s = new StreamSource();
		s.setAlias(sc.alias());
		s.setSqlQuery(sc.query());
		if (sc.slide().isPresent()) {
			s.setRawSlideValue(sc.slide().get());
		}

		if (sc.samplingRate().isPresent()) {
			s.setSamplingRate(sc.samplingRate().get().floatValue());
		}

		if (sc.disconnectBufferSize().isPresent()) {
			s.setDisconnectedBufferSize(sc.disconnectBufferSize().get());
		}

		if (sc.storageSize().isPresent()) {
			s.setRawHistorySize(sc.storageSize().get());
		}

		AddressBean[] add = new AddressBean[sc.wrappers().size()];
		int i = 0;
		// for (WrapperConf w : JavaConversions.asJavaIterable(sc.wrappers())) {
		for (WrapperConf w : sc.wrappers()) {
			add[i] = address(w);
			i++;
		}
		s.setAddressing(add);
		return s;
	}

	/**
	 * Constructs an AddressBean based on the provided WrapperConf configuration.
	 * Sets the parameters, partial order key, and output attributes of the
	 * AddressBean.
	 *
	 * @param w The WrapperConf configuration object.
	 * @return An AddressBean representing the configuration for an address
	 *         (wrapper).
	 */
	public static AddressBean address(WrapperConf w) {
		KeyValueImp[] p = new KeyValueImp[w.params().size()];
		// Iterable<String> keys = JavaConversions.asJavaIterable(w.params().keys());
		Iterable<String> keys = w.params().keySet();
		int i = 0;
		for (String k : keys) {
			p[i] = new KeyValueImp(k, w.params().get(k));
			i++;
		}
		AddressBean a = new AddressBean(w.wrapper(), p);
		if (w.partialKey().isPresent()) {
			a.setPartialOrderKey(w.partialKey().get());
		}
		DataField[] out = new DataField[(w.output().size())];
		for (int j = 0; j < out.length; j++) {
			out[j] = dataField(w.output().get(j));
		}
		a.setVsconfig(out);
		return a;
	}

	/**
	 * Constructs an InputStream based on the provided StreamConf configuration.
	 * Sets the input stream name, count, rate, query, and sources attributes of the
	 * InputStream.
	 *
	 * @param s The StreamConf configuration object.
	 * @return An InputStream representing the configuration for a data stream.
	 * @see StreamConf
	 * @see InputStream
	 * @see StreamSource
	 */
	public static InputStream stream(StreamConf s) {
		InputStream is = new InputStream();
		is.setInputStreamName(s.name());
		is.setCount(Long.valueOf(s.count()));
		is.setRate(s.rate());
		is.setQuery(s.query());
		StreamSource[] ss = new StreamSource[s.sources().size()];
		for (int j = 0; j < ss.length; j++) {
			ss[j] = source(s.sources().get(j));
		}
		is.setSources(ss);
		return is;
	}

	/**
	 * Constructs a VSensorConfig based on the provided VsConf configuration.
	 * Sets the main class, description, name, timestamp uniqueness, lifecycle pool
	 * size,
	 * output stream rate, priority, initialization priority, addressing, input
	 * streams,
	 * web input parameters, output structure, main class initial parameters, and
	 * storage configuration.
	 *
	 * @param vs The VsConf configuration object.
	 * @return A VSensorConfig representing the configuration for a virtual sensor.
	 */
	public static VSensorConfig vsensor(VsConf vs) {
		VSensorConfig v = new VSensorConfig();
		v.setMainClass(vs.processing().className());
		v.setDescription(vs.description());
		v.setName(vs.name());
		v.setIsTimeStampUnique(vs.processing().uniqueTimestamp());
		if (vs.poolSize().isPresent()) {
			v.setLifeCyclePoolSize(vs.poolSize().get());
		}

		if (vs.processing().rate().isPresent()) {
			v.setOutputStreamRate(vs.processing().rate().get());
		}

		v.setPriority(vs.priority());
		v.setInitPriority(vs.initPriority());
		KeyValueImp[] addr = new KeyValueImp[vs.address().size()];
		// Iterable<String> keys = JavaConversions.asJavaIterable(vs.address().keys());
		Iterable<String> keys = vs.address().keySet();
		int i = 0;
		for (String k : keys) {
			addr[i] = new KeyValueImp(k, vs.address().get(k));
			i++;
		}
		v.setAddressing(addr);
		InputStream[] is = new InputStream[vs.streams().size()];
		for (int j = 0; j < is.length; j++) {
			is[j] = stream(vs.streams().get(j));
		}
		v.setInputStreams(is);
		if (vs.processing().webInput().isPresent()) {
			WebInputConf wic = vs.processing().webInput().get();
			v.setWebParameterPassword(wic.password());
			WebInput[] wi = new WebInput[wic.commands().size()];
			for (int j = 0; j < wi.length; j++) {
				wi[j] = webInput(wic.commands().get(j));
			}
			v.setWebInput(wi);
		}
		DataField[] out = new DataField[(vs.processing().output().size())];
		for (int j = 0; j < out.length; j++) {
			out[j] = dataField(vs.processing().output().get(j));
		}
		v.setOutputStructure(out);
		Map<String, String> init = vs.processing().initParams();
		ArrayList<KeyValue> ini = new ArrayList<KeyValue>();
		// Iterable<String> initkeys = JavaConversions.asJavaIterable(init.keys());
		Iterable<String> initkeys = init.keySet();
		for (String ik : initkeys) {
			logger.trace("keys:" + ik);
			ini.add(new KeyValueImp(ik.toLowerCase(), init.get(ik)));
		}
		v.setMainClassInitialParams(ini);

		StorageConfig st = new StorageConfig();
		if (vs.storageSize().isPresent()) {
			st.setStorageSize(vs.storageSize().get());
		}
		if (vs.storageDirectory().isPresent()) {
			st.setStorageDirectory(vs.storageDirectory().get());
		}
		if (vs.storage().isPresent()) {
			StorageConf sc = vs.storage().get();
			if (sc.identifier().isPresent()) {
				st.setIdentifier(sc.identifier().get());
			}
			st.setJdbcDriver(sc.driver());
			st.setJdbcURL(sc.url());
			st.setJdbcUsername(sc.user());
			st.setJdbcPassword(sc.pass());
		}
		if (st.getStorageSize() != null || st.getJdbcURL() != null) {
			v.setStorage(st);
		}
		if(vs.chunkSize().isPresent()){
			v.setChunkSize(vs.chunkSize().get());
		}
		
		return v;
	}

}

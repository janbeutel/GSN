package ch.epfl.gsn.beans;

import java.util.ArrayList;

import org.apache.commons.collections.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.epfl.gsn.utils.KeyValueImp;
import scala.collection.JavaConversions;
import scala.collection.Map;
import ch.epfl.gsn.config.*;

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
		if (gsn.slidingConf().isDefined()) {
			sliding.setStorage(storage(gsn.slidingConf().get()));
		} else {
			sliding = null;
		}
		ContainerConfig con = new ContainerConfig(
				gsn.monitorPort(), gsn.timeFormat(),
				gsn.zmqConf().enabled(), gsn.zmqConf().proxyPort(), gsn.zmqConf().metaPort(),
				storage(gsn.storageConf()), sliding, gsn.maxDBConnections(), gsn.maxSlidingDBConnections());

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
		if (st.identifier().isDefined()) {
			con.setIdentifier(st.identifier().get());
		} else {
			con.setIdentifier(null);
		}
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
		if (fc.index().isDefined()) {
			f.setIndex(fc.index().get());
		} else {
			f.setIndex("false");
		}
		if (fc.unit().isDefined()) {
			f.setUnit(fc.unit().get());
		} else {
			f.setUnit(null);
		}
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
			par[i] = dataField(wi.params().apply(i));
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
		if (sc.slide().isDefined()) {
			s.setRawSlideValue(sc.slide().get());
		}

		if (sc.samplingRate().isDefined()) {
			s.setSamplingRate(((Double) sc.samplingRate().get()).floatValue());
		}

		if (sc.disconnectBufferSize().isDefined()) {
			s.setDisconnectedBufferSize(((Integer) sc.disconnectBufferSize().get()));
		}

		if (sc.storageSize().isDefined()) {
			s.setRawHistorySize(sc.storageSize().get());
		}

		AddressBean[] add = new AddressBean[sc.wrappers().size()];
		int i = 0;
		for (WrapperConf w : JavaConversions.asJavaIterable(sc.wrappers())) {
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
		Iterable<String> keys = JavaConversions.asJavaIterable(w.params().keys());
		int i = 0;
		for (String k : keys) {
			p[i] = new KeyValueImp(k, w.params().apply(k));
			i++;
		}
		AddressBean a = new AddressBean(w.wrapper(), p);
		if (w.partialKey().isDefined()) {
			a.setPartialOrderKey(w.partialKey().get());
		}
		DataField[] out = new DataField[(w.output().size())];
		for (int j = 0; j < out.length; j++) {
			out[j] = dataField(w.output().apply(j));
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
			ss[j] = source(s.sources().apply(j));
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
		if (vs.poolSize().isDefined()) {
			v.setLifeCyclePoolSize(((Integer) vs.poolSize().get()));
		}

		if (vs.processing().rate().isDefined()) {
			v.setOutputStreamRate(((Integer) vs.processing().rate().get()));
		}

		v.setPriority(vs.priority());
		v.setInitPriority(vs.initPriority());
		KeyValueImp[] addr = new KeyValueImp[vs.address().size()];
		Iterable<String> keys = JavaConversions.asJavaIterable(vs.address().keys());
		int i = 0;
		for (String k : keys) {
			addr[i] = new KeyValueImp(k, vs.address().apply(k));
			i++;
		}
		v.setAddressing(addr);
		InputStream[] is = new InputStream[vs.streams().size()];
		for (int j = 0; j < is.length; j++) {
			is[j] = stream(vs.streams().apply(j));
		}
		v.setInputStreams(is);
		if (vs.processing().webInput().isDefined()) {
			WebInputConf wic = vs.processing().webInput().get();
			v.setWebParameterPassword(wic.password());
			WebInput[] wi = new WebInput[wic.commands().size()];
			for (int j = 0; j < wi.length; j++) {
				wi[j] = webInput(wic.commands().apply(j));
			}
			v.setWebInput(wi);
		}
		DataField[] out = new DataField[(vs.processing().output().size())];
		for (int j = 0; j < out.length; j++) {
			out[j] = dataField(vs.processing().output().apply(j));
		}
		v.setOutputStructure(out);
		Map<String, String> init = vs.processing().initParams();
		ArrayList<KeyValue> ini = new ArrayList<KeyValue>();
		Iterable<String> initkeys = JavaConversions.asJavaIterable(init.keys());
		for (String ik : initkeys) {
			logger.trace("keys:" + ik);
			ini.add(new KeyValueImp(ik.toLowerCase(), init.apply(ik)));
		}
		v.setMainClassInitialParams(ini);

		StorageConfig st = new StorageConfig();
		if (vs.storageSize().isDefined()) {
			st.setStorageSize(vs.storageSize().get());
		}
		if (vs.storage().isDefined()) {
			StorageConf sc = vs.storage().get();
			if (sc.identifier().isDefined()) {
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
		return v;
	}

}

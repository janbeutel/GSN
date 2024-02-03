/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* 
* This file is part of GSN.
* 
* GSN is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* GSN is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with GSN.  If not, see <http://www.gnu.org/licenses/>.
* 
* File: src/ch/epfl/gsn/beans/InputStream.java
*
* @author Mehdi Riahi
* @author gsn_devs
* @author Ali Salehi
* @author Timotee Maret
*
*/

package ch.epfl.gsn.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.VirtualSensor;
import ch.epfl.gsn.VirtualSensorInitializationFailedException;
import ch.epfl.gsn.utils.CaseInsensitiveComparator;
import ch.epfl.gsn.vsensor.AbstractVirtualSensor;

import org.slf4j.Logger;

public class InputStream implements Serializable {

	private static final long serialVersionUID = 6910141410904878762L;

	public static final int INITIAL_DELAY_5000MSC = 5000;

	private transient static final Logger logger = LoggerFactory.getLogger(InputStream.class);

	// private transient StorageManager storageMan = StorageManager.getInstance();

	private String inputStreamName;

	private Long count /* = Long.MAX_VALUE */;

	private transient long currentCount = 1;

	private int rate;

	private String query;

	private StreamSource[] sources;

	private HashMap<CharSequence, StreamSource> streamSourceAliasNameToStreamSourceName = new HashMap<CharSequence, StreamSource>();

	private transient VirtualSensor pool;

	private final transient TreeMap<CharSequence, CharSequence> rewritingData = new TreeMap<CharSequence, CharSequence>(
			new CaseInsensitiveComparator());

	private transient long lastVisited = 0;

	private StringBuilder rewrittenSQL;

	private boolean queryCached;

	private transient boolean hasValidated = false;

	private transient boolean cachedValidationResult = false;

	/**
	 * For making one initial delay.
	 */

	public String getQuery() {
		return this.query;
	}

	public void setQuery(final String sql) {
		this.query = sql;
	}

	public String getInputStreamName() {
		return this.inputStreamName;
	}

	public void setInputStreamName(final String inputStreamName) {
		this.inputStreamName = inputStreamName;
	}

	public Long getCount() {
		if (this.count == null || this.count == 0) {
			this.count = Long.MAX_VALUE;
		}
		return this.count;
	}

	public void setCount(final Long count) {
		this.count = count;
	}

	public int getRate() {
		return this.rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public StreamSource[] getSources() {
		return sources;
	}

	public StreamSource getSource(final String streamSourceName) {
		return this.streamSourceAliasNameToStreamSourceName.get(streamSourceName);
	}

	public void setSources(StreamSource... ss) {
		this.sources = ss;
	}

	/**
	 * This method is called by the Stream Source timed Stream-Source has new
	 * results.
	 * 
	 * @param alias The alias of the StreamSource which has new data.
	 * @throws SQLException
	 */
	// public boolean dataAvailable ( final CharSequence alias ) throws SQLException
	// {
	// if ( logger.isDebugEnabled( ) ) logger.debug( new StringBuilder( ).append(
	// "Notified by StreamSource on the alias: " ).append( alias ).toString( ) );
	// if ( this.pool == null ) {
	// logger.debug( "The input is dropped b/c the VSensorInstance is not set yet."
	// );
	// return false;
	// }
	//
	// if ( this.currentCount > this.getCount( ) ) {
	// if ( logger.isInfoEnabled( ) ) logger.info( "Maximum count reached, the value
	// *discarded*" );
	// return false;
	// }
	//
	// final long currentTimeMillis = System.currentTimeMillis( );
	// if ( this.rate > 0 && ( currentTimeMillis - this.lastVisited ) < this.rate )
	// {
	// if ( logger.isInfoEnabled( ) ) logger.info( "Called by *discarded* b/c of the
	// rate limit reached." );
	// return false;
	// }
	// this.lastVisited = currentTimeMillis;
	//
	// if ( this.rewrittenSQL == null ) {
	// this.rewrittenSQL = new StringBuilder( SQLUtils.newRewrite( getQuery( ).trim(
	// ).toLowerCase( ), this.rewritingData ));
	// if ( logger.isDebugEnabled( ) )
	// logger.debug( new StringBuilder( ).append( "Rewritten SQL: " ).append(
	// this.rewrittenSQL ).append( "(" ).append( this.storageMan.isThereAnyResult(
	// this.rewrittenSQL ) ).append( ")" )
	// .toString( ) );
	// }
	// if ( StorageManager.getInstance( ).isThereAnyResult( this.rewrittenSQL ) ) {
	// this.currentCount++;
	// AbstractVirtualSensor sensor = null;
	// if ( logger.isDebugEnabled( ) ) logger.debug( new StringBuilder( ).append(
	// "Executing the main query for InputStream : " ).append(
	// this.getInputStreamName( ) ).toString( ) );
	// int elementCounterForDebugging = -1;
	// final Enumeration < StreamElement > resultOfTheQuery =
	// StorageManager.getInstance( ).executeQuery( this.rewrittenSQL , false );
	// try {
	// sensor = pool.borrowVS( );
	// while ( resultOfTheQuery.hasMoreElements( ) ) {
	// elementCounterForDebugging++;
	// StreamElement element= resultOfTheQuery.nextElement( );
	// sensor.dataAvailable( this.getInputStreamName( ) , element );
	// }
	// } catch ( final PoolIsFullException e ) {
	// logger.warn( "The stream element produced by the virtual sensor is dropped
	// because of the following error : " );
	// logger.warn( e.getMessage( ) , e );
	// } catch ( final UnsupportedOperationException e ) {
	// logger.warn( "The stream element produced by the virtual sensor is dropped
	// because of the following error : " );
	// logger.warn( e.getMessage( ) , e );
	// } catch ( final VirtualSensorInitializationFailedException e ) {
	// logger.error( "The stream element can't deliver its data to the virtual
	// sensor " + sensor.getVirtualSensorConfiguration( ).getName( )
	// + " because initialization of that virtual sensor failed" );
	// logger.error(e.getMessage(),e);
	// } finally {
	// this.pool.returnVS( sensor );
	// }
	// if ( logger.isDebugEnabled( ) ) {
	// logger.debug( new StringBuilder( ).append( "Input Stream's result has *"
	// ).append( elementCounterForDebugging ).append( "* stream elements"
	// ).toString( ) );
	// }
	// }
	// return true;
	// }

	public void addToRenamingMapping(final CharSequence aliasName, final CharSequence viewName) {
		rewritingData.put(aliasName, viewName);
	}

	public final TreeMap<CharSequence, CharSequence> getRenamingMapping() {
		return rewritingData;
	}

	public void refreshAlias(final String alias) {
		logger.info("REFERES ALIAS CALEED");
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof InputStream)) {
			return false;
		}

		final InputStream inputStream = (InputStream) o;

		if (this.inputStreamName != null ? !this.inputStreamName.equals(inputStream.inputStreamName)
				: inputStream.inputStreamName != null) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		return (this.inputStreamName == null ? 0: this.inputStreamName.hashCode());
	}

	public void release() {
		final HashMap map = new HashMap();
		this.release(map);
	}

	public void release(final HashMap context) {
	}


	/**
	 * Validates the input stream by checking if it has valid sources.
	 * If the input stream has already been validated, the cached validation result
	 * is returned.
	 * If the input stream has no sources specified, an error message is logged and
	 * false is returned.
	 * If any of the stream sources are not valid, an error message is logged and
	 * false is returned.
	 * Otherwise, the input stream is considered valid and true is returned.
	 *
	 * @return true if the input stream is valid, false otherwise
	 */
	public boolean validate() {
		if (this.hasValidated) {
			return this.cachedValidationResult;
		}
		hasValidated = true;
		if (sources == null || sources.length == 0) {
			logger.error("Input Stream " + getInputStreamName()
					+ " is not valid (No stream sources are specified), deployment failed !");
			return false;
		}
		for (StreamSource ss : sources) {
			if (!ss.validate()) {
				logger.error("The Stream Source : " + ss.getAlias() + " specified in the Input Stream : "
						+ getInputStreamName() + " is not valid.");
				return (cachedValidationResult = false);
			}
			streamSourceAliasNameToStreamSourceName.put(ss.getAlias(), ss);
		}
		return (cachedValidationResult = true);
	}

	/**
	 * @return the pool
	 */
	public VirtualSensor getPool() {
		return pool;
	}

	/**
	 * @param pool the pool to set
	 */
	public void setPool(VirtualSensor pool) {
		this.pool = pool;
	}

	public void invalidateCachedQuery(StreamSource streamSource) {
		queryCached = false;
		rewrittenSQL = null;
	}

	/**
	 * Executes a query on the specified stream source alias and processes the
	 * results.
	 * The method handles various conditions such as the VSensorInstance not being
	 * set,
	 * reaching the maximum count, and rate limiting. If the query is cached and
	 * there are
	 * results, it executes the main query and passes the stream elements to the
	 * corresponding
	 * virtual sensor.
	 *
	 * @param alias The alias of the stream source.
	 * @return {@code true} if the query is successfully executed, {@code false}
	 *         otherwise.
	 * @throws SQLException If a SQL exception occurs during query execution.
	 */
	public boolean executeQuery(final CharSequence alias) throws SQLException {
		if(logger.isDebugEnabled()){
			logger.debug("Notified by StreamSource on the alias: " + alias);
		}
		
		if (this.pool == null) {
			if(logger.isDebugEnabled()){
				logger.debug("The input is dropped b/c the VSensorInstance is not set yet.");
			}
			return false;
		}

		if (this.currentCount > this.getCount()) {
			logger.info("Maximum count reached, the value *discarded*");
			return false;
		}

		final long currentTimeMillis = System.currentTimeMillis();
		if (this.rate > 0 && (currentTimeMillis - this.lastVisited) < this.rate) {
			logger.info("Called by *discarded* b/c of the rate limit reached.");
			return false;
		}
		this.lastVisited = currentTimeMillis;

		if (!queryCached) {
			rewriteQuery();
			if (queryCached && logger.isDebugEnabled()) {
					logger.debug(new StringBuilder().append("Rewritten SQL: ").append(this.rewrittenSQL).append("(")
						.append(Main.getWindowStorage().isThereAnyResult(this.rewrittenSQL)).append(")")
						.toString());
			}

		}
		int elementCounterForDebugging = 0;
		if (queryCached && Main.getWindowStorage().isThereAnyResult(this.rewrittenSQL)) {
			this.currentCount++;
			AbstractVirtualSensor sensor = null;
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder().append("Executing the main query for InputStream : ")
					.append(this.getInputStreamName()).toString());
			}

			final Enumeration<StreamElement> resultOfTheQuery = Main.getWindowStorage().executeQuery(this.rewrittenSQL,
					false);
			try {
				sensor = pool.borrowVS();
				while (resultOfTheQuery.hasMoreElements()) {
					elementCounterForDebugging++;
					StreamElement element = resultOfTheQuery.nextElement();
					sensor.dataAvailable_decorated(this.getInputStreamName(), element);
				}
			} catch (final UnsupportedOperationException e) {
				logger.warn(
						"The stream element produced by the virtual sensor is dropped because of the following error : "
								+ e.getMessage());
			} catch (final VirtualSensorInitializationFailedException e) {
				logger.error("The stream element can't deliver its data to the virtual sensor "
						+ sensor.getVirtualSensorConfiguration().getName()
						+ " because initialization of that virtual sensor failed: " + e.getMessage());
			} finally {
				this.pool.returnVS(sensor);
			}

		}
		if(logger.isDebugEnabled()){
			logger.debug(new StringBuilder().append("Input Stream's result has *").append(elementCounterForDebugging)
				.append("* stream elements").toString());
		}
		
		return true;
	}

	/**
	 * Rewrites the query by applying rewriting rules from the stream sources.
	 * If the rewriting fails for any source, an error message is logged and the
	 * query is not rewritten.
	 * The rewritten query is stored in the 'rewrittenSQL' variable.
	 * The 'queryCached' flag is set to true indicating that the query has been
	 * rewritten and cached.
	 */
	private void rewriteQuery() {
		String query = getQuery().trim().toLowerCase();
		for (int i = 0; i < sources.length; i++) {
			StringBuilder sb = sources[i].rewrite(query);
			if (sb == null) {
				logger.error("Rewriting query failed. The rewrite() method of the stream source <"
						+ sources[i].getAlias() + "> returned null.");
				rewrittenSQL = null;
				queryCached = false;
				return;
			} else {
				query = sb.toString();
			}
		}
		rewrittenSQL = new StringBuilder(query);
		queryCached = true;
	}
}

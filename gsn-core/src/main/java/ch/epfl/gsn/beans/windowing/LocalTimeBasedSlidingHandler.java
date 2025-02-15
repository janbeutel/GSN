/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* Copyright (c) 2020-2023, University of Innsbruck
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
* File: src/ch/epfl/gsn/beans/windowing/LocalTimeBasedSlidingHandler.java
*
* @author gsn_devs
* @author Ali Salehi
* @author bgpearn
* @author Mehdi Riahi
* @author Timotee Maret
* @author Sofiane Sarni
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.beans.windowing;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.StreamSource;
import ch.epfl.gsn.storage.SQLUtils;
import ch.epfl.gsn.utils.CaseInsensitiveComparator;
import ch.epfl.gsn.utils.GSNRuntimeException;
import ch.epfl.gsn.wrappers.AbstractWrapper;

import org.slf4j.Logger;

public class LocalTimeBasedSlidingHandler implements SlidingHandler {

    private static final transient Logger logger = LoggerFactory.getLogger(LocalTimeBasedSlidingHandler.class);
    private static int timerCount = 0;
    private List<StreamSource> streamSources;
    private AbstractWrapper wrapper;
    private Timer timer;
    private long timerTick = -1;
    private Map<StreamSource, Long> slidingHashMap;

    public LocalTimeBasedSlidingHandler(AbstractWrapper wrapper) {
        streamSources = Collections.synchronizedList(new ArrayList<StreamSource>());
        slidingHashMap = Collections.synchronizedMap(new HashMap<StreamSource, Long>());
        timer = new Timer("LocalTimeBasedSlidingHandlerTimer" + (++timerCount));
        this.wrapper = wrapper;
    }

    /**
     * Adds a StreamSource to the LocalTimeBasedSlidingHandler.
     * If the StreamSource's windowing type is not TIME_BASED_SLIDE_ON_EACH_TUPLE,
     * it updates the slidingHashMap and timerTick values accordingly.
     * If the timerTick value changes, it cancels the current timer task and
     * schedules a new one.
     * If the StreamSource's windowing type is TIME_BASED_SLIDE_ON_EACH_TUPLE,
     * it simply adds the StreamSource to the streamSources list.
     *
     * @param streamSource the StreamSource to be added
     */
    public void addStreamSource(StreamSource streamSource) {
        SQLViewQueryRewriter rewriter = new LTBSQLViewQueryRewriter();
        rewriter.setStreamSource(streamSource);
        rewriter.initialize();
        if (streamSource.getWindowingType() == WindowType.TIME_BASED_SLIDE_ON_EACH_TUPLE) {
            streamSources.add(streamSource);
        } else {
            long oldTimerTick = timerTick;
            if (streamSource.getWindowingType() == WindowType.TIME_BASED) {
                slidingHashMap.put(streamSource,
                        streamSource.getParsedSlideValue() - streamSource.getParsedStorageSize());
                if (timerTick == -1) {
                    timerTick = GCD(streamSource.getParsedStorageSize(), streamSource.getParsedSlideValue());
                } else {
                    timerTick = GCD(timerTick,
                            GCD(streamSource.getParsedStorageSize(), streamSource.getParsedSlideValue()));
                }
            } else {
                slidingHashMap.put(streamSource, 0L);
                if (timerTick == -1) {
                    timerTick = streamSource.getParsedSlideValue();
                } else {
                    timerTick = GCD(timerTick, streamSource.getParsedSlideValue());
                }
            }
            if (oldTimerTick != timerTick) {
                timer.cancel();
                timer = new Timer();
                if(logger.isDebugEnabled()){
                    logger.debug("About to schedule new timer task at period " + timerTick + "ms in the "
                        + wrapper.getDBAliasInStr() + " wrapper");
                }
                timer.schedule(new LTBTimerTask(), 500, timerTick);
            }
        }
    }

    public long GCD(long a, long b) {
        return WindowingUtil.GCD(a, b);
    }

    private class LTBTimerTask extends TimerTask {

        /**
         * Executes the sliding window operation for each stream source in the
         * slidingHashMap.
         * If the slide variable is greater than or equal to the parsed slide value of
         * the stream source,
         * the slide variable is reset to 0 and the dataAvailable method of the stream
         * source's query rewriter is called.
         * The slide variable is then updated in the slidingHashMap.
         */
        @Override
        public void run() {
            synchronized (slidingHashMap) {
                for (StreamSource streamSource : slidingHashMap.keySet()) {
                    long slideVar = slidingHashMap.get(streamSource) + timerTick;
                    if (slideVar >= streamSource.getParsedSlideValue()) {
                        slideVar = 0;
                        streamSource.getQueryRewriter().dataAvailable(System.currentTimeMillis());
                    }
                    slidingHashMap.put(streamSource, slideVar);
                }
            }
        }
    }

    /**
     * Checks if data is available for processing in the sliding window.
     *
     * @param streamElement the stream element to check for availability
     * @return true if data is available, false otherwise
     */
    public boolean dataAvailable(StreamElement streamElement) {
        boolean toReturn = false;
        synchronized (streamSources) {
            for (StreamSource streamSource : streamSources) {
                if (streamSource.getWindowingType() == WindowType.TIME_BASED_SLIDE_ON_EACH_TUPLE) {
                    toReturn = streamSource.getQueryRewriter().dataAvailable(streamElement.getTimeStamp()) || toReturn;
                }
            }
        }
        return toReturn;
    }

    /**
     * Retrieves the cutting condition for the windowing process.
     * The cutting condition is a string representation of the condition used to
     * determine the boundaries of the window.
     * It is based on the maximum window size, maximum slide value, and maximum
     * tuple count of the stream sources.
     * If the cutting condition cannot be determined, the method returns a default
     * condition.
     *
     * @return The cutting condition as a string.
     */
    public String getCuttingCondition() {
        long timed1 = -1;
        long timed2 = -1;
        long maxTupleCount = 0;
        long maxSlideForTupleBased = 0;
        long maxWindowSize = 0;

        synchronized (streamSources) {
            for (StreamSource streamSource : streamSources) {
                maxWindowSize = Math.max(maxWindowSize, streamSource.getParsedStorageSize());
            }
        }

        synchronized (slidingHashMap) {
            for (StreamSource streamSource : slidingHashMap.keySet()) {
                if (streamSource.getWindowingType() == WindowType.TIME_BASED) {
                    maxWindowSize = Math.max(maxWindowSize,
                            streamSource.getParsedStorageSize() + streamSource.getParsedSlideValue());
                } else {
                    maxSlideForTupleBased = Math.max(maxSlideForTupleBased, streamSource.getParsedSlideValue());
                    maxTupleCount = Math.max(maxTupleCount, streamSource.getParsedStorageSize());
                }
            }
        }

        if (maxWindowSize > 0) {
            timed1 = System.currentTimeMillis() - maxWindowSize;
        }

        if (maxTupleCount > 0) {
            StringBuilder query = new StringBuilder();
            if (Main.getWindowStorage().isH2() || Main.getWindowStorage().isMysqlDB()) {
                query.append(" select timed from ").append(wrapper.getDBAliasInStr()).append(" where timed <= ");
                query.append(System.currentTimeMillis() - maxSlideForTupleBased)
                        .append(" order by timed desc limit 1 offset ").append(
                                maxTupleCount - 1);
            } else if (Main.getWindowStorage().isSqlServer()) {
                query.append(" select min(timed) from (select top ").append(maxTupleCount).append(" * ")
                        .append(" from ").append(
                                wrapper.getDBAliasInStr())
                        .append(" where timed <= ").append(System.currentTimeMillis() - maxSlideForTupleBased)
                        .append(" order by timed desc) as X  ");
            }
            
            if(logger.isDebugEnabled()){
                logger.debug("Query for getting oldest timestamp : " + query);
            }
            Connection conn = null;
            try {
                ResultSet resultSet = Main.getWindowStorage().executeQueryWithResultSet(query,
                        conn = Main.getWindowStorage().getConnection());
                if (resultSet.next()) {
                    timed2 = resultSet.getLong(1);
                } else {
                    return "timed < -1";
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            } finally {
                Main.getWindowStorage().close(conn);
            }
        }

        if (timed1 >= 0 && timed2 >= 0) {
            return "timed < " + Math.min(timed1, timed2);
        }

        return "timed < " + ((timed1 == -1) ? timed2 : timed1);
    }

    /**
     * Removes a stream source from the sliding handler.
     * This method removes the specified stream source from the list of stream
     * sources,
     * removes its corresponding entry from the sliding hash map,
     * disposes the query rewriter associated with the stream source,
     * and updates the timer tick.
     *
     * @param streamSource the stream source to be removed
     */
    public void removeStreamSource(StreamSource streamSource) {
        streamSources.remove(streamSource);
        slidingHashMap.remove(streamSource);
        streamSource.getQueryRewriter().dispose();
        updateTimerTick();
    }

    /**
     * Updates the timer tick value and schedules a new timer task if necessary.
     * The timer tick value is calculated based on the windowing type and slide
     * value of each stream source.
     * If the timer tick value has changed and is greater than 0, a new timer task
     * is scheduled.
     */
    private void updateTimerTick() {
        long oldTimerTick = timerTick;
        // recalculating timer tick
        timerTick = -1;
        synchronized (slidingHashMap) {
            for (StreamSource streamSource : slidingHashMap.keySet()) {
                if (streamSource.getWindowingType() == WindowType.TIME_BASED) {
                    slidingHashMap.put(streamSource,
                            streamSource.getParsedSlideValue() - streamSource.getParsedStorageSize());
                    if (timerTick == -1) {
                        timerTick = GCD(streamSource.getParsedStorageSize(), streamSource.getParsedSlideValue());
                    } else {
                        timerTick = GCD(timerTick,
                                GCD(streamSource.getParsedStorageSize(), streamSource.getParsedSlideValue()));
                    }
                } else {
                    slidingHashMap.put(streamSource, 0L);
                    if (timerTick == -1) {
                        timerTick = streamSource.getParsedSlideValue();
                    } else {
                        timerTick = GCD(timerTick, streamSource.getParsedSlideValue());
                    }
                }
            }
        }
        if (oldTimerTick != timerTick && timerTick > 0) {
            timer.cancel();
            timer = new Timer();
            if(logger.isDebugEnabled()){
                logger.debug("About to schedule new timer task at period " + timerTick + "ms in the "
                    + wrapper.getDBAliasInStr() + " wrapper");
            }
            timer.schedule(new LTBTimerTask(), 500, timerTick);
        }
    }

    /**
     * Disposes the resources used by the LocalTimeBasedSlidingHandler.
     * This method releases any acquired resources and clears the internal data
     * structures.
     */
    public void dispose() {
        synchronized (streamSources) {
            for (StreamSource streamSource : streamSources) {
                streamSource.getQueryRewriter().dispose();
            }
            streamSources.clear();
        }
        synchronized (slidingHashMap) {
            for (StreamSource streamSource : slidingHashMap.keySet()) {
                streamSource.getQueryRewriter().dispose();
            }
            slidingHashMap.clear();
        }
    }

    public boolean isInterestedIn(StreamSource streamSource) {
        return WindowType.isTimeBased(streamSource.getWindowingType());
    }

    private class LTBSQLViewQueryRewriter extends SQLViewQueryRewriter {

        /**
         * Overrides the createViewSQL method to generate a SQL query for creating a
         * view.
         *
         * @return A CharSequence representing the SQL query for creating the view.
         * @throws GSNRuntimeException If certain conditions are not met, such as a null
         *                             wrapper object or failed validation.
         * @see GSNRuntimeException
         */
        @Override
        public CharSequence createViewSQL() {
            if (cachedSqlQuery != null) {
                return cachedSqlQuery;
            }
            if (streamSource.getWrapper() == null) {
                throw new GSNRuntimeException("Wrapper object is null, most probably a bug, please report it !");
            }
            if (!streamSource.validate()) {
                throw new GSNRuntimeException(
                        "Validation of this object the stream source failed, please check the logs.");
            }
            CharSequence wrapperAlias = streamSource.getWrapper().getDBAliasInStr();
            long windowSize = streamSource.getParsedStorageSize();
            if (streamSource.getSamplingRate() == 0 || windowSize == 0) {
                return cachedSqlQuery = new StringBuilder("select * from ").append(wrapperAlias).append(" where 1=0");
            }
            TreeMap<CharSequence, CharSequence> rewritingMapping = new TreeMap<CharSequence, CharSequence>(
                    new CaseInsensitiveComparator());
            rewritingMapping.put("wrapper", wrapperAlias);

            String sqlQuery = streamSource.getSqlQuery();
            StringBuilder toReturn = new StringBuilder();

            int fromIndex = sqlQuery.indexOf(" from ");
            if (Main.getWindowStorage().isH2() && fromIndex > -1) {
                toReturn.append(sqlQuery.substring(0, fromIndex + 6)).append(" (select * from ")
                        .append(sqlQuery.substring(fromIndex + 6));
            } else {
                toReturn.append(sqlQuery);
            }

            if (sqlQuery.toLowerCase().indexOf(" where ") < 0) {
                toReturn.append(" where ");
            } else {
                toReturn.append(" and ");
            }

            if (streamSource.getSamplingRate() != 1) {
                if (Main.getWindowStorage().isH2()) {
                    toReturn.append("( timed - (timed / 100) * 100 < ").append(streamSource.getSamplingRate() * 100)
                            .append(") and ");
                } else {
                    toReturn.append("( mod( timed , 100)< ").append(streamSource.getSamplingRate() * 100)
                            .append(") and ");
                }
            }

            WindowType windowingType = streamSource.getWindowingType();
            if (windowingType == WindowType.TIME_BASED_SLIDE_ON_EACH_TUPLE) {

                toReturn.append("(wrapper.timed >");
                if (Main.getWindowStorage().isH2()) {
                    toReturn.append(" (NOW_MILLIS()");
                } else if (Main.getWindowStorage().isMysqlDB()) {
                    toReturn.append(" (UNIX_TIMESTAMP()*1000");
                } else if (Main.getWindowStorage().isPostgres()) {
                    toReturn.append(" (extract(epoch FROM now())*1000");
                } else if (Main.getWindowStorage().isSqlServer()) {
                    // NOTE1 : The value retuend is in seconds (hence 1000)
                    // NOTE2 : There is no time in the date for the epoch, maybe
                    // doesn't match with the current system time, needs
                    // checking.
                    toReturn.append(" (convert(bigint,datediff(second,'1/1/1970',current_timestamp))*1000 )");
                }

                long timeDifferenceInMillis = storageManager.getTimeDifferenceInMillis();
                // System.out.println(timeDifferenceInMillis);
                toReturn.append(" - ").append(windowSize).append(" - ").append(timeDifferenceInMillis).append(" )");
                if (Main.getWindowStorage().isH2() || Main.getWindowStorage().isMysqlDB()) {
                    toReturn.append(") order by timed desc ");
                }

            } else {
                if (windowingType == WindowType.TIME_BASED) {

                    toReturn.append("timed in (select timed from ").append(wrapperAlias)
                            .append(" where timed <= (select timed from ")
                            .append(SQLViewQueryRewriter.VIEW_HELPER_TABLE).append(" where U_ID='")
                            .append(streamSource.getUIDStr()).append(
                                    "') and timed >= (select timed from ")
                            .append(SQLViewQueryRewriter.VIEW_HELPER_TABLE).append(
                                    " where U_ID='")
                            .append(streamSource.getUIDStr()).append("') - ").append(windowSize).append(" ) ");
                    if (Main.getWindowStorage().isH2() || Main.getWindowStorage().isMysqlDB()) {
                        toReturn.append(" order by timed desc ");
                    }

                } else {// WindowType.TUPLE_BASED_WIN_TIME_BASED_SLIDE

                    if (Main.getWindowStorage().isMysqlDB()) {
                        toReturn.append("timed <= (select timed from ").append(SQLViewQueryRewriter.VIEW_HELPER_TABLE)
                                .append(
                                        " where U_ID='")
                                .append(streamSource.getUIDStr()).append("') and timed >= (select timed from ");
                        toReturn.append(wrapperAlias).append(" where timed <= (select timed from ");
                        toReturn.append(SQLViewQueryRewriter.VIEW_HELPER_TABLE).append(" where U_ID='")
                                .append(streamSource.getUIDStr());
                        toReturn.append("') ").append(" order by timed desc limit 1 offset ").append(windowSize - 1)
                                .append(" )");
                        toReturn.append(" order by timed desc ");
                    } else if (Main.getWindowStorage().isH2()) {
                        toReturn.append("timed <= (select timed from ").append(SQLViewQueryRewriter.VIEW_HELPER_TABLE)
                                .append(
                                        " where U_ID='")
                                .append(streamSource.getUIDStr())
                                .append("') and timed >= (select distinct(timed) from ");
                        toReturn.append(wrapperAlias).append(" where timed in (select timed from ").append(wrapperAlias)
                                .append(
                                        " where timed <= (select timed from ");
                        toReturn.append(SQLViewQueryRewriter.VIEW_HELPER_TABLE).append(" where U_ID='")
                                .append(streamSource.getUIDStr());
                        toReturn.append("') ").append(" order by timed desc limit 1 offset ").append(windowSize - 1)
                                .append(" ))");
                        toReturn.append(" order by timed desc ");
                    } else if (Main.getWindowStorage().isSqlServer()) {
                        toReturn.append("timed in (select TOP ").append(windowSize).append(" timed from ")
                                .append(wrapperAlias).append(
                                        " where timed <= (select timed from ")
                                .append(SQLViewQueryRewriter.VIEW_HELPER_TABLE).append(" where U_ID='")
                                .append(streamSource.getUIDStr()).append("') order by timed desc ) ");
                    }
                }
            }

            if (Main.getWindowStorage().isH2() && fromIndex > -1) {
                toReturn.append(")");
            }
            toReturn = new StringBuilder(SQLUtils.newRewrite(toReturn, rewritingMapping));

            if(logger.isDebugEnabled()){
                logger.debug(new StringBuilder().append("The original Query : ").append(sqlQuery).toString());
                logger.debug(new StringBuilder().append("The merged query : ").append(toReturn.toString())
                        .append(" of the StreamSource ").append(streamSource.getAlias()).append(" of the InputStream: ")
                        .append(
                                streamSource.getInputStream().getInputStreamName())
                        .append("").toString());
            }

            return cachedSqlQuery = toReturn;
        }
    }
}

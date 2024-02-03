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
* File: src/ch/epfl/gsn/storage/hibernate/DBConnectionInfo.java
*
* @author Timotee Maret
*
*/

package ch.epfl.gsn.storage.hibernate;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBConnectionInfo {
    private String driverClass, url, userName, password;

    public DBConnectionInfo(String driverClass, String url, String userName, String password) {
        this.driverClass = driverClass;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    // public BasicDataSource createDataSource() {
    // return createDataSource(25, 5, -1); //TODO: tune parameters
    // }

    // public BasicDataSource createDataSource(int maxActive, int maxIde, int
    // maxWait) {
    // return createDataSource(maxActive, maxIde, maxWait, 1000 * 60 * 30, -1);
    // }

    /**
     * //@param maxActive The maximum number of active connections that can be
     * allocated from this pool at the same time, or negative for no limit. (def: 8)
     * //@param maxIde The maximum number of connections that can remain idle in the
     * pool, without extra ones being released, or negative for no limit. (def: 8)
     * //@param maxWait The maximum number of milliseconds that the pool will wait
     * (when there are no available connections) for a connection to be returned
     * before throwing an exception, or -1 to wait indefinitely. (def:indefinitely)
     * //@param minEvictableIdleTimeMillis The minimum amount of time an object may
     * sit idle in the pool before it is eligable for eviction by the idle object
     * evictor (if any). (def: 1000 * 60 * 30)
     * //@param timeBetweenEvictionRunsMillis The number of milliseconds to sleep
     * between runs of the idle object evictor thread. When non-positive, no idle
     * object evictor thread will be run. (def: -1)
     * //@return the configured BasicDataSource
     */
    /*
     * public BasicDataSource createDataSource(int maxActive, int maxIde, int
     * maxWait, long minEvictableIdleTimeMillis, long timeBetweenEvictionRunsMillis)
     * {
     * BasicDataSource ds = new BasicDataSource();
     * ds.setDriverClassName(driverClass);
     * ds.setUsername(userName);
     * ds.setPassword(password);
     * ds.setUrl(url);
     * 
     * ds.setMaxActive(maxActive);
     * ds.setMaxIdle(maxIde);
     * ds.setMaxWait(maxWait);
     * ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
     * ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
     * return ds;
     * }
     */

    public boolean equals(Object o) {
        if (null == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DBConnectionInfo that = (DBConnectionInfo) o;

        if (driverClass == null ? that.driverClass != null : !driverClass.equals(that.driverClass)) {
            return false;
        }
        if (password == null ?  that.password != null : !password.equals(that.password)) {
            return false;
        }
        if (url == null ? that.url != null : !url.equals(that.url)) {
            return false;
        }
        if (userName == null ? that.userName != null : !userName.equals(that.userName)) {
            return false;
        }

        return true;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int hashCode() {
        int result = driverClass == null ? 0 : driverClass.hashCode();
        result = 31 * result + (url == null ? 0 : url.hashCode());
        result = 31 * result + (userName == null ? 0 : userName.hashCode());
        result = 31 * result + (password == null ? 0 : password.hashCode());
        return Math.abs(result);
    }

    @Override
    public String toString() {
        return "DBConnectionInfo{" +
                "driverClass='" + driverClass + '\'' +
                ", url='" + url + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
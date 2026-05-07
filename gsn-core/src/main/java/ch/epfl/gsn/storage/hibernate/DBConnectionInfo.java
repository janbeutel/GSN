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
* File: src/ch/epfl/gsn/storage/hibernate/DBConnectionInfo.java
*
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.storage.hibernate;

public class DBConnectionInfo {
    private String driverClass, url, userName, password;

    public DBConnectionInfo(String driverClass, String url, String userName, String password) {
        this.driverClass = driverClass;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

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
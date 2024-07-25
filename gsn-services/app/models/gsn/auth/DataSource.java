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
* File: app/models/gsn/auth/DataSource.java
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package models.gsn.auth;

import java.util.List;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import be.objectify.deadbolt.java.models.Permission;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class DataSource extends Model implements Permission {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;
    
	public String value;
	
	public boolean is_public;
	
	@OneToMany(cascade = CascadeType.ALL)
	public List<UserDataSourceRead> userRead;
	
	@OneToMany(cascade = CascadeType.ALL)
	public List<UserDataSourceWrite> userWrite;

	@OneToMany(cascade = CascadeType.ALL)
	public List<GroupDataSourceRead> groupRead;

	@OneToMany(cascade = CascadeType.ALL)
	public List<GroupDataSourceWrite> groupWrite;
	
	public static final Finder<Long, DataSource> find = new Finder<>(DataSource.class);

	public static DataSource findByValue(String value) {
		return find.query().where().eq("value", value).findOne();
	}

	public String getValue() {
		return value;
	}
	
	public boolean getIs_public(){
		return is_public;
	}
	
	public void setIs_public(boolean p){
		is_public=p;
	}
}

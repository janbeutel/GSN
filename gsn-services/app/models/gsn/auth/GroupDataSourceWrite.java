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
* File: app/models/gsn/auth/GroupDataSourceWrite.java
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package models.gsn.auth;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;


import io.ebean.Finder;
import io.ebean.Model;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
@Table(name="group_data_source_write",  uniqueConstraints={
		   @UniqueConstraint(columnNames={"group_id", "data_source_id"})
		})
public class GroupDataSourceWrite extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;
    
	@ManyToOne
	@JoinColumn(name = "group_id")
	public Group group;
	
	@ManyToOne
	@JoinColumn(name = "data_source_id")
	public DataSource data_source;
	
	public static final Finder<Long, GroupDataSourceWrite> find = new Finder<>(GroupDataSourceWrite.class);

	public static List<GroupDataSourceWrite> findByUser(Group value) {
		return find.query().where().eq("group", value).findList();
	}
	
	public static List<GroupDataSourceWrite> findByDataSource(DataSource value) {
		return find.query().where().eq("data_source", value).findList();
	}
	
	public static GroupDataSourceWrite findByBoth(Group g, DataSource d) {
		return find.query().where().eq("group", g).eq("data_source", d).findOne();
	}

}

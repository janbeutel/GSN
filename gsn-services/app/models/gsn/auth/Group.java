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
* File: app/models/gsn/auth/Group.java
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package models.gsn.auth;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.ebean.Finder;
import io.ebean.Model;

@Entity
@Table(name = Group.tableName)
public class Group extends Model{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String tableName = "groups";

	@Id
	public Long id;

	public String name;
	public String description;
	
	@OneToMany(cascade = CascadeType.ALL)
	public List<GroupDataSourceRead> dataSourceRead;
	
	@OneToMany(cascade = CascadeType.ALL)
	public List<GroupDataSourceWrite> dataSourceWrite;
	
	@ManyToMany(mappedBy = "groups")
	public List<User> users;

	public static final Finder<Long, Group> find = new Finder<>(Group.class);

	public static Group findByName(String name) {
		return find.query().where().eq("name", name).findOne();
	}

	public String getName() {
		return name;
	}
}

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
* File: src/ch/epfl/gsn/vsensor/TestStreamExporterVirtualSensor.java
*
* @author Jerome Rousselot
* @author Ali Salehi
*
*/

package ch.epfl.gsn.vsensor;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.collections.KeyValue;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.Main;
import ch.epfl.gsn.utils.KeyValueImp;

public class TestStreamExporterVirtualSensor extends TestCase {
   
   private final String  user = "sa" , passwd = "" , url = "jdbc:h2:mem:.;DB_CLOSE_DELAY=-1" , streamName = "aJUnitTestStream";
   
   private VSensorConfig config;
   private static final String DRIVER_H2 = "org.h2.Driver";
   
   /*
    * To run some of these tests, a mysql server must be running on localhost
    * with the following configuration: a database 'gsntest' must exist. a user
    * 'gsntest' with password 'gsntest' must exist and have all privileges on
    * the 'gsntest' database. You can do it with the following: mysql -u -p root
    * create user 'gsntest' IDENTIFIED BY 'gsntest'; create database gsntest ;
    * grant ALL ON gsntest.* TO gsntest ; (non-Javadoc)
    * 
    * @see junit.framework.TestCase#setUp()
    */
   public void setUp ( ) {
      // Ensure GSN singletons/config are initialized; StreamExporter uses Main.getStorage().
      Main.getInstance();
      config = new VSensorConfig( );
      config.setName( "JUnitTestStreamExporterVS" );
      config.setFileName( "PlaceholderfileNameForJUNitTesting" );
      config.setOutputStructure( new DataField[] {} );
      
   }
   
   public void tearDown ( ) {
      config = null;
      try {
         DriverManager.registerDriver( new org.h2.Driver() );
         Connection connection = DriverManager.getConnection( url , user , passwd );
         connection.createStatement( ).execute( "DROP TABLE IF EXISTS " + streamName );
      } catch ( SQLException e ) {
          e.printStackTrace( );
      }
      
   }
   
   /*
    * Tries to instantiate a VS without the required arguments. Should always
    * fail.
    */
   public void testMissingAllEssentialParameters ( ) {
      StreamExporterVirtualSensor vs = new StreamExporterVirtualSensor( );
      vs.setVirtualSensorConfiguration( config );
      assertFalse( vs.initialize( ) );
   }
   
   /*
    * Tries to connect to a (supposedly) existing mysql db on local host. See
    * class comments for more info. Should succeed.
    */
   public void testConnectToExistingMySQLDB ( ) {
      StreamExporterVirtualSensor vs = new StreamExporterVirtualSensor( );
      ArrayList < KeyValue > params = new ArrayList < KeyValue >( );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_URL , url ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_USER , user ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_PASSWD , passwd ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_DRIVER , DRIVER_H2 ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.TABLE_NAME , streamName ) );
      // StreamExporterVS expects a numeric limit in 'entries'
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_ENTRIES , "1" ) );
      config.setMainClassInitialParams( params );
      vs.setVirtualSensorConfiguration( config );
      assertTrue( vs.initialize( ) );
   }
   
   /*
    * Tries to log a line into a Mysql table. The test stream generates data for
    * each possible data type.
    */
   public void testLogStatementIntoMySQLDB ( ) {
      StreamExporterVirtualSensor vs = new StreamExporterVirtualSensor( );
      // configure parameters
      ArrayList < KeyValue > params = new ArrayList < KeyValue >( );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_URL , url ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_USER , user ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_PASSWD , passwd ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_DRIVER , DRIVER_H2 ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.TABLE_NAME , streamName ) );
      params.add( new KeyValueImp( StreamExporterVirtualSensor.PARAM_ENTRIES , "1" ) );
      config.setMainClassInitialParams( params );
      vs.setVirtualSensorConfiguration( config );
      
      // configure datastream
      Vector < DataField > fieldTypes = new Vector < DataField >( );
      // Keep this test self-contained and robust: only verify insertion for
      // a simple VARCHAR field type.
      fieldTypes.add( new DataField( "value", DataTypes.VARCHAR ) );
      Serializable[] data = new Serializable[] { "A chain of chars" };

      // StreamExporterVS uses the output structure from the virtual sensor config
      // to create the destination table.
      config.setOutputStructure( fieldTypes.toArray( new DataField[] {} ) );

      vs.initialize( );
      
      long timeStamp = new Date( ).getTime( );
      StreamElement streamElement = new StreamElement( fieldTypes.toArray( new DataField[] {} ) , data , timeStamp );
      
      // give datastream to vs
      vs.dataAvailable( streamName , streamElement );
      
      // clean up and control
      boolean result = true;
      try {
         Connection connection = vs.getConnection();
         Statement statement = connection.createStatement( );
         ResultSet rs = statement.executeQuery( "SELECT COUNT(*) FROM " + streamName );
         rs.next();
         result = rs.getLong( 1 ) == 1L;
      } catch ( SQLException e ) {
         // TODO Auto-generated catch block
         e.printStackTrace( );
         result = false;
      }
      assertTrue( result );
   }
   
}

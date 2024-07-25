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
* File: app/controllers/gsn/api/GridService.scala
*
* @author Jean-Paul Calbimonte
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package controllers.gsn.api

import play.api.mvc._
import play.api.Play.current
import scala.concurrent.Future
import scala.util.Try
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Promise
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.Logger
import ch.epfl.gsn.data._
import ch.epfl.gsn.data.format._
import play.api.mvc.InjectedController
import javax.inject.Inject
import akka.actor._
import scala.concurrent.ExecutionContext

class GridService @Inject()(actorSystem: ActorSystem)(implicit ec: ExecutionContext) extends InjectedController with GsnService {   
  def gridData(sensorid:String) =grid(sensorid,EsriAscii)
  def gridTimeseries(sensorid:String)=grid(sensorid,Json,true)

  
  def grid(sensorid:String, 
      defaultFormat:OutputFormat,asTimeSeries:Boolean=false) = Action.async {implicit request=>
    Try{
      val vsname=sensorid.toLowerCase
    	
      val size:Option[Int]=queryparam("size").map(_.toInt)
      val fieldStr:Option[String]=queryparam("fields")
      val fromStr:Option[String]=queryparam("from")
      val toStr:Option[String]=queryparam("to")
      val timeFormat:Option[String]=queryparam("timeFormat")
      val boxStr:Option[String]=queryparam("box")
      val agg:Option[String]=queryparam("agg")

      val format=param("format",OutputFormat,defaultFormat)           
      val filters=new ArrayBuffer[String]
      val box:Option[Seq[Int]]=
        boxStr.map(b=>b.split(",").map(_.toInt))
      val fields:Array[String]=
        if (!fieldStr.isDefined) Array() 
        else fieldStr.get.split(",")
      if (fromStr.isDefined)          
        filters+= "timed>"+dateFormatter.parseDateTime(fromStr.get).getMillis
      if (toStr.isDefined)          
        filters+= "timed<"+dateFormatter.parseDateTime(toStr.get).getMillis
                              
      val p=Promise[Seq[SensorData]]               
      val q=actorSystem.actorOf(Props(new QueryActor(p)))
      Logger.debug("request the query actor")
      Logger.debug(s"values $vsname,$filters,$size,$timeFormat,$box,$agg,$asTimeSeries")
      q ! GetGridData(vsname,filters,size,timeFormat,box,agg,asTimeSeries)
      p.future.map{data=>        
        Logger.debug("before formatting")
                 
        format match{
            case EsriAscii=>
              val pp=EsriSerializer.ser(data.head,Seq(),false)
              Ok(pp)
            case Json=>
              val pp=JsonSerializer.ser(data.head,Seq(),false)
              Ok(pp)
            case _ => 
              val pp=JsonSerializer.ser(data.head,Seq(),false)
              Ok(pp)
          }
          
      }.recover{
        case t=>
          t.printStackTrace()
          BadRequest(t.getMessage)
      }
    }.recover{
      case t=>Future(BadRequest("Error: "+t.getMessage))
    }.get
  }
}
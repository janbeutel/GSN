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
* File: app/controllers/gsn/api/SensorService.scala
*
* @author Jean-Paul Calbimonte
* @author Julien Eberle
*
*/
package controllers.gsn.api

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.util.Try
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.zeromq.ZMQ
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Output => kOutput}
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile

import play.Logger
import play.api.mvc._
import play.api.Play.current
//import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.json.{Json => PlayJson}
import play.api.http.ContentTypes

import scalaoauth2.provider.AuthInfoRequest
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import ch.epfl.gsn.config.ConfWatcher
import ch.epfl.gsn.data._
import ch.epfl.gsn.data.format._
import ch.epfl.gsn.xpr.XprConditions
import ch.epfl.gsn.config.GetSensorConf
import ch.epfl.gsn.config.VsConf
import ch.epfl.gsn.beans.StreamElement
import play.api.Play.current
//import play.api.libs.concurrent.Akka
//import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.Logger
import scala.util.Success
import models.gsn.auth.User
import scalaoauth2.provider.AuthInfo
import java.io.ByteArrayOutputStream
import play.api.mvc.InjectedController
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import play.api.mvc.DefaultActionBuilder
import play.api.mvc.PlayBodyParsers
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.mvc.Results._
import service.gsn.UserProvider
import akka.pattern.ask
import akka.util.Timeout
import com.feth.play.module.pa.PlayAuthenticate
import controllers.gsn.GSNDataHandler
import controllers.gsn.APIPermissionAction

class SensorService @Inject()(actorSystem: ActorSystem, ec: ExecutionContext,playAuth: PlayAuthenticate) extends InjectedController with GsnService {   
  val defaultMetaProps=conf.getStringList("gsn.api.defaultMetadataProps")
  
  val kryo = new Kryo()

  def headings[A](act: Action[A])= Action.async(act.parser) { request =>
     act(request).map{_.withHeaders("Access-Control-Allow-Origin"->"*",
                                    "Access-Control-Allow-Methods"->"GET",
                                    "Access-Control-Allow-Headers"->"Content-Type")}
     
  }
/*
  def param[T](name:String,fun: String=>T,default:T)(implicit req:Request[AnyContent])=
    queryparam(name).map(fun(_)).getOrElse(default)
  */    

  def sensors = headings(Action.async {implicit request=>
    Try {
      val format = param("format", OutputFormat, defaultFormat)
      val latestVals = param("latestValues", _.toBoolean, false)
      val timeFormat = queryparam("timeFormat")
      
      val p=Promise[Seq[SensorData]]
      val st = actorSystem.actorSelection("/user/gsnSensorStore")
      val q = actorSystem.actorOf(Props(new QueryActor(p)))
      implicit val timeout: Timeout = Timeout(5.seconds)
      q ! GetAllSensors(latestVals,timeFormat)
      val defaultMetaPropsScala: Seq[String] = defaultMetaProps.asScala
      p.future.map { data =>
        val out = format match {
          case Json => JsonSerializer.ser(data, Seq(), latestVals)
          case Csv => CsvSerializer.ser(data, defaultMetaPropsScala, latestVals)
          case Xml => XmlSerializer.ser(data, defaultMetaPropsScala, latestVals)
          case Shapefile => ShapefileSerializer.ser(data, defaultMetaPropsScala, latestVals)
          case _ => JsonSerializer.ser(data, Seq(), latestVals)
        }
        result(out, format)
      }
    }.recover {
      case t =>
        t.printStackTrace
        Future(BadRequest(t.getMessage))
    }.get
  })
  
  def userInfo() = headings((APIPermissionAction(playAuth,false) compose Action).async {implicit request =>

     request match{
         case AuthInfoRequest(auth:AuthInfo[User],req) => Future(
         	Ok(PlayJson.obj("username" -> auth.user.name, "firstname" -> auth.user.firstName, "lastname" -> auth.user.lastName, "email" -> auth.user.email))
            )
         case _ => Future(Forbidden("Page only accessible with a valid oauth token."))
     }
  })
  
  def uploadSensorData(sensorid:String) = headings((APIPermissionAction(playAuth,true, sensorid) compose Action).async {implicit request =>
    Try{
      val vsname = sensorid.toLowerCase
      val data = request.body.asJson.get.toString
      val se = StreamElement.fromJSON(data)
      
      implicit val timeout = Timeout(1 seconds)
      val q=actorSystem.actorSelection("/user/gsnSensorStore/ConfWatcher")
      val p = q ? GetSensorConf(sensorid)
      p.map{c => c match{
        case conf:VsConf => {
            val wconfig = conf.streams.flatMap( s => s.sources.flatMap( so => so.wrappers ) ).filter( w => w.wrapper.equals("zeromq-push")).head
            val address = wconfig.params.get("local_address").getOrElse("localhost")
            val port = wconfig.params.get("local_port").orNull
            val context  = ZMQ.context(1)
    		    val forwarder = context.socket(ZMQ.REQ)
    		    forwarder.connect("tcp://"+address+":"+port)
    		    forwarder.setReceiveTimeOut(3000)
    	      val result = se.map(s => {
    	        val baos = new ByteArrayOutputStream()
              val o = new kOutput(baos)
              kryo.writeObjectOrNull(o,s,classOf[StreamElement])
              o.close()
              forwarder.send(baos.toByteArray)
              val rec = forwarder.recv()
              (rec != null && rec.head == 0.asInstanceOf[Byte])
            })
            forwarder.close()
            if (result.forall(identity)){
              Ok("{\"status\": \"success\"}")       
            } else {
              InternalServerError("{\"status\": \"error\", \"message\" : \"Packet forwarding to GSN core failed.\"}")
            }
          }
        case _ => InternalServerError("{\"status\": \"error\", \"message\" : \"Virtual Sensor config not found.\"}")
        }
      }
    }.recover{
      case t=>Future(BadRequest("{\"status\": \"error\", \"message\" : \""+t.getMessage+"\"}"))
    }.get
  })

 
def sensorData(sensorid:String) = headings((APIPermissionAction(playAuth,false, sensorid) compose Action).async {implicit request =>
  Try{
    val vsname = sensorid.toLowerCase
    val size: Option[Int] = queryparam("size").map(_.toInt)
    val fieldStr: Option[String] = queryparam("fields")
    val filterStr: Option[String] = queryparam("filter")
    val fromStr: Option[String] = queryparam("from")
    val toStr: Option[String] = queryparam("to")
    val fromTimestamp: Option[String] = queryparam("from_timestamp")
    val toTimestamp: Option[String] = queryparam("to_timestamp")
    val period: Option[String] = queryparam("period")
    val timeFormat: Option[String] = queryparam("timeFormat")
    val orderBy: Option[String] = queryparam("orderBy")
    val order: Option[String] = queryparam("order")
    val timeline: Option[String] = queryparam("timeline")
    val aggFunction = queryparam("agg")
    val aggPeriod = queryparam("aggPeriod")
    val isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    val format = param("format", OutputFormat, defaultFormat)
    val filters = new ArrayBuffer[String]
    val fields: Array[String] =
      if (!fieldStr.isDefined) Array()
      else fieldStr.get.split(",")
    val filterArray: Array[String] =
      if (!filterStr.isDefined) Array()
      else filterStr.get.split(",")


    if(fromTimestamp.isDefined){
      val timestamp = fromTimestamp.get
      filters += s"timed > $timestamp"
    } else if (fromStr.isDefined) {
      val fromMillis = if (fromStr.get.contains("+")) {
        isoFormatter.parseDateTime(fromStr.get).getMillis
      } else {
        dateFormatter.parseDateTime(fromStr.get).getMillis
      }
      if (timeline.isDefined) {
        filters += timeline.getOrElse("timed") + " > " + fromMillis
      } else {
        filters += s"timed > $fromMillis"
      }
    }

    if(toTimestamp.isDefined){
      val timestamp = toTimestamp.get
      filters += s"timed < $timestamp"
    } else if (toStr.isDefined) {
      val toMillis = if (toStr.get.contains("+")) {
        isoFormatter.parseDateTime(toStr.get).getMillis
      } else {
        dateFormatter.parseDateTime(toStr.get).getMillis
      }
      if (timeline.isDefined) {
        filters += timeline.getOrElse("timed") + " < " + toMillis
      } else {
        filters += s"timed < $toMillis"
      }
    }

    val conds = XprConditions.parseConditions(filterArray).recover {
      case e => throw new IllegalArgumentException("illegal conditions in filter: " + e.getMessage())
    }.get.map(_.toString)

    val agg = aggFunction.map(f => Aggregation(f, aggPeriod.get))
    val p = Promise[Seq[SensorData]]
    val q = actorSystem.actorOf(Props(new QueryActor(p)))
    q ! GetSensorData(vsname, fields, conds ++ filters, size, timeFormat, period, agg, orderBy, order, timeline)

    p.future.map { data =>
      format match {
        case controllers.gsn.api.Json =>
          val pp = JsonSerializer.ser(data.head, Seq(), false)
          Logger.debug("serialized json")
                
          Logger.debug("strings")
          Ok(pp)
        case Csv => Ok(CsvSerializer.ser(data.head, Seq(), false))
        case _ => BadRequest("Unsupported format")
      }
    }.recover {
      case t => BadRequest("Error: " + t.getMessage)
    }


  }.recover{
      case t=>Future(BadRequest("Error: "+t.getMessage))
  }.get
})

   
  def sensorField(sensorid:String,fieldid:String) = 
    Action.async {implicit request=>
    Try{
      val vsname=sensorid.toLowerCase
      //to enable
      //authorizeVs(sensorid)
    	
      val format=param("format",OutputFormat,defaultFormat)           
      val filters=new ArrayBuffer[String]
 
      val p=Promise[Seq[SensorData]]               
      val q=actorSystem.actorOf(Props(new QueryActor(p)))
      
      q ! GetSensorData(sensorid,Seq(fieldid),Seq(),Some(1),None)
      p.future.map{data=>        
          
            val series=data.head.ts.find(ts=>ts.output.fieldName.equalsIgnoreCase(fieldid))
            val fieldData=series.flatMap{s=>
              s.series.headOption              
            }.map {
              case l:Long=>Ok(l.toString)
              case d:Double=>Ok(d.toString)
              case bin:Array[Byte]=>Ok(bin).as("image")
            }         
          fieldData.get
      }.recover{
        case t=> BadRequest(t.getMessage)
      }
    }.recover{
      case t=>Future(BadRequest("Error: "+t.getMessage))
    }.get
  }

  def sensorMetadata(sensorid:String) = headings( Action.async {implicit request=>
    Try{
      //to enable
      //authorizeVs(sensorid)    	
      val latestVals=param("latestValues",_.toBoolean,false)
      val timeFormat:Option[String]=queryparam("timeFormat")
      val format=param("format",OutputFormat,defaultFormat)            
      val p=Promise[Seq[SensorData]]               
      val q=actorSystem.actorOf(Props(new QueryActor(p)))      
      q ! GetSensor(sensorid,latestVals,timeFormat)
      //val to=play.api.libs.concurrent.Promise.timeout(throw new Exception("bad things"), 15.second)
      
      p.future.map{data=>        
        format match {
            case Json=>Ok(JsonSerializer.ser(data.head,Seq(),false))
            case Csv=>Ok(CsvSerializer.ser(data.head,Seq(),false))
            case Xml=>Ok(XmlSerializer.ser(data.head, Seq(), latestVals))
            case _ => Ok(JsonSerializer.ser(data.head,Seq(),false))
        }          
      }.recover{
        case t=> BadRequest(t.getMessage)        
      }
    }.recover{
      case t=> Future(BadRequest(t.getMessage))
    }.get
  })

  def sensorSearch = Action.async {implicit request=>
    Try{
      //to enable
      //authorizeVs(sensorid)
      val sensorsStr:Option[String]=queryparam("vsnames")    	
      val size:Option[Int]=queryparam("size").map(_.toInt)
      val fieldStr:Option[String]=queryparam("fields")
      val filterStr:Option[String]=queryparam("filter")
      val fromStr:Option[String]=queryparam("from")
      val toStr:Option[String]=queryparam("to")
      val timeFormat:Option[String]=queryparam("timeFormat")

      val format=param("format",OutputFormat,defaultFormat)           
      val filters=new ArrayBuffer[String]
      val fields:Array[String]=
        if (!fieldStr.isDefined) Array() 
        else fieldStr.get.split(",")
      val vsnames:Array[String]=
        if (!sensorsStr.isDefined) Array() 
        else sensorsStr.get.split(",")
      if (fromStr.isDefined)          
        filters+= "timed>"+dateFormatter.parseDateTime(fromStr.get).getMillis
      if (toStr.isDefined)          
        filters+= "timed<"+dateFormatter.parseDateTime(toStr.get).getMillis
      val conds=XprConditions.parseConditions(filterStr.toArray).recover{                 
        case e=>throw new IllegalArgumentException("illegal conditions in filter: "+e.getMessage())
      }.get.map(_.toString)
 
      
      val dataset=vsnames.map{sensorid=>
        val p=Promise[Seq[SensorData]]               
        val q=actorSystem.actorOf(Props(new QueryActor(p)))
      
        q ! GetSensorData(sensorid,fields,conds++filters,size,timeFormat)
        p.future.map{data=>           
          data.head
        }      
      
        
      }.toSeq
      val pp=Future.sequence(dataset)
      pp.map{dats=>
        format match {
            case Json=>Ok(JsonSerializer.ser(dats,Seq(),false))
            case Csv=>Ok(CsvSerializer.serZip(dats,Seq(),false)).as("application/zip")
            case Xml=>Ok(XmlSerializer.ser(dats, Seq(), false))
            case _ => Ok(JsonSerializer.ser(dats,Seq(),false))
        }          
      }.recover{
        case t=> BadRequest(t.getMessage)              
      }      
    }.recover{
      case t=>Future(BadRequest("Error: "+t.getMessage))
    }.get
  }

  def download= (APIPermissionAction(playAuth,false) compose Action).async {implicit request=>
    //request.body.
    Future(Ok(""))
    
  }

def uploadCSV: Action[MultipartFormData[TemporaryFile]] = Action.async(parse.multipartFormData) { implicit request =>
    request.body.file("csvFile").map { filePart =>
      // Handle the file here
      val filename = filePart.filename
      val contentType = filePart.contentType
      val file = filePart.ref
      
      // Do whatever you need with the file

      Logger.debug("Uploaded file: " + filename + ", contentType: " + contentType)

      Future.successful(Ok("File uploaded"))
    }.getOrElse {
      // Handle missing file error
      Future.successful(BadRequest("Missing file"))
    }
  }


  
  def result(s:Object,out:OutputFormat)={
    val headers=new ArrayBuffer[(String,String)]
    val contentType = out match {
      case Xml=>ContentTypes.XML
      case Json=>ContentTypes.JSON
      case Shapefile=>
        headers+= "Content-Disposition"->"attachment; filename=sensors.zip"
        ContentTypes.BINARY 
      case _ =>ContentTypes.TEXT
    }
    s match {
      case s:String=>Ok(s).as(contentType)
      case j:JsValue=>
        Ok(j).as(contentType)
      case b:Array[Byte]=>Ok(b).withHeaders(headers:_*)
    }
  }

  
}



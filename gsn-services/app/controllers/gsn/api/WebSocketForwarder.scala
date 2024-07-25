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
* File: app/controllers/gsn/api/WebSocketForwarder.scala
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package controllers.gsn.api

import play.api.libs.iteratee._
import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import scala.util.Try
import scala.concurrent.Future
import play.mvc.Http
import security.gsn.GSNDeadboltHandler
import play.core.j.JavaHelpers
import scalaoauth2.provider.AuthInfoRequest
import models.gsn.auth.User
import models.gsn.auth.DataSource
import controllers.gsn.GSNDataHandler
import collection.JavaConverters._
import scalaoauth2.provider.{ProtectedResource, ProtectedResourceRequest}
import org.zeromq.ZMQ
import java.util.Date
import ch.epfl.gsn.beans.StreamElement
import ch.epfl.gsn.data._
import ch.epfl.gsn.data.format._
import javax.inject.Inject
import akka.actor._
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext
import play.api.http.websocket.{Message, TextMessage}
import play.Logger


class WebSocketForwarder @Inject()(playAuth: PlayAuthenticate)(implicit actorSystem: ActorSystem, ec: ExecutionContext) extends InjectedController {

  def socket(sensorid: String)= WebSocket.acceptOrResult[Message, Message] { requestHeader =>
    if (playAuth.isLoggedIn(new Http.Session(requestHeader.session.data.asJava))) {
      val user = User.findByAuthUserIdentity(playAuth.getUser(JavaHelpers.createJavaContext(requestHeader, JavaHelpers.createContextComponents())))
      if (hasAccess(user, false, sensorid)) {
        val flow: Flow[Message, Message, _] = createWebSocketFlow(sensorid)
        Future.successful(Right(flow))
      } else {
        Future.successful(Left(Forbidden("Logged in user has no access to these resources")))
      }
    } else {
      ProtectedResource.handleRequest(new ProtectedResourceRequest(requestHeader.headers.toMap, requestHeader.queryString), new GSNDataHandler()).flatMap {
        case Left(_) => Future.successful(Left(Forbidden("Logged in user has no access to these resources")))
        case Right(authInfo) => {
          val user = User.findById(authInfo.user.id)
          if (hasAccess(user, false, sensorid)) {
            val flow: Flow[Message, Message, _] = createWebSocketFlow(sensorid)
            Future.successful(Right(flow))
          } else {
            Future.successful(Left(Forbidden("Logged in user has no access to these resources")))
          }
        }
      }
    }
  }

    private def createWebSocketFlow(sensorid: String): Flow[Message, Message, _] = {
    val deserializer = new StreamElementDeserializer()
    val context = ZMQ.context(1)
    val subscriber = context.socket(ZMQ.SUB)
    subscriber.connect("tcp://localhost:" + 22022)
    subscriber.setReceiveTimeOut(3000)
    subscriber.subscribe((sensorid + ":").getBytes)

    Flow[Message].collect {
        case TextMessage(text) =>
        val responseText = try {
            var rec = subscriber.recv()
            while (rec == null) {
            subscriber.subscribe((sensorid + ":").getBytes)
            rec = subscriber.recv()
            }
            val o = deserializer.deserialize(sensorid, rec)
            val ts = new java.util.Date(o.getTimeStamp())
            "{ \"timestamp\":\"" + ts + "\"," + o.getFieldNames.map(x => "\"" + x.toLowerCase() + "\":\"" + o.getData(x) + "\"").mkString(",") + "}"
        } catch {
            case t: Exception => "{\"error\": \"" + t.getMessage + "\"}"
        }
        TextMessage(responseText)
    }
    }


  def hasAccess(user: User, toWrite: Boolean, vsname: String): Boolean = {
    val ds = DataSource.findByValue(vsname)
    ds == null || (ds.getIs_public && !toWrite) || user.hasAccessTo(ds, toWrite)
  }
}
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
* File: app/security/gsn/GSNScalaDeadboltHandler.scala
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package security.gsn

import be.objectify.deadbolt.scala.{DynamicResourceHandler,AuthenticatedRequest, DeadboltHandler}
import play.api.mvc.{Request, Result, Results, Session}
import play.api.mvc.Controller
import be.objectify.deadbolt.scala.models.Subject
import models.gsn.auth.User
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import play.mvc.Http
//import collection.JavaConversions._
import com.feth.play.module.pa.PlayAuthenticate
import com.feth.play.module.pa.user.AuthUserIdentity
import play.core.j.JavaHelpers

import scala.collection.JavaConverters._

class GSNScalaDeadboltHandler(auth: PlayAuthenticate,dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {

  def beforeAuthCheck[A](request: Request[A]) = {
   if (auth.isLoggedIn(new Http.Session(request.session.data.asJava))) {
			// user is logged in
			Future(None)
		} else {
			// user is not logged in

			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
		  val context = JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents())
			val originalUrl = auth.storeOriginalUrl(context)
			context.flash().put("error", "You need to log in first, to view '" + originalUrl + "'")
      Future(Option(play.mvc.Results.redirect(auth.getResolver().login()).asScala().withSession(Session(context.session().asScala.toMap))))

		} 
  }


case class UserSubject(user: models.gsn.auth.User) extends Subject {
  override val identifier: String = user.id.toString
  override val roles: List[be.objectify.deadbolt.scala.models.Role] = user.roles.asScala.map { role =>
    new be.objectify.deadbolt.scala.models.Role {
      override val name: String = role.roleName
    }
  }.toList
  override val permissions: List[be.objectify.deadbolt.scala.models.Permission] = 
    user.permissions.asScala.map { permission =>
      new be.objectify.deadbolt.scala.models.Permission {
        override val value: String = permission.value
      }
    }.toList
}
  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future.successful(None)

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = {
    val context = JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents())
    val user = User.findByAuthUserIdentity(auth.getUser(context))
    
    Future.successful(Option(UserSubject(user)))
  }

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = Future(Results.Unauthorized)



}



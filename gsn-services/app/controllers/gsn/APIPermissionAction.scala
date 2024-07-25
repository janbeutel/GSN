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
* File: app/controllers/gsn/APIPermissionAction.scala
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package controllers.gsn

import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import security.gsn.GSNDeadboltHandler
import scalaoauth2.provider._
import play.core.j.JavaHelpers
import models.gsn.auth.User
import models.gsn.auth.DataSource
import play.mvc.Http
import collection.JavaConverters._
import play.Logger
import javax.inject.Inject
import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc.Results.Forbidden

class APIPermissionAction @Inject()(playAuth: PlayAuthenticate, toWrite: Boolean, vsnames: String*)(implicit ctx: ExecutionContext) extends ActionFunction[Request, ({type L[A] = Request[A]})#L] with OAuth2Provider {

  override protected def executionContext: ExecutionContext = ctx
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    val session = request.session
    val sessiondata= request.session.data
    if (playAuth.isLoggedIn(new Http.Session(request.session.data.asJava))) {
      val u = User.findByAuthUserIdentity(playAuth.getUser(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents())))
      if (hasAccess(u,toWrite,vsnames:_*)) block(request)
      else Future(Results.Forbidden("Logged in user has no access to these resources"))
    }else{
      //implicit request => authorize(new MyDataHandler()) { authInfo => val user = authInfo.user
      //Action.async { implicit request => authorize(new GSNDataHandler()) { authInfo =>
      authorize(new GSNDataHandler())({authInfo => {
        val u = User.findById(authInfo.user.id)
        if (hasAccess(u,toWrite,vsnames:_*)) block(AuthInfoRequest(AuthInfo[User](u, authInfo.clientId, authInfo.scope, authInfo.redirectUri), request))
        else Future(Results.Forbidden("Logged in user has no access to these resources"))
      }})(request, ctx)
      
    }
  }


  def hasAccess(user: User,toWrite: Boolean,vsnames: String*):Boolean =  
    vsnames.foldRight[Boolean](true)((vs,b) => b && hasAccess(user,toWrite,vs))

       
   def hasAccess(user: User,toWrite: Boolean,vsname: String):Boolean = {
     val ds = DataSource.findByValue(vsname)
     ds == null || (ds.getIs_public && !toWrite) || user.hasAccessTo(ds, toWrite)
   }
}

object APIPermissionAction {
  def apply(playAuth: PlayAuthenticate,toWrite: Boolean, vsnames: String*)(implicit ctx: ExecutionContext): APIPermissionAction = {
    new APIPermissionAction(playAuth,toWrite, vsnames: _*)
  }
}

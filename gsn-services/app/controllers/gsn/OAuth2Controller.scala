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
* File: app/controllers/gsn/OAuth2Controller.scala
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package controllers.gsn

import scalaoauth2.provider._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import play.utils.UriEncoding
import models.gsn.auth.{User, Client, OAuthCode}
import com.feth.play.module.pa.PlayAuthenticate
import be.objectify.deadbolt.scala.DeadboltActions
import security.gsn.GSNScalaDeadboltHandler
import play.mvc.Http
import play.core.j.JavaHelpers
import scala.collection.JavaConverters._
import scala.concurrent.Future
import views.html._
import controllers.gsn.auth.Application
import play.mvc.Http.Context
import play.api.data._
import play.api.data.Forms._
import controllers.gsn.auth.Forms._
import play.api.data.Form
import java.util.UUID
import com.feth.play.module.pa.PlayAuthenticate
import javax.inject.Inject
import service.gsn.UserProvider
import be.objectify.deadbolt.scala.{DeadboltActions, anyOf, allOf, allOfGroup}

class OAuth2Controller @Inject()(playAuth: PlayAuthenticate, deadbolt: DeadboltActions,userProvider: UserProvider) extends InjectedController with OAuth2Provider {
  
    override val tokenEndpoint = new OAuth2Endpoint

    def accessToken = Action.async { implicit request =>
        issueAccessToken[AnyContent,User](new GSNDataHandler())
    }
  
    def auth = Action.async { implicit request => Future {
      Context.current.set(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents()))
      if (playAuth.isLoggedIn(new Http.Session(request.session.data.asJava))) {
       val u = User.findByAuthUserIdentity(playAuth.getUser(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents())))
       val r = request.queryString.get("response_type")
       if (r.map(x => x.contains("code")).getOrElse(false)){
           try{
               val c = request.queryString.get("client_id").get.head
               val s = request.queryString.get("client_secret").get.head
               val client = Client.findById(c)
               if (client != null && client.secret == s){
                 if (u.trusted_clients.contains(client)){
                   val c = OAuthCode.generate(u,client)
                   c.save()
                   Redirect(client.redirect+"?code="+c.code+"&response_type=code&user_name="+UriEncoding.encodePathSegment(u.name,"UTF-8")+"&user_email="+UriEncoding.encodePathSegment(u.email,"UTF-8"))
                 }else{
                   Ok(access.auth.render(c,s, client.redirect, u, client.name,userProvider))
                 }
               }else{
                   Forbidden("This client is not registered for accessing GSN, please contact the administrator if you need to add it.")
               }
           } catch {
             case e: Throwable => BadRequest(e.getMessage)
           }
       }else{
         NotImplemented("Response type not implemented: "+ r.getOrElse("null"))
       }
      }else{
        Redirect(controllers.gsn.auth.routes.Application.login()).withSession(request.session.+(("pa.url.orig", request.uri)))
      }
    }}
 
   def doAuth = Action.async { implicit request => Future {
     Context.current.set(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents()))
      if (playAuth.isLoggedIn(new Http.Session(request.session.data.asJava))) {
       val u = User.findByAuthUserIdentity(playAuth.getUser(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents())))
       clientForm.bindFromRequest.fold(
           formWithErrors => {
               BadRequest("bad request")
            },
            clientData => {
               val client = Client.findById(clientData.client_id)
               if (client != null && client.secret == clientData.client_secret){
                   val c = OAuthCode.generate(u,client)
                   c.save()
                   u.trusted_clients.add(client)
                   //u.saveManyToManyAssociations("trusted_clients")
                   Redirect(client.redirect+"?code="+c.code+"&response_type=code&user_name="+UriEncoding.encodePathSegment(u.name,"UTF-8")+"&user_email="+UriEncoding.encodePathSegment(u.email,"UTF-8"))
               }else{
                   Forbidden("This client is not registered for accessing GSN, please contact the administrator if you need to add it.")
               }
            }
       )
      }else{
        Redirect(controllers.gsn.auth.routes.Application.login()).withSession(request.session.+(("pa.url.orig", request.uri)))
      }
    }}

   def listClients = deadbolt.Restrict(roleGroups = allOfGroup( Application.USER_ROLE))() { request => Future {
       Context.current.set(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents()))
       val u = User.findByAuthUserIdentity(playAuth.getUser(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents())))
       if (u.roles.asScala.toList.filter(p => p.getName.equals(Application.USER_ROLE)).isEmpty) {
           Ok(access.clientlist.render(Client.find.all().asScala, editClientForm,userProvider))
       } else {
           Ok(access.clientlist.render(Client.findByUser(u).asScala, editClientForm,userProvider))
       }
    }}
   
   def editClient = deadbolt.Restrict(roleGroups = allOfGroup( Application.USER_ROLE))() { implicit request =>  Future {
       Context.current.set(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents()))
       val u = User.findByAuthUserIdentity(playAuth.getUser(JavaHelpers.createJavaContext(request,JavaHelpers.createContextComponents())))
       var ret:Result = null
       editClientForm.bindFromRequest.fold(
           formWithErrors => {
               println(formWithErrors)
               ret = BadRequest(access.clientlist.render(Client.find.all().asScala, formWithErrors,userProvider))
            },
            clientData => { 
              clientData.action match {
              case "add" => {
                                val c = new Client()
                                c.clientId = clientData.client_id
                                c.name = clientData.name
                                c.redirect = clientData.redirect
                                c.secret = clientData.client_secret
                                c.user = u
                                println(clientData)
                                c.linked = clientData.linked
                                c.save()
                            }
              case "edit" => {
                                val c = Client.find.byId(clientData.id)
                                if (c == null){ 
                                  ret = NotFound
                                } else {
                                  c.setClientId(clientData.client_id)
                                  c.setName(clientData.name)
                                  c.setRedirect(clientData.redirect)
                                  c.setSecret(clientData.client_secret)
                                  c.setLinked(clientData.linked)
                                  c.update()
                                }
                             }
              case "del" => {
                                val c = Client.find.byId(clientData.id)
                                if (c == null) ret = NotFound
                                else c.delete()
                            }
               }
            })
            if (ret != null)  ret else Ok(access.clientlist.render(Client.find.all().asScala, editClientForm,userProvider))  
    }}
  
}
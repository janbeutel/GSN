import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export class GSNUser {
  'id' : number;
  'username' : string
  'access_token' : string
  'refresh_token' : string
  'token_created_date' : string
  'token_expire_date' : string
  'logged_in' : boolean
}

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  constructor(private http: HttpClient) { }

  // gets login url from backend
  getLoginUrl(): Observable<any> {
    return this.http.get('http://walker.uibk.ac.at:8000/oauth_code',  { withCredentials: true });
  }

  // creates profile after login in
  // returns user
  createProfile(params: string): Observable<any> {
    var url = 'http://walker.uibk.ac.at:8000/profile/' + params;
    return this.http.get(url,  { withCredentials: true });
  }

}



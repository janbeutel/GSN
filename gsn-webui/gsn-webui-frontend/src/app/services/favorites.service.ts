import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FavoritesService {

  constructor(private http: HttpClient) {}

  remove(sensor_name: string): Observable<any> {
    const params = new HttpParams().set('remove', sensor_name);
    return this.http.get('http://walker.uibk.ac.at:8000/favorites/', { params, withCredentials: true });
  }

  add(sensor_name: string): Observable<any> {
    const params = new HttpParams().set('add', sensor_name);
    return this.http.get('http://walker.uibk.ac.at:8000/favorites/', { params, withCredentials : true });
  }

  list(): Observable<any> {
    return this.http.get('http://walker.uibk.ac.at:8000/favorites_list/', { withCredentials: true });
  }

}

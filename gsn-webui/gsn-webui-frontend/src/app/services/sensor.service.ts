import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SensorService {

  constructor(private http: HttpClient) { }

  getSensors(): Observable<any> {
    return this.http.get('http://walker.uibk.ac.at:8000/sensors');
  }

  getSensorData(sensorName: string, from: string, to: string): Observable<any>{
    console.log("SERVICE", from);
    console.log("SERVICE", to);
    return this.http.get(`http://walker.uibk.ac.at:8000/sensors/${sensorName}/${from}/${to}/`, { withCredentials: true });
  }
}

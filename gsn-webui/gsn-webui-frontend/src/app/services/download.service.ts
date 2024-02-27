import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class DownloadService {
  constructor(private http: HttpClient) {}

  download(scope: any): void {
    this.http.post('http://walker.uibk.ac.at:8000/download/', scope.details, { responseType: 'blob', withCredentials : true })
      .subscribe((data: any) => {
        const blobURL = URL.createObjectURL(data);
        const anchor = document.createElement('a');
        anchor.download = `${scope.sensorName}.csv`;
        anchor.href = blobURL;
        anchor.click();
      }, (error: any) => {
        console.log('Post failed');
      });
  }

  downloadMultiple(sensorList: string[], from: string, to: string): void {
    sensorList.forEach((sensor: string) => {
      this.http.get(`http://walker.uibk.ac.at:8000/download/${sensor}/${from}/${to}/`, { responseType: 'blob', withCredentials: true })
        .subscribe((data: any) => {
          const blobURL = URL.createObjectURL(data);
          const anchor = document.createElement('a');
          anchor.download = `${sensor}.csv`;
          anchor.href = blobURL;
          anchor.click();
        }, (error: any) => {
          window.alert(`You do not have access to the sensor ${sensor}`);
        });
    });
  }
}

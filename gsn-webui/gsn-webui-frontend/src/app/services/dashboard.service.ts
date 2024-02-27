import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FavoritesService } from './favorites.service';
import { Observable, forkJoin, switchMap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(
    private http: HttpClient,
    private favoritesService : FavoritesService
  ) { }

  dashboard(): Observable<{ [key: string]: any }> {
    // Create an object to hold the observables for each dashboard request
    const dashboardRequests: { [key: string]: Observable<any> } = {};

    // Get all favorites
    return this.favoritesService.list().pipe(
      switchMap((data: any) => {
        const favoritesList = data.favorites_list;

        // For each favorite in favoritesList, create an observable for the dashboard request
        favoritesList.forEach((favorite: string) => {
          dashboardRequests[favorite] = this.http.get(`http://walker.uibk.ac.at:8000/dashboard/${favorite}/`, { withCredentials: true });
        });

        // Use forkJoin to combine all the dashboard requests into a single observable
        return forkJoin(dashboardRequests);
      })
    );
  }

}



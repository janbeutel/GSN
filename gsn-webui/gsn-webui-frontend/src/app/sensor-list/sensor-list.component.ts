/*
 * sensor-list.component.ts
 * Component for sensor list
 *
 * This file contains code based on or derived from OpenLayers (https://openlayers.org/),
 * which is licensed under the BSD 2-Clause License.
 *
 * Copyright (c) 2005-present, OpenLayers Contributors All rights reserved.
 * All rights reserved.
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

import { Component, OnInit } from '@angular/core';
import { SensorService } from "src/app/services/sensor.service";

// Add the OpenLayers imports
import Map from 'ol/Map';
import View from 'ol/View';
import TileLayer from 'ol/layer/Tile';
import XYZ from 'ol/source/XYZ';
import Feature from 'ol/Feature';
import Point from 'ol/geom/Point';
import { fromLonLat } from 'ol/proj';
import { Style, Icon } from 'ol/style';
import { Vector } from 'ol/layer';
import { Vector as VectorSource } from 'ol/source';
import { FavoritesService } from '../services/favorites.service';


@Component({
  selector: 'app-sensor-list',
  templateUrl: './sensor-list.component.html',
  styleUrls: ['./sensor-list.component.scss']
})
export class SensorListComponent implements OnInit {
  loading = true;
  sensors: any[] = [];
  favorites: any[] = [];
  lat = 0; // Set the initial latitude
  lng = 0; // Set the initial longitude
  map: any; // Declare a map variable


  constructor(
    private sensorService: SensorService,
    private favoritesService: FavoritesService) {

  }

  ngOnInit() {
    this.loadSensors();
  }

 firstSensorIndex: number =-1;
  loadSensors() {
    this.sensorService.getSensors().subscribe(
      (data: any) => {
        this.sensors = data.features;
        this.loading = false;
        console.log(this.sensors);
        // Set the center of the map to the first sensor's coordinates
        if (this.sensors.length > 0) {
          for (let index = 0; index < this.sensors.length; index++) {

            if(this.sensors[index].geometry !=null){
              const firstSensor = this.sensors[index];
              this.firstSensorIndex = index;
              this.lat = firstSensor.geometry.coordinates[0];
              this.lng = firstSensor.geometry.coordinates[1];
              break; // Exit the loop after the assignment
            }
          }
          this.initMap(); // Initialize the map
        }
        this.favoritesService.list().subscribe(
          (data) => {
            this.favorites = data.favorites_list;
          },
          (error) => {
            console.error(error);
          }
        );
      },
      (error: any) => {
        console.error(error);
      }
    );
  }


  initMap() {
    // Create a new map instance
    this.map = new Map({
      target: 'map', // Set the target HTML element where the map will be rendered
      layers: [
        new TileLayer({
          source: new XYZ({
            url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png' // Use OpenStreetMap as the base layer
          })
        })
      ],
      view: new View({
        center: [this.lng, this.lat], // Set the initial center of the map
        zoom: 10 // Set the initial zoom level
      })
    });
    // Create a vector source and layer for markers
    const markerSource = new VectorSource();
    const markerLayer = new Vector({
      source: markerSource
    });

    // Add the marker layer to the map
    this.map.addLayer(markerLayer);

    // Set the center of the map to the first sensor's coordinates
    if(this.firstSensorIndex != -1){
      const firstSensor = this.sensors[this.firstSensorIndex];
      const firstSensorCoordinates = firstSensor.geometry.coordinates;
      const centerCoordinates = fromLonLat([firstSensorCoordinates[0], firstSensorCoordinates[1]]);
      this.map.getView().setCenter(centerCoordinates);
    } else {
      const centerCoordinates = fromLonLat([7.6586, 45.9765]);
      this.map.getView().setCenter(centerCoordinates);

    }


    // Loop through the sensors and add markers to the map
    this.sensors.forEach(sensor => {
      if(sensor.geometry != null){
        const coordinates = sensor.geometry.coordinates;
        const marker = new Feature({
          geometry: new Point(fromLonLat(coordinates))
        });

        const markerStyle = new Style({
          image: new Icon({
            src: '../../assets/285659_marker_map_icon.svg', // Provide the path to your marker icon
            anchor: [0.5, 1] // Set the anchor point of the icon (adjust if needed)
          })
        });

        marker.setStyle(markerStyle);
        markerSource.addFeature(marker);
      }
    });
  }


}

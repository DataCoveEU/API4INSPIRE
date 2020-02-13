import { Component, OnInit } from '@angular/core';
import { HomeService } from '../home.service';
declare var ol: any;
import * as $ from 'jquery';

import 'ol/ol.css';
import Feature from 'ol/Feature';
import Map from 'ol/Map';
import View from 'ol/View';
import FullScreen from 'ol/control';
import Circle from 'ol/geom/Circle';
import {Tile as TileLayer, Vector as VectorLayer} from 'ol/layer';
import {OSM, Vector as VectorSource} from 'ol/source';
import GeoJSON from 'ol/format/GeoJSON';
import { HttpClient } from '@angular/common/http';


@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit {

  map: any;
  importantLinks: any = []; 

  collections: any = ["Lukas", "Tobias"];

  constructor(private homeService: HomeService, private http: HttpClient, private httpClient: HttpClient) { }

  async ngOnInit() {
    var col:any = (await this.getCollections());
    this.collections = col.collections;

    this.importantLinks = await this.homeService.getLinks();

    this.http.get("collections/tna_insp_airspacearea/items").subscribe(json =>{

      var vectorSource = new VectorSource({
        features: (new GeoJSON({ featureProjection: 'EPSG:4326' })).readFeatures(json)
      });
      
      var vectorLayer = new VectorLayer({
        source: vectorSource
      });
      
      var map = new Map({
        layers: [
          new TileLayer({
            source: new OSM()
          }),
          vectorLayer
        ],
        target: 'map',
        view: new View({
          projection: 'EPSG:4326',
          center: [16, 48],
          zoom: 6
        })
      });

});
    
  }

  async getCollections() {
    return new Promise((resolve, reject) =>{
      this.httpClient.get('collections').subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    });
  }
}

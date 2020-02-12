import { Component, OnInit } from '@angular/core';
import { HomeService } from '../home.service';
declare var ol: any;
import * as $ from 'jquery';

import 'ol/ol.css';
import Feature from 'ol/Feature';
import Map from 'ol/Map';
import View from 'ol/View';
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
    //[{link: '#', name: 'No links available yet'}];

  constructor(private homeService: HomeService, private http: HttpClient) { }

  async ngOnInit() {
    this.importantLinks = await this.homeService.getLinks();

    this.http.get("collections/insp_airspacearea/items").subscribe(res =>{

      console.log(res)


      var vectorLayer = new VectorLayer({
        source: new VectorSource({
            format: new GeoJSON(),
            url: 'collections/insp_airspacearea/items'
        })
    });

    this.map = new Map({
      target: 'map',
      layers: [
        new TileLayer({
          source: OSM()
        }),
        vectorLayer
      ],
      view: new View({
        center: ol.proj.fromLonLat([13.509380, 47.327426]),
        zoom: 7
      })
    });

    });

    
  }
}

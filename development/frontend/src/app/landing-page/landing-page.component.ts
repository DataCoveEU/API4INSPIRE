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

  constructor(private homeService: HomeService, private http: HttpClient) { }

  async ngOnInit() {
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

    var
      container = document.getElementById('popup'),
      content_element = document.getElementById('popup-content'),
      closer = document.getElementById('popup-closer');
  
  closer.onclick = function() {
      overlay.setPosition(undefined);
      closer.blur();
      return false;
  };
  var overlay = new ol.Overlay({
      element: container,
      autoPan: true,
      offset: [0, -10]
  });
  map.addOverlay(overlay);
  
  var fullscreen = new FullScreen();
  map.addControl(fullscreen);
  
  map.on('click', function(evt){
      var feature = map.forEachFeatureAtPixel(evt.pixel,
        function(feature, layer) {
          return feature;
        });
      if (feature) {
          var geometry = feature.getGeometry();
          var coord = geometry.getCoordinates();
          
          var content = '<h3>' + feature.get('name') + '</h3>';
          content += '<h5>' + feature.get('description') + '</h5>';
          
          content_element.innerHTML = content;
          overlay.setPosition(coord);
          
          console.info(feature.getProperties());
      }
  });
  map.on('pointermove', function(e) {
      if (e.dragging) return;
         
      var pixel = map.getEventPixel(e.originalEvent);
      var hit = map.hasFeatureAtPixel(pixel);
      
      map.getTarget().style.cursor = hit ? 'pointer' : '';
  });
});
    
  }
}

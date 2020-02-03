import { Component, OnInit } from '@angular/core';
declare var ol: any;

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit {

  map: any;

  constructor() { }

  ngOnInit() {


    this.map = new ol.Map({
      target: 'map',
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM()
        })
      ],
      view: new ol.View({
        center: ol.proj.fromLonLat([16.363449, 48.210033]),
        zoom: 10
      })
    });
  }
}

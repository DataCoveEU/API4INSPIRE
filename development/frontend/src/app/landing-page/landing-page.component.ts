import { Component, OnInit } from '@angular/core';
import { HomeService } from '../home.service';
declare var ol: any;

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit {

  map: any;
  importantLinks: any = []; 
    //[{link: '#', name: 'No links available yet'}];

  constructor(private homeService: HomeService) { }

  async ngOnInit() {
    this.importantLinks = await this.homeService.getLinks();

    this.map = new ol.Map({
      target: 'map',
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM()
        })
      ],
      view: new ol.View({
        center: ol.proj.fromLonLat([13.509380, 47.327426]),
        zoom: 7
      })
    });
  }
}

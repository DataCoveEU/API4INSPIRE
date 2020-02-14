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
import {Image as ImageLayer} from 'ol/layer';
import {OSM, Vector as VectorSource} from 'ol/source';
import GeoJSON from 'ol/format/GeoJSON';
import ImageWMS from 'ol/source/ImageWMS';
import { HttpClient } from '@angular/common/http';
import LayerSwitcher from 'ol-layerswitcher';
import SourceOSM from 'ol/source/OSM';
import LayerTile from 'ol/layer/Tile';


@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit {

  map: Map;
  importantLinks: any = []; 


  collections: any = ["Lukas", "Tobias"];

  constructor(private homeService: HomeService, private http: HttpClient, private httpClient: HttpClient) { }

  async ngOnInit() {
    var col:any = (await this.getCollections());
    this.collections = col.collections;

    this.importantLinks = await this.homeService.getLinks(); 
    
  

    var austrocontorl = new ImageLayer({
      title: "Bundeslaender",
      type: 'base',
      source: new ImageWMS({
        url: 'https://sdigeo-free.austrocontrol.at/geoserver/free/wms',
        params: {'LAYERS': 'free:BEV_VGD_BL', 'VERSION': '1.3.0'},
        serverType: 'geoserver'
      })
    })

    var osm = new LayerTile({
      title: 'OSM',
      type: 'base',
      visible: true,
      source: new SourceOSM()
    })

    var layerSwitcher = new LayerSwitcher({
      tipLabel: 'Legende', // Optional label for button
      groupSelectStyle: 'children' // Can be 'children' [default], 'group' or 'none'
  });

    this.map = new Map({
      layers: [
        austrocontorl,
        osm
      ],
      target: 'map',
      view: new View({
        projection: 'EPSG:3857',
        center: [1781111.85, 6106854.83],
        zoom: 6
      })
  });

  this.map.addControl(layerSwitcher);
    
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

  onClick(event: any){
    var checked = event.target.checked;
    var link = event.target.parentNode.parentNode.children[1].textContent;

    if(checked){
      this.http.get(`collections/${link}/items`).subscribe(async (json:any) =>{
        for(var i = 10;i<15000;i+=10){
          var js = await $.get(`collections/${link}/items?offset=${i}`);
          json.features.push(...(js.features))
          if(js.links.filter(link => link.rel == 'next').length == 0){
            var vectorSource = new VectorSource({
              features: (new GeoJSON({ featureProjection: 'EPSG:3857' })).readFeatures(json)
            });
            
            var vectorLayer = new VectorLayer({
              source: vectorSource,
              name: link
            });
    
            this.map.getLayers().getArray().push(vectorLayer);
            this.map.render();
            break;
          }
        }
      });
    }else{
      var layers = this.map.getLayerGroup().getLayers().getArray()
      .filter(layer => layer.getProperties().name != link)
      var layerObject = this.map.getLayerGroup().getLayers()
      layerObject.array_ = layers;
      this.map.getLayerGroup().setLayers(layerObject);
      this.map.render();
    }
 }
}

import { Component, OnInit } from '@angular/core';
import { HomeService } from '../home.service';
declare var ol: any;
import * as $ from 'jquery';

import 'ol/ol.css';
import Map from 'ol/Map';
import View from 'ol/View';
import {Tile as TileLayer, Vector as VectorLayer} from 'ol/layer';
import {Image as ImageLayer} from 'ol/layer';
import {OSM, Vector as VectorSource} from 'ol/source';
import GeoJSON from 'ol/format/GeoJSON';
import ImageWMS from 'ol/source/ImageWMS';
import { HttpClient } from '@angular/common/http';
import LayerSwitcher from 'ol-layerswitcher';
import SourceOSM from 'ol/source/OSM';
import LayerTile from 'ol/layer/Tile';
import LayerGroup from 'ol/layer/Group';
import {defaults as defaultControls, ZoomToExtent} from 'ol/control';


@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit {

  map: Map;
  importantLinks: any = []; 
  zoomToExtent: ZoomToExtent;

  showLoading: boolean = false;

  collections: any = ["Lukas", "Tobias"];

  constructor(private homeService: HomeService, private http: HttpClient, private httpClient: HttpClient) { }

  async ngOnInit() {
    //Load all the collections
    var col:any = (await this.getCollections());
    this.collections = col.collections;

    //Load all the important links to show them in the footer
    this.importantLinks = await this.homeService.getLinks(); 
    
  
    //init the layer where only the borders of the federal states are shown
    var austrocontorl = new ImageLayer({
      title: "Federal States",
      type: 'base',
      source: new ImageWMS({
        url: 'https://sdigeo-free.austrocontrol.at/geoserver/free/wms',
        params: {'LAYERS': 'free:BEV_VGD_BL', 'VERSION': '1.3.0'},
        serverType: 'geoserver'
      })
    })

    //init the open street map layer
    var osm = new LayerTile({
      title: 'OSM',
      type: 'base',
      visible: true,
      source: new SourceOSM()
    })

    // init the button to switch layer
    var layerSwitcher = new LayerSwitcher({
      tipLabel: 'Legende', // Optional label for button
      groupSelectStyle: 'children' // Can be 'children' [default], 'group' or 'none'
  });

    //init the map with the tow layers and set the deafult view point
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

  
  //Add the layer-switch button
  this.map.addControl(layerSwitcher);
  //init and add the "zoom to extend button"
  this.zoomToExtent = new ZoomToExtent();
  this.map.addControl(this.zoomToExtent);
    
  }

  /**
   * Load the collections using the OGC Simple API
   */
  async getCollections() {
    return new Promise((resolve, reject) =>{
      this.httpClient.get('collections').subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    });
  }

  /**
   * Select a collection to show in the map
   * 
   * @param event 
   */
  onClick(event: any){
    this.showLoading = true;
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

            vectorLayer.setExtent(vectorSource.getExtent())
    
            this.map.getLayers().getArray().push(vectorLayer);
            this.map.render();
            
            this.setExtent();
            this.showLoading = false;
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

      this.setExtent();
      this.showLoading = false;
    }
 }

 /**
  * Updates the extend of the "zoom to extend" button
  */
 setExtent(){
  var layers = this.map.getLayerGroup().getLayers().getArray()
  .filter(layer => layer.getProperties().name != undefined)
  if(layers.length > 0){
    var bbox = layers[0].getExtent();
    for(var x = 1;x<layers.length;x++){
      var bboxZw = layers[x].getExtent();
      if(bboxZw){
        bbox[0] = bbox[0] > bboxZw[0] ? bboxZw[0] : bbox[0];
        bbox[1] = bbox[1] > bboxZw[1] ? bboxZw[1] : bbox[1];
        bbox[2] = bbox[2] < bboxZw[2] ? bboxZw[2] : bbox[2];
        bbox[3] = bbox[3] < bboxZw[3] ? bboxZw[3] : bbox[3];
      }
    }
    
    this.map.removeControl(this.zoomToExtent);
    this.zoomToExtent = new ZoomToExtent({
      extent: bbox
    })
    this.map.addControl(this.zoomToExtent);
    this.map.render();
  }else{
    this.map.removeControl(this.zoomToExtent);
    this.zoomToExtent = new ZoomToExtent()
    this.map.addControl(this.zoomToExtent);
    this.map.render();
  }
 }
}

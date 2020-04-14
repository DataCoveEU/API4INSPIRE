/*
    The OGC API Simple provides enviromental data
    Created on Wed Feb 26 2020
    Copyright (c) 2020 - Lukas Gäbler

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/


import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConnectorService } from '../connector.service';

@Component({
  selector: 'app-collections',
  templateUrl: './collections.component.html',
  styleUrls: ['./collections.component.scss']
})
export class CollectionsComponent implements OnInit {

  constructor(private httpClient: HttpClient) { }

  featureCollections: any = [];

  async ngOnInit() {
    //Init the feature collectins array
    var col:any = (await this.getCollections());
    this.featureCollections = col.collections;

    
    
  }

  /**
   * Load the collections from the DB using the OGC Simple API 
   */
  async getCollections() {
    return new Promise((resolve, reject) =>{
      this.httpClient.get('collections' + this.buildString()).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    });
  }

  buildString() {
    var erg = "";
    var filter = window.location.search.split('?f=text%2Fhtml')
    if(filter[1] != "" ) {
      var fil = filter[1].split("&")
      for(let i = 1; i < fil.length; i++) {
        erg = erg + fil[i] + "&"
      }
      return "?" + erg;
    }
    return erg;
  }

}

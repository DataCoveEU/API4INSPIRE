/*
    The OGC API Simple provides environmental data
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
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { isGeneratedFile } from '@angular/compiler/src/aot/util';
import { parseTemplate, parseHostBindings } from '@angular/compiler';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.scss']
})
export class ItemsComponent implements OnInit {

  collection: string = "";
  items: any = [];

  next: string = "";
  previous: string = "";

  nextAv: boolean = true;
  prevAv: boolean = true;

  last = 0;

  jsonLink: string = "";

  constructor(private router: ActivatedRoute, private httpClient: HttpClient) { }

  async ngOnInit() {
    // set the collection variable (the collection that is selected)
    this.router.params.subscribe(async(query)=>{
      this.collection = query.collection;
    });
    // load all the items
    this.items = await this.getItems("collections/" + this.collection + "/items", false, false);

    if(!this.containsLink("next", this.items)) {
      this.nextAv = false;
    } else {
      this.nextAv = true;
    }
    if(!this.containsLink("prev", this.items)) {
      this.prevAv = false;
    } else {
      this.prevAv = true;
    }

    this.next = this.getLink("next", this.items);
    this.previous = this.getLink("prev", this.items);
    this.jsonLink = this.getLink("self", this.items);
    
    this.parseSelf();  
  }

  /**
   * Load the items using the OGC Simple API
   */
  getItems(path: string, isNext, isBefore) {
    return new Promise((resolve, reject)=>{
      this.httpClient.get(this.buildString(isNext, isBefore, path))
      .subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }


  async nextPage() {
      this.items = await this.getItems(this.next, true, false);
    if(!this.containsLink("next", this.items)) {
      this.nextAv = false;
    } else {
      this.nextAv = true;
    }
    if(!this.containsLink("prev", this.items)) {
      this.prevAv = false;
    } else {
      this.prevAv = true;
    }
      

    this.next = this.getLink("next", this.items);
    this.previous = this.getLink("prev", this.items);
    this.jsonLink = this.getLink("self", this.items);
    this.parseSelf();

  }

  async previousPage() {
    this.items = await this.getItems(this.previous, false, true);
      
    if(!this.containsLink("next", this.items)) {
      this.nextAv = false;
    } else {
      this.nextAv = true;
    }
    if(!this.containsLink("prev",this.items)) {
      this.prevAv = false;
    } else {
      this.prevAv = true;
    }  
    
    this.next = this.getLink("next", this.items);
    this.previous = this.getLink("prev", this.items);
    this.jsonLink = this.getLink("self", this.items);
    this.parseSelf();
  }

  containsLink(kind: string, items):boolean {
    for(let i = 0; i < items.links.length; i++) {
      if(items.links[i].rel == kind) {
        return true;
      }
    }
    return false;
  }

  getLink(kind: string, items):string {
    for(let i = 0; i < items.links.length; i++) {
      if(items.links[i].rel == kind) {
        return items.links[i].href;
      }
    }
    return "";
  }
  

  parseSelf() {
    var show = document.getElementById("disp");
    var str = this.jsonLink.split("=");
    if(str[2].includes("&")) {
      var gs = str[2].split("&");
      str[2] = gs[0];
    }
    var params = new URLSearchParams(window.location.search);
    if(params.                                                                                                                                                                                          get("limit") == null) {
      show.innerHTML = "Displaying items: " + str[2] + " - " + (parseInt(str[2]) + 10)
    } else {
      show.innerHTML = "Displaying items: " + str[2] + " - " + (parseInt(str[2]) + parseInt(params.get("limit")))
    }
    
  }

  /**
   * 
   * @param isNext is the string needed for the next link
   * @param isBefore is the string needed for the previous lin 
   */
  buildString(isNext, isBefore, path) { 
    var p = path;
    if(path.includes("?")) {
      p = path.split("?")[0];
    }
    var filts = "";
    var params = new URLSearchParams(window.location.search);
    params.delete("f");
    var url = new URL(window.location.toString());
    filts = url.search = params.toString();
    if(filts.length != 0) {
      return p + "?" + filts // + "&f=application/json";
    }  
    return p + filts;
  }

}

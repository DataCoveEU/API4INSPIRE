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
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

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
    // set the collection varibale (the collection that is selected)
    this.router.params.subscribe(async(query)=>{
      this.collection = query.collection;
    });
    // load all the items
    this.items = await this.getItems("collections/" + this.collection + "/items");

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
  getItems(path: string) {
    return new Promise((resolve, reject)=>{
      this.httpClient.get(path)
      .subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }


  async nextPage() {
      this.items = await this.getItems(this.next);
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
    this.items = await this.getItems(this.previous);
      
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
    show.innerHTML = "Displaying items: " + str[2] + " - " + (parseInt(str[2])+10)
  }

}

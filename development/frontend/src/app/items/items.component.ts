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

  constructor(private router: ActivatedRoute, private httpClient: HttpClient) { }

  async ngOnInit() {
    // set the collection varibale (the collection that is selected)
    this.router.params.subscribe(async(query)=>{
      this.collection = query.collection;
    });
    // load all the items
    this.items = await this.getItems();
  }

  /**
   * Load the items using the OGC Simple API
   */
  getItems() {
    return new Promise((resolve, reject)=>{
      this.httpClient.get("collections/" + this.collection + "/items")
      .subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

}

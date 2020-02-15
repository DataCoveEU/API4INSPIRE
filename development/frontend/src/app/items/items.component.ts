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
    this.router.params.subscribe(async(query)=>{
      this.collection = query.collection;
    });
    this.items = await this.getItems();
    console.log(this.items);
  }

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

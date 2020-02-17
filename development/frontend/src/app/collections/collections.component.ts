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
    var col:any = (await this.getCollections());
    this.featureCollections = col.collections;
    console.log(this.featureCollections);
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

}

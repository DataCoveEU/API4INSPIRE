import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { renderFlagCheckIfStmt } from '@angular/compiler/src/render3/view/template';

@Injectable({
  providedIn: 'root'
})
export class ConnectorService {

  connectors: any;
  tables: any;
  columns: any;

  constructor(private httpClient: HttpClient) { }

  async getConnector() {
    this.connectors = await new Promise((resolve, reject) =>{
      this.httpClient.post('/api/getConnectors', {
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    });
    return this.connectors;
  }

  async getTables(json:object) {
    this.tables = await new Promise((resolve, reject) =>{
      this.httpClient.post('/api/getTables', json).subscribe((res)=>{
        resolve(res);;
      }, (err)=>{
        reject(err);
      })
    });
    return this.tables;
  }

  async getColumn(json:object) {
    this.columns = await new Promise((resolve, reject) =>{
      this.httpClient.post('/api/getColumns', json)
      .subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
    return this.columns;
  }

  addConnector(json:object) {
    this.httpClient.post('/api/addConnector', 
      json,{
      responseType: 'json'
    }).subscribe((res)=>{
      console.log("Connector added");
    }, (err)=>{
      console.log(err);
    });
  }
}

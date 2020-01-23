import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ConnectorService {

  connectors: any;

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
    this.connectors = await new Promise((resolve, reject) =>{
      this.httpClient.post('/api/getTables', json).subscribe((res)=>{
        resolve(res);;
      }, (err)=>{
        reject(err);
      })
    });
    return this.connectors;
  }
}

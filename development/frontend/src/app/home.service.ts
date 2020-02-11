import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class HomeService {

  constructor(private httpClient: HttpClient) { }

  async getLinks() {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/getImportantLinks', {
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async removeLink(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/removeImportantLink', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async addLink(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/addImportantLink', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }
}

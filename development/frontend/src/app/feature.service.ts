import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class FeatureService {

  constructor(private httpClient: HttpClient) { }

  async setAsGeometry(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/setGeo', json)
      .subscribe((res)=>{
        resolve(res);
      },(err)=>{
        reject(err);
      })
    });
  }

  async setAsId(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/setId', json)
      .subscribe((res)=>{
        resolve(res)
      }, (err)=>{
        reject(err);
      })
    });
  }
}

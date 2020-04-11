import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class FeatureService {

  constructor(private httpClient: HttpClient) { }


  /**
   * Set a column as Geometry
   * 
   * @param json the json object with the details of the column
   */
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

  /**
   * Set a column as ID
   * 
   * @param json the json object with the details of the column
   */
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

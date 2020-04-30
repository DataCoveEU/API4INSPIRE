import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class HomeService {

  constructor(private httpClient: HttpClient) { }

  /**
   * Load all the important links
   */
  async getLinks() {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/getImportantLinks', {
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  /**
   * 
   * @param json the object with the details of the link which should be removed
   * Remove an important link from the list
   */
  async removeLink(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/removeImportantLink', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  /**
   * 
   * @param json the object with the details of the link which should be added
   * Add an important link to the list
   */
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

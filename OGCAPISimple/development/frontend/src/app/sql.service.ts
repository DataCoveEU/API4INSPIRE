import { Injectable } from '@angular/core';
import { resolve } from 'url';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SqlService {

  constructor(private httpClient: HttpClient) { }

  /**
   * Callt the api path to execute the SQL
   * 
   * @param json the object with the details where the sql should be executed and saved (id, query, collection name, check)
   */
  async executeSQL(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/executeSQL', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res)
      }, (err)=>{
        reject(err);
      })
    })
  }
}

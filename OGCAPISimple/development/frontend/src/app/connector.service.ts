import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class ConnectorService {

  connectors: any;
  tables: any;
  columns: any;


  constructor(private httpClient: HttpClient) { }

  /**
   * Get all connectors from the DB
   */
  async getConnector() {
    this.connectors = await new Promise((resolve, reject) =>{
      this.httpClient.post('/ogcapisimple/api/getConnectors', {
      }).subscribe((res:any)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    });
    return this.connectors;
  }

  /**
   * Get all tables from a DB
   * 
   * @param json the json object with the id of the DB
   */
  async getTables(json:object) {
    this.tables = await new Promise((resolve, reject) =>{
      this.httpClient.post('/ogcapisimple/api/getTables', json).subscribe((res)=>{
        resolve(res);;
      }, (err)=>{
        reject(err);
      })
    });
    return this.tables;
  }

  /**
   * Get all columns from a Table
   * 
   * @param json the json object with the id of the DB and the table name
   */
  async getColumn(json:object) {
    this.columns = await new Promise((resolve, reject) =>{
      this.httpClient.post('/ogcapisimple/api/getColumns', json)
      .subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
    return this.columns;
  }
  
  /**
   * Add a new DB connection
   * 
   * @param json the json object with the details of the connection
   */
  addConnector(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/addConnector', 
      json,{
      responseType: 'text'
    }).subscribe((res)=>{
      resolve(res);
    }, (err)=>{
      reject(err);
    });
    })
    
  }

  /**
   * Rename a table
   * 
   * @param json the json object with the details of the table
   */
  async renameTable(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post("/ogcapisimple/api/renameCollection", json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  /**
   * Rename a column
   * 
   * @param json The json object with the details of the column
   */
  async renameColumn(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post("/ogcapisimple/api/renameProp", json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      });
    })
  }

  /**
   * Change connector properties like the name or in postgres the port, schema, hostname, usernam,
   * password,...
   * 
   * @param json the json object with the details of the connector
   */
  async changeConnectorProps(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/setConnectorProps', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  /**
   * Exclude a table
   * 
   * @param json the json object with the details of the table to exclude
   */
  async excludeTable(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/excludeTable', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  /**
   * Exclude a column
   * 
   * @param json the json object with the details of the column to exclude
   */
  async excludeColumn(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/excludeColumn', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async excludeAllTables(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post("/ogcapisimple/api/excludeAllTables", json, {
        responseType: "text"
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async excludeAllColumns(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post("/ogcapisimple/api/excludeAllColumns", json, {
        responseType: "text"
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async deleteConnector(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post("/ogcapisimple/api/deleteConnector", json, {
        responseType: "text"
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async checkConnection(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/checkConnection', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async deleteSQL(json: object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post("/ogcapisimple/api/deleteSQL", json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async getGeoColumns(json: object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/getGeo', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }

  async getIdColumns(json: object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/getId', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    })
  }
}

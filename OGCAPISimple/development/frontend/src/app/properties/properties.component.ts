/*
    The OGC API Simple provides enviromental data
    Created on Wed Feb 26 2020
    Copyright (c) 2020 - Lukas Gäbler

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

import { Component, OnInit, ɵNOT_FOUND_CHECK_ONLY_ELEMENT_INJECTOR } from '@angular/core';

import * as $ from 'jquery';
import { ConnectorService } from '../connector.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-properties',
  templateUrl: './properties.component.html',
  styleUrls: ['./properties.component.scss']
})
export class PropertiesComponent implements OnInit {

  sqlite: boolean = false;
  postgres: boolean = false;

  connectorNameForm: FormGroup;
  connectorNameSubmitted: boolean = false;

  changeUsernameForm: FormGroup;
  changeUserSubmitted: boolean = false;

  changePasswordForm: FormGroup;
  changePasswordSubmitted: boolean = false;

  changeHostnameForm: FormGroup;
  changeHostnameSubmitted: boolean = false;

  changePortForm: FormGroup;
  changePortSubmitted: boolean = false;

  changeSchemaForm: FormGroup;
  changeSchmemaSubmitted: boolean = false;

  changeDatabaseForm: FormGroup;
  changeDatabaseSubmitted: boolean = false;

  sqlConnectorNameForm: FormGroup;
  sqlConnectorNameSub: boolean = false

  index: any = -1;

  selectedConnector: any;

  connectors: any = [{"id":"No connectors are currently available"}];

  constructor(private con: ConnectorService, private formBuilder: FormBuilder) { }

  async ngOnInit() {
    //init all the forms
    this.connectorNameForm = this.formBuilder.group({
      conName: ['', Validators.required]
    });

    this.changeUsernameForm = this.formBuilder.group({
      newName: ['', Validators.required]
    });

    this.changePasswordForm = this.formBuilder.group({
      newPwd: ['', Validators.required]
    });
  
    this.changeHostnameForm = this.formBuilder.group({
      hostname: ['', Validators.required]
    });

    this.changePortForm = this.formBuilder.group({
      newPort: ['', Validators.required]
    });

    this.changeSchemaForm = this.formBuilder.group({
      newSchema: ['', Validators.required]
    });

    this.changeDatabaseForm = this.formBuilder.group({
      name: ['', Validators.required]
    });

    this.sqlConnectorNameForm = this.formBuilder.group({
      conName: ['', Validators.required]
    });


    //Load all connectors
    this.connectors = await this.con.getConnector();    

    if(this.connectors.length == 0){
      this.connectors = [{id: "No Connectors"}];
    }

    var sel = document.getElementById('connector') as HTMLSelectElement;
    this.selectedConnector = this.connectors[0];
    
    this.index = sel.selectedIndex;
    this.index == -1 ? this.index = 0 : this.index = this.index

    //Differentiate between sqlite and postgres
    if(this.connectors[this.index].class.includes("PostgreSQL")) {
      this.postgres = true;
      this.sqlite = false;
    } else if(this.connectors[this.index].class.includes("SQLite")) {
      this.sqlite = true;
      this.postgres = false;
    }

    //Change the form depending on what connector it is
    sel.onchange = (event: any)=>{
      var cal = event.target.options[event.target.selectedIndex].getAttribute('id');
      
      this.selectedConnector = this.connectors[sel.selectedIndex];
      this.index = sel.selectedIndex;

      if(cal.includes("PostgreSQL")) {
        this.sqlite = false;
        this.postgres = true;
      } else if(cal.includes("SQLite")) {
        this.sqlite = true;
        this.postgres = false;
      }
    }
  }

  /**
   * Change a postgres connection name
   */
  submitConnectorName() {
    this.connectorNameSubmitted = true;
    if(this.connectorNameForm.invalid) {
      return;
    }
    var json = {
      'class': "postgres",
      'id': this.selectedConnector.id,
      'newid': this.connectorNameForm.value.conName
    };
    var er = document.getElementById("infoField");
    this.con.changeConnectorProps(json).then(async ()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Connectorname changed", "INFORMATION");
      await this.reloadData();
    }).catch((err)=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Connectorname not changed", "ERROR");
      console.log(err);
    })
  }

  /**
   * Change a postgres connection username
   */
  changeUsername() {
    this.changeUserSubmitted = true;
    if(this.changeUsernameForm.invalid) {
      return;
    }

    var json = {
      'class': "postgres",
      'id': this.selectedConnector.id,
      'username': this.changeUsernameForm.value.newName
    };

    var er = document.getElementById("infoField");
    this.con.changeConnectorProps(json).then(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Usernamed changed", "INFORMATION");
    }).catch((err)=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Usernamed not changed", "ERROR");
    });
  }

  /**
   * Change a psotgres connection password
   */
  changePassword() {
    this.changePasswordSubmitted = true;
    if(this.changePasswordForm.invalid) {
      return;
    }

    var json = {
      'class': "postgres",
      'id': this.selectedConnector.id,
      'password': this.changePasswordForm.value.newPwd
    };

    var er = document.getElementById("infoField");
    this.con.changeConnectorProps(json).then(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Password changed", "INFORMATION");
    }).catch((err)=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Password not changed", "ERROR");
    });
  }

  /**
   * Change a postgres connection hostname
   */
  changeHostname() {
    this.changeHostnameSubmitted = true;
    if(this.changeHostnameForm.invalid) {
      return;
    } 

    var json = {
      'class': "postgres",
      'id': this.selectedConnector.id,
      'hostname': this.changeHostnameForm.value.hostname,
    };

    var er = document.getElementById("infoField");
    this.con.changeConnectorProps(json).then(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Hostname changed", "INFORMATION");
    }).catch((err)=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Hostname not changed", "ERROR");
    });
  }

  /**
   * Change a postgres connection port
   */
  changePort() {
    this.changePortSubmitted = true;
    if(this.changePortForm.invalid) {
      return;
    }

    var er = document.getElementById("infoField");
    var json = {
      'class': "postgres",
      'id': this.selectedConnector.id,
      'port': this.changePortForm.value.newPort
    };
    this.con.changeConnectorProps(json).then(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Port changed", "INFORMATION");
    }).catch((err)=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Port not changed", "ERROR");
    });
  }

  /**
   * Chnage postgres connection schema
   */
  changeSchema() {
    this.changeSchmemaSubmitted = true;
    if(this.changeSchemaForm.invalid) {
      return;
    }

    var json = {
      'class': "postgres",
      'id': this.selectedConnector.id,
      'schema': this.changeSchemaForm.value.newSchema
    };

    var er = document.getElementById("infoField");
    this.con.changeConnectorProps(json).then(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Schema changed", "INFORMATION");
    }).catch(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Schema not changed", "ERROR");
    });
  }

  /**
   * Change a postgres connection database
   */
  changeDatabase() {
    this.changeDatabaseSubmitted = true;
    if(this.changeDatabaseForm.invalid) {
      return;
    }

    var json = {
      'class': "postgres",
      'id': this.selectedConnector.id,
      'database': this.changeDatabaseForm.value.name
    };

    var er = document.getElementById("infoField");
    this.con.changeConnectorProps(json).then(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Database changed", "INFORMATION");
    }).catch(()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Database not changed", "ERROR");
    });
  }

  /**
   * Remove a connector
   */
  async delConnector() {
    var er = document.getElementById("infoField");
    this.con.deleteConnector({'id': this.selectedConnector.id}).then(()=>{
      //Show infor message
      er.style.marginTop = "2%";
          er.innerHTML = this.messages(false, "Connection deleted", "INFORMATION");
    }).catch((err)=>{
      //Show error message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Connection not deleted", "ERROR");
    });

    this.reloadData()

  

  }

  /**
   * Change sqlite connection name
   */
  async sqLiteConnectorName() {
    this.sqlConnectorNameSub = true;
    if(this.sqlConnectorNameForm.invalid) {
      return;
    }

    var json = {
      'class': "sqlite",
      'orgid': this.selectedConnector.id,
      'id': this.sqlConnectorNameForm.value.conName
    };

    var er = document.getElementById("infoField");
    this.con.changeConnectorProps(json).then(async ()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Connector renamed", "INFORMATION")
      await this.reloadData();

    }).catch((err)=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Connector not renamed", "ERROR");
      console.log(err);
    });
  }

  /**
   * Test a connection 
   */
  testConnection() {
    var json = {
      'id': this.selectedConnector.id
    };
    var er = document.getElementById("infoField");

    this.con.checkConnection(json).then(()=>{
      //Return info message
      er.style.marginTop = "2%";
          er.innerHTML = this.messages(false, "Test was successfull", "INFORMATION");
    }).catch((err) =>{
      //Return error message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Test was not successfull", "ERROR");
    }) 
  }

  messages(isError:boolean, text: string, title: string):string {
    var erg = "";
    if(isError) {
      erg =  `<div class="card card-custom">
                <div class="card-header" style="background-color: #F56565; color: white">${title}</div>
                  <div class="card-body" style="background-color: #FFF5F5; color: #355376">
                    <p>${text}</p>
                  </div>
                </div>
              </div>`;
    } else {
      erg =  `<div class="card card-custom">
                <div class="card-header" style="background-color: #38B2AC; color: white">INFORMATION</div>
                  <div class="card-body" style="background-color: #E6FFFA; color: #234E52">
                    <p>${text}</p>
                  </div>
                </div>
              </div>`;
    }
    return erg;
  }

  async reloadData() {
      this.connectors = await this.con.getConnector();
   
      this.index = 0;
      var sel = document.getElementById('connector') as HTMLSelectElement;
      sel.value = this.index;

     this.selectedConnector = this.connectors[0];
      var cal = this.connectors[0].class;
      if(cal.includes("PostgreSQL")) {
        this.sqlite = false;
        this.postgres = true;
      } else if(cal.includes("SQLite")) {
        this.sqlite = true;
        this.postgres = false;
      }
  }

  async reloadWithOutChange() {
    var indx = this.index;
    this.connectors = await this.con.getConnector();
    this.selectedConnector = this.connectors[indx];

    if(this.connectors.length == 0) {
      this.sqlite = false;
      this.postgres = false;
    }
  }

}

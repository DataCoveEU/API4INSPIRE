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

import { Component, OnInit } from '@angular/core';

import * as $ from 'jquery';
import { ConnectorService } from '../connector.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-add-connector',
  templateUrl: './add-connector.component.html',
  styleUrls: ['./add-connector.component.scss']
})
export class AddConnectorComponent implements OnInit {

  addPostgresConnectorForm: FormGroup;
  postgresSubmitted: boolean = false;
  passwordsEquals: boolean = false;

  constructor(private conService:ConnectorService, private formBuilder:FormBuilder) { }

  ngOnInit() {
    //Initialzie the form to add a postgres connector
    this.addPostgresConnectorForm = this.formBuilder.group({
      connectorName: ['', Validators.required],
      username: ['', Validators.required],
      password: ['', Validators.required],
      hostname: ['', Validators.required],
      port: ['', Validators.required],
      schema: ['', Validators.required],
      database: ['', Validators.required]
    });

    var sel = document.getElementById("connector");

    //Handle the event if another kind of selector to add is selected
    sel.onchange = (event: any)=>{
      var cal = event.target.options[event.target.selectedIndex].getAttribute('id');
      
    };
    
  }
  
  /**
   * Handle event if a postgres connector is submitted
   */
  addPostgresConnector() {
    this.postgresSubmitted = true;
    if(this.addPostgresConnectorForm.invalid){
      return;
    }
    //Get the values from the input form
    var conName = this.addPostgresConnectorForm.value.connectorName;
    var uname = this.addPostgresConnectorForm.value.username;
    var pwd = this.addPostgresConnectorForm.value.password;
    var host = this.addPostgresConnectorForm.value.hostname;
    var port = this.addPostgresConnectorForm.value.port;
    var schema = this.addPostgresConnectorForm.value.schema;
    var database = this.addPostgresConnectorForm.value.database;

    //Create the json which will be sent to the backend
    var json = {
      "class": "postgres",
      "id": conName,
      "database": database,
      "schema": schema,
      "hostname": host,
      "port": port,
      "username": uname,
      "password": pwd,
      "isTest": false
    };

    //Call the service
    this.conService.addConnector(json).then(()=>{
      alert("Connector added")
    }).catch(()=>{
      alert("ERROR: Connector not added")
    });
  }

  /**
   *  Handle the event if a postgres connection should be tested 
   */
  addPostgresTest() {
    this.postgresSubmitted = true;
    if(this.addPostgresConnectorForm.invalid){
      return;
    }
    //Get the values from the input form
    var conName = this.addPostgresConnectorForm.value.connectorName;
    var uname = this.addPostgresConnectorForm.value.username;
    var pwd = this.addPostgresConnectorForm.value.password;
    var host = this.addPostgresConnectorForm.value.hostname;
    var port = this.addPostgresConnectorForm.value.port;
    var schema = this.addPostgresConnectorForm.value.schema;
    var database = this.addPostgresConnectorForm.value.database;

    //Create the json which will be sent to the backend
    var json = {
      "class": "postgres",
      "id": conName,
      "database": database,
      "schema": schema,
      "hostname": host,
      "port": port,
      "username": uname,
      "password": pwd,
      "isTest": true
    };

    var er = document.getElementById("errorField");
    //Call the service
    this.conService.addConnector(json).then(()=>{
      //Info message
      er.innerHTML = `<div class="card card-custom">
                        <div class="card-header" style="background-color: #38B2AC; color: white">TEST POSTGRES</div>
                          <div class="card-body" style="background-color: #E6FFFA; color: #234E52">
                            <p>Postgres connection test was successfull</p>
                          </div>
                        </div>
                      </div>`;
    }).catch(()=>{
      //Error message
      er.innerHTML = `<div class="card card-custom">
                        <div class="card-header" style="background-color: #F56565; color: white">TEST POSTGRES</div>
                          <div class="card-body" style="background-color: #FFF5F5; color: #CE303C">
                            <p>Postgres conncetion test failed</p>
                          </div>
                        </div>
                      </div>`;
    });
  }
}

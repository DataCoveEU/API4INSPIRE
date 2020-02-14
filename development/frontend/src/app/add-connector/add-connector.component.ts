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
      if(cal == "sqlite") {

      } else if(cal == "postgres") {

      }
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
      er.innerHTML = `<div class="card card-custom">
                        <div class="card-header" style="background-color: #38B2AC; color: white">TEST POSTGRES</div>
                        <div class="card-body" style="background-color: #E6FFFA; color: #234E52">
                            <p>
                                Connector add test was successfully
                            </p>
                            </div>
                    </div>`;
    }).catch(()=>{
      er.innerHTML = `<div class="card card-custom">
                                <div class="card-header" style="background-color: #F56565; color: white">TEST POSTGRES</div>
                                  <div class="card-body" style="background-color: #FFF5F5; color: #CE303C">
                                    <p>Connector NOT added</p>
                                  </div>
                                </div>
                              </div>`;
    });
  }
}

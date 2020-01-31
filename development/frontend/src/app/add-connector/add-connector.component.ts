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
  addPostgresConnector(isTest: boolean) {
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
      "isTest": isTest
    };

    //Call the service
    this.conService.addConnector(json).then(()=>{
      alert("Connector added")
    }).catch(()=>{
      alert("ERROR: Connector not added")
    });
  }
}

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

  isSQLite:boolean = false;
  sqlite: any;

  isPostgres: boolean = false;

  addPostgresConnectorForm: FormGroup;
  postgresSubmitted: boolean = false;
  passwordsEquals: boolean = false;

  addSQLiteConnectorForm: FormGroup;
  sqliteSubmitted: boolean = false;

  constructor(private conService:ConnectorService, private formBuilder:FormBuilder) { }

  ngOnInit() {
    //Initialzie the form to add a postgres connector
    this.addPostgresConnectorForm = this.formBuilder.group({
      connectorName: ['', Validators.required],
      username: ['', Validators.required],
      password: ['', Validators.required],
      repeatPassword: ['', Validators.required],
      hostname: ['', Validators.required],
      port: ['', Validators.required],
      schema: ['', Validators.required],
      database: ['', Validators.required]
    });

    //Initalize the form to add a sqlite connector
    this.addSQLiteConnectorForm = this.formBuilder.group({
      conName: ['', Validators.required],
      path: ['', Validators.required]
    })

    this.isSQLite = true;
    this.sqlite = document.getElementById("content");
    var sel = document.getElementById("connector");

    //Handle the event if another kind of selector to add is selected
    sel.onchange = (event: any)=>{
      var cal = event.target.options[event.target.selectedIndex].getAttribute('id');
      if(cal == "sqlite") {
        this.isSQLite = true;
        this.isPostgres = false;
      } else if(cal == "postgres") {
        this.isSQLite = false;
        this.isPostgres = true;
      }
    };
    
  }

  /**
   * Handle the event if a sqlite connector is submitted
   */
  addSQLiteConnector() {
    this.sqliteSubmitted = true;
    if(this.addSQLiteConnectorForm.invalid) {
      return;
    }
    var conName = this.addSQLiteConnectorForm.value.conName;
    var path = this.addSQLiteConnectorForm.value.path
    var files: any = document.getElementById('fileSel')

    //Create the JSON for the service
    var json = {
      "class": "sqlite",
      "id": conName,
      "database": "DatabaseB",
      "schema": null,
      "hostname": null,
      "path": path,
      "port": null
    }

    //It is not yet possible to add a sqlite connector
    alert("It is not yet possible to add a SQLite Connector");
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
    var repwd = this.addPostgresConnectorForm.value.repeatPassword;
    var host = this.addPostgresConnectorForm.value.hostname;
    var port = this.addPostgresConnectorForm.value.port;
    var schema = this.addPostgresConnectorForm.value.schema;
    var database = this.addPostgresConnectorForm.value.database;

    //Check if the repeated password is valid
    if(pwd != repwd) {
      this.passwordsEquals = true;
      return;
    }
    this.passwordsEquals = false;

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
    this.conService.addConnector(json);
  }
}

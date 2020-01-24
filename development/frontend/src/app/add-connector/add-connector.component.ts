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

    this.addSQLiteConnectorForm = this.formBuilder.group({
      conName: ['', Validators.required],
      path: ['', Validators.required]
    })

    this.isSQLite = true;
    this.sqlite = document.getElementById("content");
    var sel = document.getElementById("connector");
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

  addSQLiteConnector() {
    this.sqliteSubmitted = true;
    if(this.addSQLiteConnectorForm.invalid) {
      return;
    }
    var conName = this.addSQLiteConnectorForm.value.conName;
    var path = this.addSQLiteConnectorForm.value.path
    var files: any = document.getElementById('fileSel')
    for(var x of files.files) {
      console.log("file: " + x.path);
    }
    console.log($('#fileSel').files)
    var json = {
      "class": "sqlite",
      "id": conName,
      "database": "DatabaseB",
      "schema": null,
      "hostname": null,
      "path": path,
      "port": null
    }

    alert("It is not yet possible to add a SQLite Connector");
  }

  addPostgresConnector() {
    this.postgresSubmitted = true;
    if(this.addPostgresConnectorForm.invalid){
      return;
    }
    var conName = this.addPostgresConnectorForm.value.connectorName;
    var uname = this.addPostgresConnectorForm.value.username;
    var pwd = this.addPostgresConnectorForm.value.password;
    var repwd = this.addPostgresConnectorForm.value.repeatPassword;
    var host = this.addPostgresConnectorForm.value.hostname;
    var port = this.addPostgresConnectorForm.value.port;
    var schema = this.addPostgresConnectorForm.value.schema;
    var database = this.addPostgresConnectorForm.value.database;

    if(pwd != repwd) {
      this.passwordsEquals = true;
      return;
    }
    this.passwordsEquals = false;

    var json = {
      "class": "postgres",
      "id": conName,
      "database": database,
      "schema": schema,
      "hostname": host,
      "port": port
    };
  }
}

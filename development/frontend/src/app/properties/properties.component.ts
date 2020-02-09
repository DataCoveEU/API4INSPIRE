import { Component, OnInit } from '@angular/core';

import * as $ from 'jquery';
import { ConnectorService } from '../connector.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-properties',
  templateUrl: './properties.component.html',
  styleUrls: ['./properties.component.scss']
})
export class PropertiesComponent implements OnInit {

  changeNameForm: FormGroup; 
  changeNameSubmitted: boolean = false;

  allConnectors:any;
  connectors: any;

  constructor(private con: ConnectorService, private formBuilder: FormBuilder, private conService: ConnectorService) { }

  async ngOnInit() {

    this.allConnectors = await this.con.getConnector();

    this.changeNameForm = this.formBuilder.group({
      newName: ['', Validators.required]
    });
  }

  changeName() {

  }

  sqlite() {
    var header = document.getElementById("sqlite");
    var pos = document.getElementById("postgres");
    pos.style.backgroundColor = "white";
    header.style.backgroundColor = "#EEEEEE";

    
  }

  postgres() {
    var header = document.getElementById("postgres");
    var sq = document.getElementById("sqlite");
    sq.style.backgroundColor = "white";
    header.style.backgroundColor = "#EEEEEE";
  }
}

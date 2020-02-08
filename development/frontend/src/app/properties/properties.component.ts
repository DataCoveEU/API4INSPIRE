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

  sqlite: boolean = false;
  postgres: boolean = false;

  connectorNameForm: FormGroup;
  connectorNameSubmitted: boolean = false;


  connectors: any = [{"id":"No connectors are currently available"}];

  constructor(private con: ConnectorService, private formBuilder: FormBuilder, private conService: ConnectorService) { }

  async ngOnInit() {

    this.connectorNameForm = this.formBuilder.group({
      conName: ['', Validators.required]
    })

    this.sqlite = true;

    //Load all connectors
    this.connectors = await this.con.getConnector();
    
    console.log(this.connectors);

    var sel = document.getElementById('connector');
    //Change the form depending on what connector it is
    sel.onchange = (event: any)=>{
      var cal = event.target.options[event.target.selectedIndex].getAttribute('id');
      if(cal == "postgres") {
        this.sqlite = false;
        this.postgres = true;
      } else if(cal == "sqlite") {
        this.sqlite = true;
        this.postgres = false;
      }
    }
  }

  submitConnectorName() {
    this.connectorNameSubmitted = true;
    if(this.connectorNameForm.invalid) {
      return;
    }
    alert("Ich brauch die Admin doc")
  }

}

import { Component, OnInit } from '@angular/core';

import * as $ from 'jquery';
import { ConnectorService } from '../connector.service';

@Component({
  selector: 'app-properties',
  templateUrl: './properties.component.html',
  styleUrls: ['./properties.component.scss']
})
export class PropertiesComponent implements OnInit {

  sqlite: boolean = false;
  postgres: boolean = false;

  connectors: any = [{"id":"No connectors are currently available"}];

  constructor(private con: ConnectorService) { }

  async ngOnInit() {
    this.sqlite = true;

    this.connectors = await this.con.getConnector();

    var sel = document.getElementById('connector');
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

}

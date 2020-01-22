import { Component, OnInit } from '@angular/core';

import * as $ from 'jquery';

@Component({
  selector: 'app-add-connector',
  templateUrl: './add-connector.component.html',
  styleUrls: ['./add-connector.component.scss']
})
export class AddConnectorComponent implements OnInit {

  isSQLite:boolean = false;
  sqlite: any;

  isPostgres: boolean = false;


  constructor() { }

  ngOnInit() {
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
}

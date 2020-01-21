import { Component, OnInit } from '@angular/core';

import * as $ from 'jquery';

@Component({
  selector: 'app-add-connector',
  templateUrl: './add-connector.component.html',
  styleUrls: ['./add-connector.component.scss']
})
export class AddConnectorComponent implements OnInit {

  isSQLite:boolean = true;
  sqlite: any;

  constructor() { }

  ngOnInit() {
    this.sqlite = document.getElementById("content");
    var sel = document.getElementById("connector");
    sel.onchange = (event: any)=>{
      var cal = event.target.options[event.target.selectedIndex].getAttribute('id');
      if(cal == "sqlite") {
        this.showSQLite();
      } else if(cal == "postgres") {
        this.showPostgres();
      }
    };
  }

  showSQLite() {
    
  }

  showPostgres() {
      this.isSQLite = false;
      $('#content').html("" +
      '<div class="row" style="width: 100%; margin-top: 1%">' +
        '<div class="col-sm-6">' +
          '<input type="text" class="form-control" style="width: 100%" placeholder="Conncetor name"/>' +
        '</div>' +
      '</div>' +
      '<div class="row" style="width: 100%; margin-top: 1%">' +
        '<div class="col-sm-6">' +
          '<input type="text" class="form-control" style="width: 100%" placeholder="Username" />' +
        '</div>' +
        '<div class="col-sm-6">' +
          '<input type="password" class="form-control" style="width: 100%" placeholder="Password" />' + 
        '</div>' +
      '</div>' +
      '<div class="row" style="width: 100%; margin-top: 1%">' +
        '<div class="col-sm-6" style="width: 100%"></div>' +
        '<div class="col-sm-6">' +
          '<input type="password" class="form-control" style="width: 100%" placeholder="Repeat password" />' +
        '</div>' +
      '</div>'+ 
      '<div class="row" style="width: 100%">' +
        '<div class="col-sm-6">' +
          '<input class="form-control" type="text" style="width: 100%" placeholder="Hostname" />' +
        '</div>' +
      '</div>' +
      '<div class="row" style="width: 100%; margin-top: 1%">' +
        '<div class="col-sm-6">' +
          '<input type="number" class="form-control" style="width: 100%" placeholder="Port" />' +
        '</div>' +
      '</div>' +
      '<div class="row" style="width: 100%; margin-top: 1%">' +
        '<div class="col-sm-6">' +
          '<input class="form-control" type="text" style="width: 100%" placeholder="Schema" />' +
        '</div>' +
      '</div>' + 
      '<div class="row" style="width: 100%; margin-top: 1%">' +
        '<div class="col-sm-6">' +
          '<button class="btn btn-success" style="width: 100%">Test connction</button>' +
        '</div>' +
        '<div class="col-sm-6">' +
          '<button class="btn btn-success" style="width: 100%">Add connector</button>' +
        '</div>' +
      '</div>'
      );
  }
}

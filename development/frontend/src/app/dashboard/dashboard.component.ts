import { Component, OnInit } from '@angular/core';

import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConnectorService } from '../connector.service';
import { SqlService } from '../sql.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  tableNames = [];

  columnNames = [];
  columnConfigNames = [];

  connectors: any = [];

  selectedConnector: any;

  showCols: boolean = false;
  showRenameTable: boolean = false;
  showRenameCol: boolean = false;

  tableSelect: boolean = false;
  idTableSelected: string = "";

  columnSelected: boolean = false;
  idColumnSelected: string = "";

  renameTableForm: FormGroup;
  tableNameSubmitted: boolean = false;

  renameColumnForm: FormGroup;
  columnNameSubmitted: boolean = false;

  sqlForm: FormGroup;
  sqlSubmitted: boolean = false;

  sqlSucess: boolean = true;

  constructor(private formBuilder: FormBuilder, private conService: ConnectorService, private sqlService: SqlService) { }

  async ngOnInit() {
    //Init the forms to rename the tables and columns and to execute the sql query
    this.sqlForm = this.formBuilder.group({
      collectionId: ['', Validators.required],
      sqlQuery: ['', Validators.required]
    });

    this.renameTableForm = this.formBuilder.group({
      tableName: ['', Validators.required]
    });

    this.renameColumnForm = this.formBuilder.group({
      columnName: ['', Validators.required]
    });

    //Load all the connectors from the config
    this.connectors = await this.conService.getConnector();

    if(this.connectors.length == 0){
      this.connectors = [{id: "No Connectors"}];
    }

    var select = document.getElementById("selectField") as HTMLSelectElement;
    var index = select.selectedIndex;
    
    if(index == -1){index = 0}
  
      this.selectedConnector = this.connectors[index];
      console.log(index);
      console.log(this.connectors);
      //Load the table names from the selected connector
      this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });

    //Eevent when another conncetor in the dropdown is selected
    select.onchange = async (event: any)=>{
        var select = document.getElementById("selectField") as HTMLSelectElement;
        this.selectedConnector = this.connectors[select.selectedIndex];

        this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });
        this.tableSelect = false;
      }
  }

  /**
   * Handle the click event when a table row with table names is clicked
   *
   * @param name the name or the id of the table row that has been clicked
   */
  async onClickTableName(name: string) {
    // If a "tablename row" is already selected, then
    // you have to change the style
    if(this.tableSelect) {
      var change = document.getElementById(this.idTableSelected);
      change.style.backgroundColor = "white";
      change.style.color = "black";

      // If a "columnname row" is also selected
      // then you have to deselect is
      if(this.columnSelected) {
        var col = document.getElementById(this.idColumnSelected)
        col.style.backgroundColor = "white";
        col.style.color = "black";

        this.columnSelected = false;
        this.showRenameCol = false;
      }
    }

    // Change the style of the selected row
    // and save the id of it
    var row = document.getElementById(name);
    row.style.color = "white";
    row.style.backgroundColor = "#0069D9";
    this.showRenameTable = true;
    this.tableSelect = true;
    this.idTableSelected = name;
    this.showCols = true;

    this.columnNames = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+name});
  }

  /**
   * Handle the click event when a table row with columns is clicked
   *
   * @param name the name or id of the table row that has been clicked
   */
  onClickColumn(name: string) {
    // If a "columnname row" is already selected
    // it has to be deselected
    if(this.columnSelected) {
      var change = document.getElementById(this.idColumnSelected);
      change.style.backgroundColor = "white";
      change.style.color = "black";
    }

    // Change the style of the selected row
    // and save the id of it
    var row = document.getElementById(name);
    row.style.color = "white";
    row.style.backgroundColor = "#0069D9";
    this.showRenameCol = true;
    this.idColumnSelected = name;
    this.columnSelected = true;
  }

  /**
   * Handle the click event when the new table name is submitted
   */
  submitTable() {
    this.tableNameSubmitted = true;
    if(this.renameTableForm.invalid) {
      return;
    }

    //Init the JSON for the backend
    var json = {
      'id': this.selectedConnector.id,
      'orgName': this.idTableSelected,
      'alias': this.renameTableForm.value.tableName
    };

    this.conService.renameTable(json).then(
      async ()=>{
        this.reload();
      }
    ).catch(()=>{
      alert("Not renamed")
    });

  }

  /**
   * Handle the click event when the new column name is submitted
   */
  submitColumn() {
    this.columnNameSubmitted = true;
    if(this.renameColumnForm.invalid) {
      return;
    }

    //Init the JSON for the backend
    var json = {
      'id': this.selectedConnector.id,
      'table': this.idTableSelected,
      'alias': this.renameColumnForm.value.columnName,
      'orgName': this.idColumnSelected
    };

    this.conService.renameColumn(json).then(async()=>{
      this.reload();
      this.columnNames = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+this.idTableSelected});
    }).catch(()=>{
      alert("Not renamed")
    });

  }

  /**
   * Reload the connectors and tables
   */
  async reload() {
    this.connectors = await this.conService.getConnector();

    var select = document.getElementById("selectField") as HTMLSelectElement;
    this.selectedConnector = this.connectors[select.selectedIndex]

    this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id }); //{'id':'Inspire'}

  }

  /**
   * Reload and load the new tables
   */
  async loadNewTables() {
    var select = document.getElementById("selectField") as HTMLSelectElement;
    this.selectedConnector = this.connectors[select.selectedIndex]

    this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });
  }

  /**
   * Handle the "execute" event for the sql query
   */
  executeSQL(check: boolean) {
    this.sqlSubmitted = true;
    if(this.sqlForm.invalid) {
      return;
    }

    var json = {
      'id': this.selectedConnector.id,
      'sql': this.sqlForm.value.sqlQuery,
      'collectionName': this.sqlForm.value.collectionId//,
      //'check':check
    };

    this.sqlService.executeSQL(json).then(()=>{
      alert("SQL executed successfully")
    }).catch((err)=>{
      this.sqlSucess = false;
      var errorText = document.getElementById('sqlError');
      errorText.innerHTML = err;
      alert("Not executed successfully")
    });

  }

}

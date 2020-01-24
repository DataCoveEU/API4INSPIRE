import { Component, OnInit } from '@angular/core';

import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConnectorService } from '../connector.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  tableNames = ['Table 1', 'Table 2', 'Table 3', 'Table 4'];

  columnNames = ['Col 1', 'Col 2', 'Col 3'];
  columnConfigNames = ['Conf 1', '', 'Conf 2'];

  connectors: any = [{"name": "No connectors available"}];

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

  constructor(private formBuilder: FormBuilder, private conService: ConnectorService) { }

  async ngOnInit() {     
    this.connectors = await this.conService.getConnector();

    this.renameTableForm = this.formBuilder.group({
      tableName: ['', Validators.required]
    });
    
    this.renameColumnForm = this.formBuilder.group({
      columnName: ['', Validators.required]
    });

    var select = document.getElementById("selectField") as HTMLSelectElement;
    this.selectedConnector = this.connectors[select.selectedIndex]

    this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id }); //{'id':'Inspire'}

    //Eevent when another conncetor in the dropdown is selected
    select.onchange = async (event: any)=>{
        this.selectedConnector = event.target.value;
        this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id});
        this.idTableSelected = "";
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

  submitTable() {
    this.tableNameSubmitted = true;
    if(this.renameTableForm.invalid) {
      return;
    }

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

  submitColumn() {
    this.columnNameSubmitted = true;
    if(this.renameColumnForm.invalid) {
      return;
    }

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

  async reload() {
    this.connectors = await this.conService.getConnector();

    var select = document.getElementById("selectField") as HTMLSelectElement;
    this.selectedConnector = this.connectors[select.selectedIndex]

    this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id }); //{'id':'Inspire'}

  }
}
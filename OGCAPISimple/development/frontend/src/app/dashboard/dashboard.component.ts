/*
    The OGC API Simple provides environmental data
    Created on Wed Feb 26 2020
    Copyright (c) 2020 - Lukas Gäbler

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/


import { Component, OnInit } from '@angular/core';

import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConnectorService } from '../connector.service';
import { SqlService } from '../sql.service';
import { FeatureService } from '../feature.service';
import { HomeService } from '../home.service';
import { async } from '@angular/core/testing';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  //The table names that will be displayed
  tableNames = [];

  //The column names that will be displayed
  columnNames = [];
  //columnConfigNames = [];

  //The connectors loaded from the config file
  connectors: any = [];

  //The connector which is selected
  selectedConnector: any;
  indexSelectedConnector:any;

  //The important links from the config file
  importantLinks: any = ["Lukas", "Tobias", "Kathi", "Klaus"];

  //The columns will be shown if a table is selected
  showCols: boolean = false;
  //The form to assign a collection name will be shown if a table is selected
  showRenameTable: boolean = false;
  //The form to assign a property name will be shown if a column is selected
  showRenameCol: boolean = false;

  //Check if a table is selected
  tableSelect: boolean = false;
  //The id of the selected table
  idTableSelected: string = "";

  //Check if a column is selected
  columnSelected: boolean = false;
  //The id of the selected column
  idColumnSelected: string = "";

  //The form to assign a collection name
  renameTableForm: FormGroup;
  tableNameSubmitted: boolean = false;

  //The form to assign a property name
  renameColumnForm: FormGroup;
  columnNameSubmitted: boolean = false;

  //The form to execute a sql query
  sqlForm: FormGroup;
  sqlSubmitted: boolean = false;

  //The form to add an important link
  addImportantLinkFrom: FormGroup;
  addLinkSubmitted: boolean = false;

  //Check if the sql query was executed successfully or not
  sqlNotSuccess: boolean = false;

  //Show the error field or not show it
  errorField: boolean = false;

  canBeUsedAsGeoColumn: boolean = false;
  canBeUsedAsIdColumn: boolean = false;

  availableAsGeoColumn: any = [];
  availableAsIdColumn: any = [];

  geoColumn: string = "";
  idColumn: string = "";

  allTablesExcluded: boolean;
  allColumnsExcluded: boolean = false;

  queryName = "";
  query = "";

  constructor(  private formBuilder: FormBuilder,
                private conService: ConnectorService,
                private featureService: FeatureService,
                private sqlService: SqlService,
                private homeService: HomeService) { }



  async ngOnInit() {
    //Init the forms to rename the tables and columns and to execute the sql query
    this.sqlForm = this.formBuilder.group({
      collectionId: ['', Validators.required],
      sqlQuery: ['', Validators.required]
    });

    this.addImportantLinkFrom = this.formBuilder.group({
      addLink: ['', Validators.required],
      displayName: ['', Validators.required]
    });

    this.renameTableForm = this.formBuilder.group({
      tableName: ['', Validators.required]
    });

    this.renameColumnForm = this.formBuilder.group({
      columnName: ['', Validators.required]
    });

    this.importantLinks = await this.homeService.getLinks();


    //Load all the connectors from the config
    this.connectors = await this.conService.getConnector();

    if(this.connectors.length == 0){
      this.connectors = [{id: "No Connectors"}];
    }

    var select = document.getElementById("selectField") as HTMLSelectElement;
    var index = select.selectedIndex;

    index == -1 ? index = 0 : index = index

    this.selectedConnector = this.connectors[index];
    this.indexSelectedConnector = 0;
    //Load the table names from the selected connector
    this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });
    

    //Event when another connector in the dropdown is selected
    select.onchange = async (event: any)=>{
      var select = document.getElementById("selectField") as HTMLSelectElement;
      this.selectedConnector = this.connectors[select.selectedIndex];
      this.indexSelectedConnector = select.selectedIndex;

      var indx = this.indexSelectedConnector;
      await this.reloadWithOutChange();
      select.value = indx;

      this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });
      var change = document.getElementById(this.idTableSelected);
      if(change != null) {
        change.style.backgroundColor = "white";
        change.style.color = "black";
      }
      this.tableSelect = false;
      this.idTableSelected = "";
      this.idColumnSelected = "";
      this.columnSelected = false;
      this.columnNames = [];
      this.showCols = false;
      this.showRenameCol = false;
      this.showRenameTable = false;

      this.allTablesExcluded = this.areAllTablesExcluded();
    }

    //Check if all the tables are excluded
    for(let i = 0; i < this.tableNames.length; i++) {
      if(this.selectedConnector.config[this.tableNames[i]] == undefined) {
        this.allTablesExcluded = false;
        break;
      } else if(this.selectedConnector.config[this.tableNames[i]].exclude == undefined) {
        this.allTablesExcluded = false;
        break;
      } else if(this.selectedConnector.config[this.tableNames[i]].exclude == false) {
        this.allTablesExcluded = false;
        break;
      } else {
        this.allTablesExcluded = true;
      }
    }
  }



  /**
   * Handle the click event when a table row with table names is clicked
   *
   * @param name the name or the id of the table row that has been clicked
   */
  async onClickTableName(name: string) {

    if(this.selectedConnector.sqlString[name] != undefined) {
      this.queryName = name;
      this.query = this.selectedConnector.sqlString[name];
    } else {
      this.queryName = "";
      this.query = "";
    }
    // If a "table name row" is already selected, then
    // you have to change the style
    if(this.tableSelect) {
      var change = document.getElementById(this.idTableSelected);
      change.style.backgroundColor = "white";
      change.style.color = "black";

      // If a "column name row" is also selected
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
    this.availableAsGeoColumn = await this.conService.getGeoColumns({'id': this.selectedConnector.id, 'table':''+name});
    this.availableAsIdColumn = await this.conService.getIdColumns({'id': this.selectedConnector.id, 'table':''+name});
      
    
    if(this.selectedConnector.config[name] == undefined) {
//      console.log("Connector for this table is undefined")
      this.geoColumn = "";
    } else if(this.selectedConnector.config[name].geoCol == undefined) {
//      console.log("There is no geo column for this table yet")
      this.geoColumn = "";
    } else {
      this.geoColumn = this.selectedConnector.config[name].geoCol
    }

    if(this.selectedConnector.config[name] == undefined) {
//      console.log("Connector for this table is undefined")
      this.idColumn = "";
    } else if(this.selectedConnector.config[name].idCol == undefined) {
//      console.log("There is no id column for this table yet")
      this.idColumn = "";
    } else {
      this.idColumn = this.selectedConnector.config[name].idCol
    }
    this.allColumnsExcluded = this.checkIfAllColumnExcluded();

    if(this.selectedConnector.sqlString[this.idTableSelected] != null) {
      this.canBeUsedAsGeoColumn = true;
      this.canBeUsedAsIdColumn = true;
    }
  }

  /**
   * Handle the click event when a table row with columns is clicked
   *
   * @param name the name or id of the table row that has been clicked
   */
  onClickColumn(name: string) {
    if(this.availableAsGeoColumn.includes(name)) {
      this.canBeUsedAsGeoColumn = true;
    } else {
      this.canBeUsedAsGeoColumn = false;
    }

    if(this.availableAsIdColumn.includes(name)){
      this.canBeUsedAsIdColumn = true;
    } else {
      this.canBeUsedAsIdColumn = false;
    }
   

    // If a "column name row" is already selected
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

    if(this.selectedConnector.sqlString[this.idTableSelected] != null) {
      this.canBeUsedAsGeoColumn = true;
      this.canBeUsedAsIdColumn = true;
    }
  }

  /**
   * Handle the click event when the new table name is submitted
   */
  async submitTable() {
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

    if(this.tableNames.includes(this.renameTableForm.value.tableName)) {
      var er = document.getElementById("infoField");
          er.style.marginTop = "2%";
          er.innerHTML = this.messages(true, "This name is already assigned to another table", "ERROR");
          return;
    }

    //The unique names have to be on all of the databases
    for(let i = 0;  i < this.connectors.length; i++) {
      var con = this.connectors[i];
      var tab = await this.conService.getTables({'id': con.id });
      var er = document.getElementById("infoField");
      //The unique names have to be on all tables
      for(let j = 0; j < tab.length; j++) {
        if(con.config[tab[j]] == undefined) {

        } else if(con.config[tab[j]].alias == undefined) {

        } else if(con.config[tab[j]].alias == this.renameTableForm.value.tableName) {
          //Error Message
          er.style.marginTop = "2%";
          er.innerHTML = this.messages(true, `This name is already assigned to another table. Table: ${tab[j]}`, "ERROR");
          return;
        }
      }
    }

    this.conService.renameTable(json).then(
      async ()=>{
          //Info message
          er.style.marginTop = "2%";
          er.innerHTML = this.messages(false, "Table renamed successfully", "INFORMATION");
          var indx = this.indexSelectedConnector;
          await this.reloadWithOutChange();
          var select = document.getElementById("selectField") as HTMLSelectElement;
          select.value = indx;

      }
    ).catch(()=>{
      //Error Message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Table not renamed", "ERROR");
    })

  }

  async reloadWithOutChange() {
    var indx = this.indexSelectedConnector;
    this.connectors = await this.conService.getConnector();
    this.selectedConnector = this.connectors[indx];
    this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });
  }

  /**
   * Handle the click event when the new column name is submitted
   */
  async submitColumn() {
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

    var er = document.getElementById("infoField");
    if(this.columnNames.includes(this.renameColumnForm.value.columnName)) {
      //Error message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "This name is the original name of another column", "ERROR");
      return;
    }

    for(let i = 0; i < this.columnNames.length; i++) {
      if(this.selectedConnector.config[this.idTableSelected] == undefined) {

      } else if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]] == undefined) {

      } else if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]].alias == undefined) {

      } else if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]].alias == this.renameColumnForm.value.columnName) {
        //Error message
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(true, `This name is already assigned to a column: ${this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]].alias}`, "ERROR");
        return;
      }
    }
    var er = document.getElementById("infoField");
    this.conService.renameColumn(json).then(async()=>{
      var indx = this.indexSelectedConnector;
      await this.reloadWithOutChange();
      var select = document.getElementById("selectField") as HTMLSelectElement;
      select.value = indx;
      this.columnNames = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+this.idTableSelected});
      //Info message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Column renamed successful", "INFORMATION");
    }).catch(()=>{
          //Error message
          er.style.marginTop = "2%";
          er.innerHTML = this.messages(true, "Column not renamed", "ERROR");
    });

  }

 /**
   * Reload the connectors and tables
   */
  async reload(query) {
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
  executeSQL() {
    this.sqlSubmitted = true;
    if(this.sqlForm.invalid) {
      return;
    }

    var json = {
      'id': this.selectedConnector.id,
      'sql': this.sqlForm.value.sqlQuery,
      'collectionName': this.sqlForm.value.collectionId,
      'check': false
    };

    var errorText = document.getElementById('sqlError');
    this.sqlService.executeSQL(json).then(
      async ()=>{
        //Show info message
        errorText.innerHTML = this.messages(false, "SQL executed successful. The view has been added to the list of collections above", "INFORMATION");
        this.reload(false);

      }
    ).catch((err)=>{
      this.sqlNotSuccess = true;
      //Show error message
      errorText.innerHTML = this.messages(true, `${err.error}`, "SQL ERROR");
    });

  }

  /**
   * Handle the "test SQL" button click event
   */
  testSQL() {
    this.sqlSubmitted = true;
    if(this.sqlForm.invalid) {
      return;
    }

    var json = {
      'id': this.selectedConnector.id,
      'sql': this.sqlForm.value.sqlQuery,
      'collectionName': this.sqlForm.value.collectionId,
      'check': true
    };

    var errorText = document.getElementById('sqlError');
    //Call the service
    this.sqlService.executeSQL(json).then(
      async ()=>{
        //Show info message
        errorText.innerHTML = this.messages(false, "The SQL test was successful", "TEST INFORMATION");
      }
    ).catch((err)=>{
      this.sqlNotSuccess = true;
      //Show error message
      errorText.innerHTML = this.messages(true, `${err.error}`, "SQL TEST ERROR");
    });

  }

  /**
   * Handle the exclude event when the checkbox is changed in the tables
   */
  async excludeTable(tableName: string) {
    var cb = document.getElementById("checkbox-" + tableName) as HTMLInputElement;
    var checked: boolean = cb.checked;
    var bool: boolean = false;
    if(checked) {
      bool = true;
    } else {
      bool = false;
    }
    var json = {
      'id': this.selectedConnector.id,
      'table': tableName,
      'exclude': bool
    };

    //Call the service
    await this.conService.excludeTable(json);
    //Check if all the tables are excluded
    this.allTablesExcluded = this.areAllTableExcludedCheckbox();

    var indx = this.indexSelectedConnector;
    await this.reloadWithOutChange();
    var select = document.getElementById("selectField") as HTMLSelectElement;
    select.value = indx;
  }

  /**
   * Exclude a column
   *
   * @param colName then name of the column that should be excluded
   */
  excludeColumn(colName: string) {
    var cb = document.getElementById("checkbox-" + colName) as HTMLInputElement;
    var checked: boolean = cb.checked;
    var bool: boolean = false;
    var er = document.getElementById("infoField");
    if(colName == this.geoColumn) {
      //Show error message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, `${colName} cant be excluded as it is the GEO Column`, "ERROR");
      return;
    }

    if(colName == this.idColumn) {
      //Show error message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, `${colName} cant be excluded as it is the ID Column`, "ERROR");
      return;
    }
    if(checked) {
      bool = true;
    } else {
      bool = false;
    }
    var json = {
      'id': this.selectedConnector.id,
      'table': this.idTableSelected,
      'column': colName,
      'exclude': bool
    };
    //Call the service to exclude the column
    this.conService.excludeColumn(json);
    //Check if all the columns are excluded
    this.allColumnsExcluded = this.checkIfAllColumnExcludedCheckbox();
  }


  /**
   * Handle the click event when a column should be used as ID
   */
  async useAsId()  {
    var checkbox = document.getElementById("useAsId") as HTMLInputElement;
    var checked:boolean = checkbox.checked;
    var setTo: boolean;
    var json;
    if(checked) {
      setTo = false;
      json = {
        'id': this.selectedConnector.id,
        'table': this.idTableSelected,
        'column': this.idColumnSelected
      }

    } else {
      setTo = true;
      json = {
        'id': this.selectedConnector.id,
        'table': this.idTableSelected,
        'column': null
      }
    }

    var er = document.getElementById("infoField");
    this.featureService.setAsId(json).then(async()=>{
      if(setTo) {
        //Show info message
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(false, `${this.idColumnSelected} is no more the ID column`, "INFORMATION")
        this.idColumn = "";

        var indx = this.indexSelectedConnector;
        await this.reloadWithOutChange();
        var select = document.getElementById("selectField") as HTMLSelectElement;
        select.value = indx;
        this.columnNames = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+this.idTableSelected});
      } else {
        //Show info message
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(false, `${this.idColumnSelected} is now the ID column`, "INFORMATION")
        this.idColumn = this.idColumnSelected;

        var indx = this.indexSelectedConnector;
        await this.reloadWithOutChange();
        var select = document.getElementById("selectField") as HTMLSelectElement;
        select.value = indx;
        this.columnNames = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+this.idTableSelected});
      }
      ;
      }).catch((err)=>{
        //Show error message
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(true, "Not selected as ID column", "ERROR");
    });
    
  }

  /**
   * Handle the click event when a column should be used as geometry
   */
  useAsGeometry() {
    var checkbox = document.getElementById("useAsGeometry") as HTMLInputElement;
    var checked:boolean = checkbox.checked;
    var setTo: boolean;
    var json;
    if(checked) {
      setTo = false;
      json = {
        'id': this.selectedConnector.id,
        'table': this.idTableSelected,
        'column': this.idColumnSelected
      }

    } else {
      setTo = true;
      json = {
        'id': this.selectedConnector.id,
        'table': this.idTableSelected,
        'column': null
      }


    }
    var er = document.getElementById("infoField");
    this.geoColumn = this.idColumnSelected;
    this.featureService.setAsGeometry(json).then(async()=>{
      //Show info message if the service call was successful
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, `${this.idColumnSelected} is now the GEO column`, "INFORMATION");

      if(setTo) {
        //is no more geo col
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(false, `${this.idColumnSelected} is no more the GEO column`, "INFORMATION")
        this.geoColumn = "";


        var indx = this.indexSelectedConnector;
        await this.reloadWithOutChange();
        var select = document.getElementById("selectField") as HTMLSelectElement;
        select.value = indx;
        this.columnNames = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+this.idTableSelected});
      } else {
        //is now geo col
         er.style.marginTop = "2%";
         er.innerHTML = this.messages(false, `${this.idColumnSelected} is now the GEO column`, "INFORMATION") 
 
         var indx = this.indexSelectedConnector;
         await this.reloadWithOutChange();
         var select = document.getElementById("selectField") as HTMLSelectElement;
         select.value = indx;
         this.columnNames = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+this.idTableSelected});
      }

    }).catch(()=>{
      //Show an error message if the call wasn't successful
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(true, "Not selected as GEO column", "ERROR");
    });
  }

  /**
   * Handle the click event when all tables should be included or excluded
   */
  async excludeAllTables() {
    var tables:any = document.getElementsByClassName("excludeTable");
    var excludeAll:any = document.getElementById("excludeAllTables");
    var exclude: Boolean;
    if(excludeAll.checked) {
      // After clicking the checkbox is checked
      // so all og the tables will be excluded
      exclude = true;
      for(var i = 0; i < tables.length; i++) {
        tables[i].checked = "checked";
      }
      this.allTablesExcluded = true;
    } else {
      // After clicking the checkbox is not checked
      // so all of the tables will be included
      exclude = false;
      for(var i = 0; i < tables.length; i++) {
        tables[i].checked = false;
      }
      this.allTablesExcluded = false;
    }
    var json = {
      'id': this.selectedConnector.id,
      'exclude': exclude
    };

    this.conService.excludeAllTables(json);
  }

  /**
   * Handle the click event when all the columns should be included or excluded
   */
  excludeAllColumns() {
    var columns:any = document.getElementsByClassName("excludeColumn");
    var but:any = document.getElementById("excludeAllColumns")
    var exclude: Boolean;
    if(but.checked) {
      exclude = true;
      for(var i = 0; i < columns.length; i++) {
        columns[i].checked = "checked";
      }
      this.allColumnsExcluded = true;
    } else {
      exclude = false;
      for(var i = 0; i < columns.length; i++) {
        columns[i].checked = false;
      }
      this.allColumnsExcluded = false;
    }

    var json = {
      'id': this.selectedConnector.id,
      'table': this.idTableSelected,
      'exclude': exclude
    };
    this.conService.excludeAllColumns(json);

  }

  addImportantLink() {
    this.addLinkSubmitted = true;
    if(this.addImportantLinkFrom.invalid) {
      return;
    }
    var er = document.getElementById("infoLinkField");
    for(let i = 0; i < this.importantLinks.length; i++) {
      if(this.addImportantLinkFrom.value.displayName == this.importantLinks[i].name) {
        //Show error message
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(true, "A link with this name already exists", "ERROR");
        return;
      } else if (this.addImportantLinkFrom.value.addLink == this.importantLinks[i].link) {
        //Show error message
        er.style.marginTop = "2%";
        er.innerHTML = this.messages(true, "This link already exists", "ERROR");
        return;
      }
    }

    var json = {
      'link': this.addImportantLinkFrom.value.addLink,
      'name': this.addImportantLinkFrom.value.displayName
    };
    var er = document.getElementById("infoLinkField");
    this.homeService.addLink(json).then( async ()=>{
      //Show info message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Added as important link", "INFORMATION")
      this.importantLinks = await this.homeService.getLinks();

    }).catch((err)=>{
      //Show error message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Not added as important link", "ERROR");
    });
  }

  /**
   * Check if all the tales are excluded by checking if the checkboxes are checked
   */
  areAllTableExcludedCheckbox(): boolean {
    for(let i = 0; i < this.tableNames.length; i++) {
      var check = document.getElementById("checkbox-" + this.tableNames[i]) as HTMLInputElement;
      if(check.checked == false) {
        return false;
      }
    }
    return true;
  }

  areAllTablesExcluded(): boolean {
    for(let i = 0; i < this.tableNames.length; i++) {
        if(this.selectedConnector.config[this.tableNames[i]].exclude == false) {
          return false
        }
    }

    return true;
  }


  /**
   * Check if all the columns are excluded by checking the config
   */
  checkIfAllColumnExcluded():boolean {
    for(let i = 0; i< this.columnNames.length; i++) {
      if(this.selectedConnector.config[this.idTableSelected] == undefined) {
        return false;
      } else if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]] == undefined) {
        return false;
      } else if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]].exclude == undefined)  {
        return false;
      } else if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]].exclude == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if all the columns are excluded by checking if the checkboxes are checked
   */
  checkIfAllColumnExcludedCheckbox():boolean {
    for(let i = 0; i < this.columnNames.length; i++) {
      var check = document.getElementById("checkbox-" + this.columnNames[i]) as HTMLInputElement;
      if(check.checked == false) {
        return false;
      }
    }
    return true;
  }


  /**
   * Handle the delete event for an important link
   *
   * @param name the name of the important link that should be removed
   */
  removeImportantLink(name:string) {
    var json = {
      "name": name
    };
    var er = document.getElementById("infoLinkField");
    this.homeService.removeLink(json).then(async ()=>{
      //Show info message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Important link successfully removed", "INFORMATION");
          this.importantLinks = await this.homeService.getLinks();
    }, (err)=>{
      //Show the error message
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Link not removed", "ERROR");
    });
  }

 /* delSQL(name: string) {
    var json = {
      'name': name
    };
    var er = document.getElementById("infoField");
    this.conService.deleteSQL(json).then(async ()=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(false, "Successfully removed", "INFORMATION");
      this.idTableSelected = "";
      await this.reload(true);
      //this.queryName = "Lukas was here"
    }, (err)=>{
      er.style.marginTop = "2%";
      er.innerHTML = this.messages(true, "Not removed", "ERROR");
    })
  }*/


  messages(isError:boolean, text: string, title: string):string {
    var erg = "";
    if(isError) {
      erg =  `<div class="card card-custom">
                <div class="card-header" style="background-color: #F56565; color: white">${title}</div>
                  <div class="card-body" style="background-color: #FFF5F5; color: #355376">
                    <p>${text}</p>
                  </div>
                </div>
              </div>`;
    } else {
      erg =  `<div class="card card-custom">
                <div class="card-header" style="background-color: #38B2AC; color: white">INFORMATION</div>
                  <div class="card-body" style="background-color: #E6FFFA; color: #234E52">
                    <p>${text}</p>
                  </div>
                </div>
              </div>`;
    }
    return erg;
  }

  /**
   * Update the a query
   */
  updateSQL() {

    var json = {
      'id': this.selectedConnector.id,
      'sql': this.sqlForm.value.sqlQuery,
      'sqlName': this.idTableSelected,
      "newName": this.sqlForm.value.collectionId
    };  

    var errorText = document.getElementById('sqlError');
    this.conService.updateSQL(json).then(()=>{
      // Show success message
      errorText.innerHTML = this.messages(false, "SQL updated successful", "INFORMATION");
      this.reload(false);
      this.idTableSelected = this.sqlForm.value.collectionId;
    }).catch((err)=>{
      // Show error message
      errorText.innerHTML = this.messages(true, "SQL not updated successful", "INFORMATION");
    })
    
  }

  /**
   * Delete a sql view
   */
  deleteSQL() {
    var errorText = document.getElementById('sqlError');
    this.conService.delSQL({"name": this.idTableSelected}).then(async ()=>{
        // Show success message
        errorText.innerHTML = this.messages(false, "SQL deleted successful", "INFORMATION");
        await this.reload(false);
        this.idTableSelected = "";
        this.tableSelect = false;
        this.columnNames = [];
        this.queryName = "";
        this.query = "";
        //this.connectors = await this.conService.getConnector();

      }).catch((err)=>{
        // Show error message
        errorText.innerHTML = this.messages(true, "SQL not deleted successful", "INFORMATION");
      })
  }

  
}

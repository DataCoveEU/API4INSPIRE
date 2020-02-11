import { Component, OnInit } from '@angular/core';

import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConnectorService } from '../connector.service';
import { SqlService } from '../sql.service';
import { FeatureService } from '../feature.service';
import { HomeService } from '../home.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  //The table names that will be displayed
  tableNames = [];

  //The columnnames that will be displayed
  columnNames = [];
  //columnConfigNames = [];

  //The connectors loaded from the config file
  connectors: any = [];

  //The connector which is selected
  selectedConnector: any;

  //The important links from the config file
  importantLinks: any = ["Lukas", "Tobias", "Kathi", "Klaus"];

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

  addImportantLinkFrom: FormGroup;
  addLinkSubmitted: boolean = false;

  sqlSucess: boolean = true;

  checkedTable:boolean = false;
  checkedColumn: boolean = false;

  errorField: boolean = false;

  geoColumn: string = "";
  idColumn: string = "";

  constructor(  private formBuilder: FormBuilder, 
                private conService: ConnectorService, 
                private featureService: FeatureService, 
                private sqlService: SqlService,
                private homeSerivce: HomeService) { }

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

    //Load all the connectors from the config
    this.connectors = await this.conService.getConnector();

    if(this.connectors.length == 0){
      this.connectors = [{id: "No Connectors"}];
    }

    var select = document.getElementById("selectField") as HTMLSelectElement;
    var index = select.selectedIndex;

    if(index == -1){index = 0}

      this.selectedConnector = this.connectors[index];
      //Load the table names from the selected connector
      this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });

      //Event when another conneector in the dropdown is selected
      select.onchange = async (event: any)=>{
        var select = document.getElementById("selectField") as HTMLSelectElement;
        this.selectedConnector = this.connectors[select.selectedIndex];

        this.tableNames = await this.conService.getTables({'id': this.selectedConnector.id });
        this.tableSelect = false;
        this.checkIfAllTableExcluded();
      }

      this.checkIfAllTableExcluded();
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
    
    if(this.selectedConnector.config[name] == undefined) {

    } else if(this.selectedConnector.config[name].geoCol == undefined) {

    } else {
      this.geoColumn = this.selectedConnector.config[name].geoCol
    }

    if(this.selectedConnector.config[name] == undefined) {

    } else if(this.selectedConnector.config[name].idCol == undefined) {
      this.idColumn = this.selectedConnector.config[name].idCol 
    }
    this.checkIfAllColumnExcluded();
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

    //The unique names have to be on all of the databases
    for(let i = 0;  i < this.connectors.length; i++) {
      var con = this.connectors[i];
      var tab = await this.conService.getTables({'id': con.id });
      //The unique names have to be on all tables
      for(let j = 0; j < tab.length; j++) {
        if(con.config[tab[j]] == undefined) {
          console.log("undefined")
        } else if(con.config[tab[j]].alias == undefined) {
          console.log("undefined")
        } else if(con.config[tab[j]].alias == this.renameTableForm.value.tableName) {
          //alert("This name is already assigned to a table");
          var er = document.getElementById("errorField");
          er.innerHTML = "ERROR: This name is already assigned to a table";
          this.errorField = true;
          return;
        }
      }
    }

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

    for(let i = 0; i < this.columnNames.length; i++) {
      if(this.selectedConnector.config[this.idTableSelected] == undefined) {
        console.log("undefined")
      } else {
        if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]] == undefined) {
          console.log("undefined v2")
        } else {
          if(this.selectedConnector.config[this.idTableSelected].map[this.columnNames[i]].alias == this.renameColumnForm.value.columnName) {
            alert("This name is already assigned to a column");
            return;
          }
        }
      }
    }


/*
    for(let i = 0; i < this.connectors.length; i++) {
      var con = this.connectors[i];
      var tabs = await this.conService.getTables({'id': con.id });
      for(let j = 0; j < tabs.length; j++) {
        var tab = tabs[j];
        var cols = await this.conService.getColumn({'id': this.selectedConnector.id, 'table':''+tab});
        for(let h = 0; h < cols.length; h++) {
          var col = cols[h];
          if(con.config[tab] == undefined) {
            console.log("undefined v1")
          } else if(con.config[tab].map[col] == undefined) {
            console.log("undefined v2")
          } else if(con.config[tab].map[col].alias == undefined) {
            console.log("undefined v3");
          } else if(con.config[tab].map[col].alias == this.renameColumnForm.value.columnName) {
            var er = document.getElementById("errorField");
            er.innerHTML = "ERROR: This name is already assigned to a column";
            return;
          }
        }        
      }
    }

*/
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
      'collectionName': this.sqlForm.value.collectionId,
      'check':check
    };

    this.sqlService.executeSQL(json).then(
      async ()=>{
        alert("SQL executed successfully")
      }
    ).catch((err)=>{
      this.sqlSucess = false;
      var errorText = document.getElementById('sqlError');
      errorText.innerHTML = err;
      alert("Not executed successfully")
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

    await this.conService.excludeTable(json);
    this.checkIfAllTableExcluded();
  }

  excludeColumn(colName: string) {
    var cb = document.getElementById("checkbox-" + colName) as HTMLInputElement;
    console.log(cb);
    var checked: boolean = cb.checked;
    var bool: boolean = false;
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
    this.conService.excludeColumn(json);
    this.checkIfAllColumnExcluded();
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
    
    this.idColumn = this.idColumnSelected;
    this.featureService.setAsId(json);
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
    this.geoColumn = this.idColumnSelected;
    this.featureService.setAsGeometry(json);
  }

  /**
   * Handle the click event when all tables should be included or excluded
   */
  async excludeAllTables() {
    var tables:any = document.getElementsByClassName("excludeTable");
    var exlcudeAll:any = document.getElementById("exludeAllTables");
    var exclude: Boolean;
    if(exlcudeAll.checked) {
      // After clicking the checkbox is checked
      // so all og the tables will be exluded
      exclude = true;
      for(var i = 0; i < tables.length; i++) {
        tables[i].checked = "checked";
      }
    } else {
      // After clicking the checkbox is not checked
      // so all of the tables will be included
      exclude = false;
      for(var i = 0; i < tables.length; i++) {
        tables[i].checked = false;
      }
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
    } else {
      exclude = false;
      for(var i = 0; i < columns.length; i++) {
        columns[i].checked = false;
      }
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
    
    var json = {
      'link': this.addImportantLinkFrom.value.addLink,
      'name': this.addImportantLinkFrom.value.displayName
    };

    this.homeSerivce.addLink(json);
  }

  checkIfAllTableExcluded() {
    var allExclude = true;

    for(let i = 0; i < this.tableNames.length; i++) {
     var check = document.getElementById("checkbox-" + this.tableNames[i]) as HTMLInputElement; 
     if(check.checked) {

     } else {
       allExclude = false;
     }
    }

    var check = document.getElementById("exludeAllTables") as HTMLInputElement;
    if(allExclude) {
      check.checked = true;
    } else {
      check.checked = false;
    }
  }

  checkIfAllColumnExcluded() {
    var allExclude = true;
    for(let i = 0; i < this.columnNames.length; i++) {
      var check = document.getElementById("checkbox-" + this.columnNames[i]) as HTMLInputElement;
      if(check.checked) {

      } else {
        allExclude = false;
      }
    }
    
    var checkbox = document.getElementById("excludeAllColumns") as HTMLInputElement;
    if(allExclude) {
      checkbox.checked = true;
    } else {
      checkbox.checked = false;
    }
  }

}
 
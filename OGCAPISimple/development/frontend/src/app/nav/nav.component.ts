/*
    The OGC API Simple provides enviromental data
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
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.scss']
})
export class NavComponent implements OnInit {

  loggedIn: boolean = true;
  isDashboard: boolean = false;
  isProperties: boolean = false;
  isLogin: boolean = false;


  constructor(private router:Router, public auth: AuthService) { }

  ngOnInit() {
    var url = this.router.url;
    if(url == "/") {
      //If you are on the landing page --> the "Home" link is bold
      var home = document.getElementById("home");
      home.style.fontWeight = "bold";
    } else if (url == "/dashboard") {
      //If the admin is on the dashboard
      if(this.auth.loggedIn() == false) {
        this.router.navigate(['/']);
      } 
      //AND you are logged in --> the "Dashboard" link is bold
      this.isDashboard = true;
    } else if (url == "/properties") {
      //If you are on the properties page
      if(this.auth.loggedIn() == false) {
        this.router.navigate(['/']);
      } 
      //AND you are logged in --> the "Properties" link is bold
      this.isProperties = true;
    } else if (url == "/login") {
      //If you are on the login page --> the "Login" link is bold
      this.isLogin = true;
    } 
  }


  logout() {
    this.auth.logout();
  }
}
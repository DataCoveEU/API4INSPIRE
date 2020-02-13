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
    } else if(url == "/imprint") {
      //If you are on the imprint page --> the "Imprint" link is bold
      var imp = document.getElementById("imp");
      imp.style.fontWeight = "bold";
    }
  }


  logout() {
    this.auth.logout();
  }
}
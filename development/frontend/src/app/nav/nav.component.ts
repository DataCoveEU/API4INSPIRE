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

  isLoggedIn:boolean = false;

  constructor(private router:Router, private auth: AuthService) { }

  ngOnInit() {
    var url = this.router.url;
    if(url == "/") {
      var home = document.getElementById("home");
      home.style.fontWeight = "bold";
    } else if (url == "/dashboard") {
      this.isDashboard = true;
    } else if (url == "/properties") {
      this.isProperties = true;
    } else if (url == "/login") {
      this.isLogin = true;
    } else if(url == "/imprint") {
      var imp = document.getElementById("imp");
      imp.style.fontWeight = "bold";
    }
    
  }


  logout() {
    this.auth.logout();
  }

}

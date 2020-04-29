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
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { async } from '@angular/core/testing';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  submitted: boolean = false;
  success: boolean = false;

  loginNotSuccessful: boolean = false;


  constructor(private formBuilder: FormBuilder,public authservice: AuthService, private router: Router) { }

  ngOnInit() {
    //Init the login form
    this.loginForm = this.formBuilder.group({
      uname: ['', Validators.required],
      pwd: ['', Validators.required]
    });
  }

  /**
   * Handle the submit event when the user logs in and submits
   */
  login() {
    this.submitted = true;
    this.authservice.login(this.loginForm.value.uname,this.loginForm.value.pwd).then(
      async ()=>{
        this.loginNotSuccessful = false;
      }
    ).catch(()=>{
      this.loginNotSuccessful = true;
    });

  }

}

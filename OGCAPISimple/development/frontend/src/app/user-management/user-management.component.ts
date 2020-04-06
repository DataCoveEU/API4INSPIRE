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
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {

  changePwdForm: FormGroup;
  submit: boolean = false;
  notEqual: boolean = false;

  constructor(private formBuilder: FormBuilder, private auth: AuthService) { }

  ngOnInit() {
    //Init the form to change the password
    this.changePwdForm = this.formBuilder.group({
      oldPwd: ['', Validators.required],
      newPwd: ['', Validators.required],
      reNewPwd: ['', Validators.required]
    });
  }

  /**
   * Handle the event when the new password is submitted
   */
  renamePwd() {
    this.submit = true;
    if(this.changePwdForm.invalid) {
      return;
    }

    var old = this.changePwdForm.value.oldPwd;
    var newPwd = this.changePwdForm.value.newPwd;
    var reNewPwd = this.changePwdForm.value.reNewPwd;

    if(newPwd != reNewPwd) {
      this.notEqual = true;
      return;
    }
    this.auth.changePwd({'pwd':newPwd});
  }
}

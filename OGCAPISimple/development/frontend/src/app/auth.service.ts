import {Injectable} from '@angular/core';
import { HttpClient} from '@angular/common/http';

import {Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  loggedin: boolean = false;

  constructor(private httpClient: HttpClient, private router: Router, public jwtHelper: JwtHelperService) {}

  /**
   * 
   * @param username the username of the user who wants to login
   * @param password the password of the user who wants to login
   * 
   * Service to call the login api path
   */
    login(username: string, password: string) {
      return new Promise((resolve, reject)=>{
        this.httpClient.post('/ogcapisimple/authenticate',{
          username,
          password
        },{responseType: 'json'})
            .subscribe(
              (res: any) => {
                  localStorage.setItem('access_token', res.token);
                  this.router.navigateByUrl('/dashboard');
                  this.loggedin = true;
                  resolve(res);
            }, (err) => {
                reject(err);
            }
            );
      })
    
  }

  /**
   * Log the user out who is logged in
   */
  logout() {
    localStorage.removeItem('access_token');
    this.router.navigateByUrl('login');
  }
  
  /**
   * Check if there is a user logged in
   */
  loggedIn(): boolean {
    return localStorage.getItem('access_token') !==  null && !this.jwtHelper.isTokenExpired();
  }

  /**
   * 
   * @param json object with the details for the new password
   *  pwd --> new admin pwd
   * Change the admin password
   */
  changePwd(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/ogcapisimple/api/changePwd', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    });
  }
}
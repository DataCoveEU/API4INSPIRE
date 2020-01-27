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

    login(username: string, password: string) {
    this.httpClient.post('/authenticate',{
      username,
      password
    },{responseType: 'json'})
        .subscribe(
          (res: any) => {
              localStorage.setItem('access_token', res.token);
              this.router.navigateByUrl('/dashboard');
              this.loggedin = true;
        }, (err) => {
            console.log(err);
        }
        );
  }

  logout() {
    localStorage.removeItem('access_token');
    this.router.navigateByUrl('login');
  }
  
  loggedIn(): boolean {
    return localStorage.getItem('access_token') !==  null && !this.jwtHelper.isTokenExpired();
  }

  changePwd(json:object) {
    return new Promise((resolve, reject)=>{
      this.httpClient.post('/api/changePwd', json, {
        responseType: 'text'
      }).subscribe((res)=>{
        resolve(res);
      }, (err)=>{
        reject(err);
      })
    });
  }
}
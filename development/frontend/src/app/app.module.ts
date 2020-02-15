import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { ErrorComponent } from './error/error.component';
import { NavComponent } from './nav/nav.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PropertiesComponent } from './properties/properties.component';
import { AddConnectorComponent } from './add-connector/add-connector.component';
import { ImprintComponent } from './imprint/imprint.component';
import { JwtModule } from '@auth0/angular-jwt';
import { AuthService } from './auth.service';
import { HttpClientModule } from '@angular/common/http';

import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { JwtInterceptor } from './jwt.interceptor';
import { UserManagementComponent } from './user-management/user-management.component';
import {APP_BASE_HREF} from '@angular/common';
import { CollectionsComponent } from './collections/collections.component';

export function tokenGetter(){return localStorage.getItem('access_token')}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LandingPageComponent,
    ErrorComponent,
    NavComponent,
    DashboardComponent,
    PropertiesComponent,
    AddConnectorComponent,
    ImprintComponent,
    UserManagementComponent,
    CollectionsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    JwtModule.forRoot({
      config:{
        tokenGetter:  tokenGetter,
        whitelistedDomains: ['*'],
        blacklistedRoutes: ['/']
      }
    })
  ],
  providers: [AuthService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
    ],
  bootstrap: [AppComponent]
})
export class AppModule { }

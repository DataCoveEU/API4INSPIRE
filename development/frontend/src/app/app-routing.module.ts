/*
 * Created on Wed Feb 26 2020
 *
 * Copyright (c) 2020 - Lukas Gäbler
 */
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { ErrorComponent } from './error/error.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PropertiesComponent } from './properties/properties.component';
import { AddConnectorComponent } from './add-connector/add-connector.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { CollectionsComponent } from './collections/collections.component';
import { ItemsComponent } from './items/items.component';


const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'properties', component: PropertiesComponent },
  { path: 'collections', component: CollectionsComponent},
  { path: 'connector', component: AddConnectorComponent },
  { path: 'connector', component: AddConnectorComponent },
  { path: 'collections/:collection/items', component: ItemsComponent},
  { path: 'user-management', component: UserManagementComponent },
  { path: '', component: LandingPageComponent },
  { path: 'error', component: ErrorComponent },
  { path: '**', redirectTo: '/error', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

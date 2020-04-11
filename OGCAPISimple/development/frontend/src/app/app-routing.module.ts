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

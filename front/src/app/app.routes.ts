import { Routes } from '@angular/router';

import { Dashboard } from './features/dashboard/dashboard';
import { Home } from './features/home/home';

export const routes: Routes = [
	{
		path: '',
		component: Home
	},
	{
		path: 'dashboard',
		component: Dashboard,
		canActivate: [() => import('./core/auth.guard').then(m => m.authGuard)]
	},
	{
		path: 'register',
		loadComponent: () => import('./features/usuarios/register').then(m => m.Register)
	},
	{
		path: 'login',
		loadComponent: () => import('./features/usuarios/login').then(m => m.Login)
	}
];

import { Routes } from '@angular/router';

import { Dashboard } from './features/dashboard/dashboard';
import { Home } from './features/home/home';
import { EscaneamentoComponent } from './features/escaneamento/escaneamento';
import { GaleriaComponent } from './features/galeria/galeria';

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
		path: 'escaneamento',
		component: EscaneamentoComponent,
		canActivate: [() => import('./core/auth.guard').then(m => m.authGuard)]
	},
	{
		path: 'acervo',
		component: GaleriaComponent
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

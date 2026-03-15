import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: '',
		loadComponent: () => import('./features/home/home').then(m => m.Home)
	},
	{
		path: 'dashboard',
		loadComponent: () => import('./features/dashboard/dashboard').then(m => m.Dashboard),
		canActivate: [() => import('./core/auth.guard').then(m => m.authGuard)]
	},
	{
		path: 'escaneamento',
		loadComponent: () => import('./features/escaneamento/escaneamento').then(m => m.EscaneamentoComponent),
		canActivate: [() => import('./core/auth.guard').then(m => m.authGuard)]
	},
	{
		path: 'acervo',
		loadComponent: () => import('./features/galeria/galeria').then(m => m.GaleriaComponent)
	},
	{
		path: 'acervo/:id',
		loadComponent: () => import('./features/galeria/documento-detalhe').then(m => m.DocumentoDetalheComponent)
	},
	{
		path: 'register',
		loadComponent: () => import('./features/usuarios/register').then(m => m.Register)
	},
	{
		path: 'login',
		loadComponent: () => import('./features/usuarios/login').then(m => m.Login)
	},
	{
		path: 'forgot-password',
		loadComponent: () => import('./features/usuarios/forgot-password').then(m => m.ForgotPassword)
	},
	{
		path: 'reset-password',
		loadComponent: () => import('./features/usuarios/reset-password').then(m => m.ResetPassword)
	},
	{
		path: 'admin/usuarios',
		loadComponent: () => import('./features/admin/user-management').then(m => m.UserManagementComponent),
		canActivate: [() => import('./core/auth.guard').then(m => m.authGuard)]
	}
];

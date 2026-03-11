

import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkWithHref, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../core/auth.service';
import { UserService } from '../../core/user.service';
import { ThemeToggleComponent } from '../theme-toggle/theme-toggle.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkWithHref, ThemeToggleComponent, CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class Navbar implements OnInit {
  isMenuOpen = false;
  perfil: string | undefined;

  constructor(public router: Router, public auth: AuthService, private userService: UserService) {}

  ngOnInit(): void {
    if (this.isLoggedIn()) {
      this.loadUserInfo();
    }

    // Escuta mudanças de rota para atualizar info de perfil após login
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      if (this.isLoggedIn() && !this.perfil) {
        this.loadUserInfo();
      } else if (!this.isLoggedIn()) {
        this.perfil = undefined;
      }
    });
  }

  loadUserInfo() {
    this.userService.getMe().subscribe({
      next: (user) => {
        this.perfil = user.perfil;
      },
      error: () => {
        this.perfil = undefined;
      }
    });
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }


  goTo(path: string) {
    this.router.navigate([path]);
  }

  isLoggedIn(): boolean {
    return !!this.auth.getToken();
  }

  logout() {
    this.auth.logout();
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  }
}
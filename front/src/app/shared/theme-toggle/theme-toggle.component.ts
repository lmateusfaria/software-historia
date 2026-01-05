import { Component, OnInit, OnDestroy } from '@angular/core';
import { ThemeService } from '../../core/theme.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [],
  template: `
    <button 
      (click)="toggleTheme()" 
      class="btn-theme-toggle"
      [attr.aria-label]="isDark ? 'Alternar para tema claro' : 'Alternar para tema escuro'"
      title="{{ isDark ? 'Tema claro' : 'Tema escuro' }}"
    >
      <!-- Ícone de sol (tema claro) -->
      @if (!isDark) {
        <svg 
          class="w-5 h-5 text-text-light dark:text-text-dark transition-colors" 
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path 
            stroke-linecap="round" 
            stroke-linejoin="round" 
            stroke-width="2" 
            d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
          />
        </svg>
      }
      
      <!-- Ícone de lua (tema escuro) -->
      @if (isDark) {
        <svg 
          class="w-5 h-5 text-text-light dark:text-text-dark transition-colors" 
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path 
            stroke-linecap="round" 
            stroke-linejoin="round" 
            stroke-width="2" 
            d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
          />
        </svg>
      }
    </button>
  `,
  styles: [`
    :host {
      display: inline-block;
    }
  `]
})
export class ThemeToggleComponent implements OnInit, OnDestroy {
  private subscription?: Subscription;
  isDark = false;

  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    // Define o estado inicial
    this.isDark = this.themeService.isDarkTheme();
    
    // Escuta mudanças de tema
    this.subscription = this.themeService.theme$.subscribe((theme: any) => {
      this.isDark = theme === 'dark';
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
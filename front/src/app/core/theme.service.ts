import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable } from 'rxjs';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'app-theme';
  private themeSubject = new BehaviorSubject<Theme>(this.getInitialTheme());
  
  theme$: Observable<Theme> = this.themeSubject;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    // Só aplica o tema se estivermos no browser
    if (isPlatformBrowser(this.platformId)) {
      this.applyTheme(this.themeSubject.value);
    }
  }

  /**
   * Verifica se está executando no browser
   */
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  /**
   * Obtém o tema inicial baseado na preferência salva ou sistema
   */
  private getInitialTheme(): Theme {
    // Se não está no browser, retorna tema padrão
    if (!this.isBrowser()) {
      return 'dark';
    }

    // Primeiro verifica se há tema salvo no localStorage
    try {
      if (typeof Storage !== 'undefined' && localStorage) {
        const savedTheme = localStorage.getItem(this.THEME_KEY) as Theme;
        if (savedTheme && (savedTheme === 'light' || savedTheme === 'dark')) {
          return savedTheme;
        }
      }
    } catch (error) {
      console.warn('Erro ao acessar localStorage:', error);
    }

    // Se não há tema salvo, usa a preferência do sistema
    try {
      if (typeof window !== 'undefined' && window.matchMedia) {
        const darkMediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        if (darkMediaQuery.matches) {
          return 'dark';
        }
      }
    } catch (error) {
      console.warn('Erro ao detectar preferência do sistema:', error);
    }

    // Fallback para tema escuro
    return 'dark';
  }

  /**
   * Aplica o tema ao documento
   */
  private applyTheme(theme: Theme): void {
    if (!this.isBrowser()) {
      return;
    }

    try {
      if (typeof document !== 'undefined' && document.documentElement) {
        const htmlElement = document.documentElement;
        
        if (theme === 'dark') {
          htmlElement.classList.add('dark');
        } else {
          htmlElement.classList.remove('dark');
        }
      }
    } catch (error) {
      console.warn('Erro ao aplicar tema:', error);
    }
  }

  /**
   * Salva o tema no localStorage (apenas no browser)
   */
  private saveTheme(theme: Theme): void {
    if (!this.isBrowser()) {
      return;
    }

    try {
      if (typeof Storage !== 'undefined' && localStorage) {
        localStorage.setItem(this.THEME_KEY, theme);
      }
    } catch (error) {
      console.warn('Erro ao salvar tema no localStorage:', error);
    }
  }

  /**
   * Obtém o tema atual
   */
  getCurrentTheme(): Theme {
    return this.themeSubject.value;
  }

  /**
   * Define um novo tema
   */
  setTheme(theme: Theme): void {
    this.themeSubject.next(theme);
    this.applyTheme(theme);
    this.saveTheme(theme);
  }

  /**
   * Alterna entre tema claro e escuro
   */
  toggleTheme(): void {
    const currentTheme = this.getCurrentTheme();
    const newTheme: Theme = currentTheme === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  /**
   * Verifica se o tema atual é escuro
   */
  isDarkTheme(): boolean {
    return this.getCurrentTheme() === 'dark';
  }
}
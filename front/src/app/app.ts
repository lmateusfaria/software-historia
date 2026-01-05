import { booleanAttribute, Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/navbar/navbar';
import { ToastContainer } from './shared/toast/toast';
import { ThemeService } from './core/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Navbar, ToastContainer],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontEndAngular');
  protected isMenuOpen = booleanAttribute('false');
  protected isOpen = booleanAttribute('false');
  
  // Injeta o ThemeService para inicializar o tema
  private themeService = inject(ThemeService);
}

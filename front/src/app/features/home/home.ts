import { Component, OnInit, AfterViewInit, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit, AfterViewInit {

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  private isBrowser(): boolean {
    return typeof window !== 'undefined' && typeof document !== 'undefined';
  }

  ngOnInit(): void {
    // Inicialização básica
  }

  ngAfterViewInit(): void {
    if (this.isBrowser()) {
      this.initializeAnimations();
      this.startCounters();
      this.observeElements();
      this.optimizeAnimations();
    }
  }

  private initializeAnimations(): void {
    // Adiciona animações de entrada suaves
    const elements = document.querySelectorAll('.fade-in, .fade-in-delay, .fade-in-delay-2');
    elements.forEach((element, index) => {
      setTimeout(() => {
        element.classList.add('animate-in');
      }, index * 100);
    });
  }

  private startCounters(): void {
    const counters = document.querySelectorAll('.counter');
    
    counters.forEach((counter) => {
      const target = parseInt(counter.getAttribute('data-target') || '0');
      const duration = 2000; // 2 segundos
      const step = target / (duration / 16); // 60fps
      let current = 0;

      const updateCounter = () => {
        current += step;
        if (current < target) {
          counter.textContent = Math.floor(current).toString();
          requestAnimationFrame(updateCounter);
        } else {
          counter.textContent = target.toString();
        }
      };

      // Inicia o contador com um pequeno delay
      setTimeout(() => {
        updateCounter();
      }, 500);
    });
  }

  private observeElements(): void {
    // Intersection Observer para animações durante o scroll
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('animate-in');
        }
      });
    }, {
      threshold: 0.1,
      rootMargin: '0px 0px -50px 0px'
    });

    // Observa os cards de features
    const featureCards = document.querySelectorAll('.feature-card');
    featureCards.forEach((card) => {
      observer.observe(card);
    });

    // Observa os itens de estatísticas
    const statItems = document.querySelectorAll('.stat-item');
    statItems.forEach((item) => {
      observer.observe(item);
    });
  }

  private optimizeAnimations(): void {
    if (this.isBrowser()) {
      // Detecta se o usuário prefere movimento reduzido
      const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      
      if (prefersReducedMotion) {
        // Desabilita animações para melhor acessibilidade
        document.body.classList.add('reduced-motion');
      }

      // Pausa animações quando a página não está visível
      document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
          document.body.classList.add('paused-animations');
        } else {
          document.body.classList.remove('paused-animations');
        }
      });
    }
  }

  // Método para navegar para diferentes seções
  goTo(path: string): void {
    this.router.navigate([path]);
  }

  // Método para navegar para o registro
  onGetStarted(): void {
    this.router.navigate(['/register']);
  }

  // Método para navegar para o dashboard (se logado) ou login
  onExploreFeatures(): void {
    this.router.navigate(['/login']);
  }

  // Método para smooth scroll (caso implemente âncoras na página)
  scrollToSection(sectionId: string): void {
    if (this.isBrowser()) {
      const element = document.getElementById(sectionId);
      if (element) {
        element.scrollIntoView({ 
          behavior: 'smooth',
          block: 'start'
        });
      }
    }
  }

  // Método para adicionar efeito de ripple nos botões
  addRippleEffect(event: MouseEvent): void {
    if (!this.isBrowser()) return;

    const button = event.currentTarget as HTMLElement;
    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;

    const ripple = document.createElement('div');
    ripple.style.cssText = `
      position: absolute;
      width: ${size}px;
      height: ${size}px;
      left: ${x}px;
      top: ${y}px;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      transform: scale(0);
      animation: ripple 0.6s ease-out;
      pointer-events: none;
    `;

    button.style.position = 'relative';
    button.style.overflow = 'hidden';
    button.appendChild(ripple);

    // Remove o ripple após a animação
    setTimeout(() => {
      if (ripple.parentNode) {
        ripple.parentNode.removeChild(ripple);
      }
    }, 600);
  }
}
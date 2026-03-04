import { Component, OnInit, AfterViewInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit, AfterViewInit, OnDestroy {

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void { }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.runHeroGSAP();
      this.observeScrollElements();
      this.runCounters();
    }
  }

  ngOnDestroy(): void { }

  private async runHeroGSAP(): Promise<void> {
    const gsap = (await import('gsap')).gsap;

    // Sequência de entrada do hero
    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
    tl.to('#hero-badge', { opacity: 1, y: 0, duration: 0.6, delay: 0.2 })
      .to('#hero-title', { opacity: 1, y: 0, duration: 0.7 }, '-=0.3')
      .to('#hero-sub', { opacity: 1, y: 0, duration: 0.6 }, '-=0.4')
      .to('#hero-ctas', { opacity: 1, y: 0, duration: 0.6 }, '-=0.4')
      .to('#hero-scroll', { opacity: 1, duration: 0.5 }, '-=0.2');

    // Configurar posição inicial para animação GSAP
    gsap.set(['#hero-badge', '#hero-title', '#hero-sub', '#hero-ctas'], { y: 30 });

    // Partículas flutuantes no canvas
    this.drawParticles();
  }

  private drawParticles(): void {
    const canvas = document.getElementById('hero-particles') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Tamanho responsivo
    const resize = () => {
      canvas.width = canvas.offsetWidth;
      canvas.height = canvas.offsetHeight;
    };
    resize();
    window.addEventListener('resize', resize);

    const particles: Array<{
      x: number; y: number; vx: number; vy: number;
      r: number; alpha: number; color: string;
    }> = [];

    const colors = ['#00A79D', '#F7941D', '#33BBAF', '#FAA84A'];
    for (let i = 0; i < 50; i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - 0.5) * 0.3,
        vy: (Math.random() - 0.5) * 0.3,
        r: Math.random() * 3 + 1,
        alpha: Math.random() * 0.4 + 0.1,
        color: colors[Math.floor(Math.random() * colors.length)]
      });
    }

    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      for (const p of particles) {
        p.x += p.vx;
        p.y += p.vy;
        if (p.x < 0) p.x = canvas.width;
        if (p.x > canvas.width) p.x = 0;
        if (p.y < 0) p.y = canvas.height;
        if (p.y > canvas.height) p.y = 0;

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = p.color + Math.floor(p.alpha * 255).toString(16).padStart(2, '0');
        ctx.fill();
      }
      requestAnimationFrame(animate);
    };
    animate();
  }

  private observeScrollElements(): void {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          (entry.target as HTMLElement).style.animation = 'fadeUpIn 0.7s ease-out both';
          (entry.target as HTMLElement).style.opacity = '1';
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -60px 0px' });

    document.querySelectorAll('.feat-animate, .stat-item, .cta-animate').forEach(el => {
      observer.observe(el);
    });
  }

  private runCounters(): void {
    const counterObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const el = entry.target as HTMLElement;
          const target = parseInt(el.getAttribute('data-target') || '0');
          let current = 0;
          const step = target / 60;
          const tick = () => {
            current += step;
            if (current < target) {
              el.textContent = Math.floor(current).toString();
              requestAnimationFrame(tick);
            } else {
              el.textContent = target.toString();
            }
          };
          tick();
          counterObserver.unobserve(entry.target);
        }
      });
    }, { threshold: 0.5 });

    document.querySelectorAll('.counter').forEach(el => counterObserver.observe(el));
  }

  onGetStarted(): void {
    this.router.navigate(['/register']);
  }

  onExploreFeatures(): void {
    this.router.navigate(['/login']);
  }

  scrollToSection(sectionId: string): void {
    if (isPlatformBrowser(this.platformId)) {
      document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth' });
    }
  }
}
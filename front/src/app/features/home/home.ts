import { Component, OnInit, AfterViewInit, OnDestroy, Inject, PLATFORM_ID, signal } from '@angular/core';
import { Router } from '@angular/router';
import { isPlatformBrowser, CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit, AfterViewInit, OnDestroy {

  // Sinal para controlar qual card está em foco (para o efeito de blur global)
  protected focusedCard = signal<number | null>(null);

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void { }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.runHeroGSAP();
      this.initFluidSimulation();
      this.observeScrollElements();
      this.runCounters();
    }
  }

  ngOnDestroy(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.removeEventListener('mousemove', this.onMouseMove);
    }
  }

  private async runHeroGSAP(): Promise<void> {
    const gsap = (await import('gsap')).gsap;

    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
    tl.to('#hero-badge', { opacity: 1, y: 0, duration: 0.6, delay: 0.2 })
      .to('#hero-title', { opacity: 1, y: 0, duration: 0.7 }, '-=0.3')
      .to('#hero-logo', { opacity: 1, y: 0, duration: 0.7, scale: 1 }, '-=0.5')
      .to('#hero-sub', { opacity: 1, y: 0, duration: 0.6 }, '-=0.4')
      .to('#hero-ctas', { opacity: 1, y: 0, duration: 0.6 }, '-=0.4')
      .to('#hero-scroll', { opacity: 1, duration: 0.5 }, '-=0.2');

    gsap.set(['#hero-badge', '#hero-title', '#hero-logo', '#hero-sub', '#hero-ctas'], { y: 30 });
  }
  private gl: WebGLRenderingContext | null = null;
  private program: WebGLProgram | null = null;
  private mouseX = 0;
  private mouseY = 0;
  private clickedParticles: { x: number, y: number }[] = [];

  private initFluidSimulation(): void {
    const canvas = document.getElementById('hero-particles') as HTMLCanvasElement;
    if (!canvas) return;

    this.gl = canvas.getContext('webgl', { antialias: true, alpha: true });
    if (!this.gl) {
      console.error('WebGL não suportado');
      return;
    }

    const resize = () => {
      const dpr = window.devicePixelRatio || 1;
      canvas.width = window.innerWidth * dpr;
      canvas.height = window.innerHeight * dpr;
      if (this.gl) this.gl.viewport(0, 0, canvas.width, canvas.height);
    };
    resize();
    window.addEventListener('resize', resize);
    window.addEventListener('mousemove', this.onMouseMove);
    
    const heroSection = document.getElementById('hero');
    if (heroSection) {
      heroSection.addEventListener('click', this.onCanvasClick);
    }

    const vsSource = `
      attribute vec2 a_position;
      void main() {
        gl_Position = vec4(a_position, 0, 1);
      }
    `;

    const fsSource = `
      precision mediump float;
      uniform vec2 u_resolution;
      uniform float u_time;
      uniform vec2 u_mouse;
      uniform float u_dpr;
      uniform vec2 u_clickedPoints[20];
      uniform int u_clickedCount;

      float noise(vec2 p) {
        return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
      }

      void main() {
        vec2 uv = gl_FragCoord.xy / u_resolution;
        float aspect = u_resolution.x / u_resolution.y;
        vec2 mouseUv = u_mouse / (u_resolution / u_dpr);
        mouseUv.y = 1.0 - mouseUv.y;
        vec2 uvAspect = vec2(uv.x * aspect, uv.y);
        vec2 mouseAspect = vec2(mouseUv.x * aspect, mouseUv.y);
        
        float dist = distance(uvAspect, mouseAspect);
        vec3 color1 = vec3(0.0, 0.65, 0.61); // UNIFEF Teal
        vec3 color2 = vec3(0.97, 0.58, 0.11); // UNIFEF Orange
        
        float strength = smoothstep(0.25 * aspect, 0.0, dist);
        vec3 finalColor = mix(vec3(0.01, 0.02, 0.02), color1, uv.y * 0.4);
        finalColor += color2 * strength * 0.4;
        
        float dTeal = distance(uvAspect, vec2(0.3 * aspect, 0.7));
        finalColor += color1 * smoothstep(0.5, 0.0, dTeal) * 0.2;

        // --- Partículas Lentas ---
        for (int i = 0; i < 36; i++) {
          float id = float(i);
          vec2 pBase = fract(vec2(noise(vec2(id, 111.1)) + sin(u_time * 0.05 + id) * 0.05, noise(vec2(id, 222.2)) + cos(u_time * 0.03 + id) * 0.05));
          vec2 pAspect = vec2(pBase.x * aspect, pBase.y);
          float mDist = distance(pAspect, mouseAspect);
          vec2 push = mDist < 0.2 ? normalize(pAspect - mouseAspect) * (0.2 - mDist) * 0.5 : vec2(0.0);
          float d = distance(uvAspect, pAspect + push);
          float size = 0.0008 + noise(vec2(id)) * 0.002;
          float pt = smoothstep(size, 0.0, d);
          float glow = smoothstep(0.3 * aspect, 0.0, distance(pAspect + push, mouseAspect));
          finalColor += pt * mix(color1, color2, glow) * (0.72 + glow * 0.48);
        }

        // --- Partículas por Clique (Maiores e Interativas) ---
        for (int i = 0; i < 20; i++) {
          if (i >= u_clickedCount) break;
          vec2 pBase = u_clickedPoints[i];
          vec2 pAspect = vec2(pBase.x * aspect, pBase.y);
          float mDist = distance(pAspect, mouseAspect);
          vec2 push = mDist < 0.15 ? normalize(pAspect - mouseAspect) * (0.15 - mDist) * 0.4 : vec2(0.0);
          float d = distance(uvAspect, pAspect + push);
          float size = 0.003; // 30%+ maior que as normais
          float pt = smoothstep(size, 0.0, d);
          finalColor += pt * color2 * 0.8;
        }

        gl_FragColor = vec4(finalColor, 1.0);
      }
    `;

    this.program = this.createProgram(vsSource, fsSource);
    if (!this.program) return;

    const positionBuffer = this.gl.createBuffer();
    this.gl.bindBuffer(this.gl.ARRAY_BUFFER, positionBuffer);
    this.gl.bufferData(this.gl.ARRAY_BUFFER, new Float32Array([-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1]), this.gl.STATIC_DRAW);

    const animate = (time: number) => {
      if (!this.gl || !this.program) return;
      this.gl.clear(this.gl.COLOR_BUFFER_BIT);
      this.gl.useProgram(this.program);

      const resLoc = this.gl.getUniformLocation(this.program, 'u_resolution');
      const timeLoc = this.gl.getUniformLocation(this.program, 'u_time');
      const mouseLoc = this.gl.getUniformLocation(this.program, 'u_mouse');
      const dprLoc = this.gl.getUniformLocation(this.program, 'u_dpr');
      const pointsLoc = this.gl.getUniformLocation(this.program, 'u_clickedPoints');
      const countLoc = this.gl.getUniformLocation(this.program, 'u_clickedCount');

      this.gl.uniform2f(resLoc, canvas.width, canvas.height);
      this.gl.uniform1f(timeLoc, time * 0.001);
      this.gl.uniform2f(mouseLoc, this.mouseX, this.mouseY);
      this.gl.uniform1f(dprLoc, window.devicePixelRatio || 1);
      
      const flatPoints = new Float32Array(40);
      this.clickedParticles.forEach((p, i) => {
        flatPoints[i * 2] = p.x;
        flatPoints[i * 2 + 1] = p.y;
      });
      this.gl.uniform2fv(pointsLoc, flatPoints);
      this.gl.uniform1i(countLoc, this.clickedParticles.length);

      const posLoc = this.gl.getAttribLocation(this.program, 'a_position');
      this.gl.enableVertexAttribArray(posLoc);
      this.gl.vertexAttribPointer(posLoc, 2, this.gl.FLOAT, false, 0, 0);

      this.gl.drawArrays(this.gl.TRIANGLES, 0, 6);
      requestAnimationFrame(animate);
    };
    requestAnimationFrame(animate);
  }

  private onMouseMove = (e: MouseEvent): void => {
    const canvas = document.getElementById('hero-particles') as HTMLCanvasElement;
    if (canvas) {
      const rect = canvas.getBoundingClientRect();
      this.mouseX = e.clientX - rect.left;
      this.mouseY = e.clientY - rect.top;
    }
  };

  private onCanvasClick = (e: MouseEvent): void => {
    const canvas = document.getElementById('hero-particles') as HTMLCanvasElement;
    if (canvas) {
      const rect = canvas.getBoundingClientRect();
      const x = (e.clientX - rect.left) / rect.width;
      const y = (e.clientY - rect.top) / rect.height;
      this.clickedParticles.push({ x, y: 1.0 - y });
      if (this.clickedParticles.length > 20) this.clickedParticles.shift();
    }
  };

  private createProgram(vs: string, fs: string): WebGLProgram | null {
    if (!this.gl) return null;
    const vShader = this.gl.createShader(this.gl.VERTEX_SHADER)!;
    this.gl.shaderSource(vShader, vs);
    this.gl.compileShader(vShader);
    const fShader = this.gl.createShader(this.gl.FRAGMENT_SHADER)!;
    this.gl.shaderSource(fShader, fs);
    this.gl.compileShader(fShader);
    const prog = this.gl.createProgram()!;
    this.gl.attachShader(prog, vShader);
    this.gl.attachShader(prog, fShader);
    this.gl.linkProgram(prog);
    return prog;
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
    this.router.navigate(['/login']);
  }

  onExploreFeatures(): void {
    this.router.navigate(['/register']);
  }

  scrollToSection(sectionId: string): void {
    if (isPlatformBrowser(this.platformId)) {
      document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth' });
    }
  }

  setFocusedCard(index: number | null): void {
    this.focusedCard.set(index);
  }

  getCardStyles(cardNumber: number, type: 'teal' | 'orange') {
    if (this.focusedCard() === cardNumber) {
      if (type === 'teal') {
        return {
          'box-shadow': '0 0 40px rgba(0, 167, 157, 0.6), inset 0 0 15px rgba(0, 167, 157, 0.1)',
          'border-color': 'rgba(0, 167, 157, 0.8)'
        };
      } else {
        return {
          'box-shadow': '0 0 40px rgba(247, 148, 29, 0.6), inset 0 0 15px rgba(247, 148, 29, 0.1)',
          'border-color': 'rgba(247, 148, 29, 0.8)'
        };
      }
    }
    return {};
  }
  
  getCardClasses(cardNumber: number) {
    const focused = this.focusedCard();
    if (focused === cardNumber) {
      return 'z-40 transform scale-110 -translate-y-2';
    } else if (focused !== null) {
      return 'blur-[5px] opacity-40 transform scale-95';
    }
    return '';
  }
}
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}', './src/index.html'],
  theme: {
    extend: {
      colors: {
        // Tema principal baseado em branco e preto
        primary: {
          DEFAULT: '#000000', // preto puro
          light: '#333333',   // preto claro
          dark: '#000000',    // preto escuro
          accent: '#ffffff',  // branco para contraste
          // Cores adaptáveis para texto
          text: {
            light: '#000000', // preto no tema claro
            dark: '#ffffff',  // branco no tema escuro
          },
        },
        // Cores semânticas para padronizar o projeto inteiro
        success: {
          DEFAULT: '#16a34a', // verde 600
          light: '#22c55e',   // verde 500
          dark: '#166534',    // verde 800
        },
        warning: {
          DEFAULT: '#f59e0b', // amber 500
          light: '#fbbf24',   // amber 400
          dark: '#b45309',    // amber 700
        },
        danger: {
          DEFAULT: '#dc2626', // red 600
          light: '#ef4444',   // red 500
          dark: '#991b1b',    // red 800
        },
        info: {
          DEFAULT: '#3b82f6', // blue 500
          light: '#60a5fa',   // blue 400
          dark: '#1e40af',    // blue 800
        },
        // Sistema de cores para tema claro/escuro
        background: {
          light: '#ffffff',   // branco puro (tema claro)
          dark: '#000000',    // preto puro (tema escuro)
        },
        surface: {
          light: '#f8f9fa',   // cinza muito claro (tema claro)
          dark: '#1a1a1a',    // cinza muito escuro (tema escuro)
        },
        text: {
          light: '#000000',   // preto (texto no tema claro)
          dark: '#ffffff',    // branco (texto no tema escuro)
          muted: {
            light: '#6b7280', // cinza médio (tema claro)
            dark: '#9ca3af',  // cinza claro (tema escuro)
          },
        },
        border: {
          light: '#e5e7eb',   // borda clara
          dark: '#374151',    // borda escura
        },
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      keyframes: {
        gradient: {
          "0%, 100%": { backgroundPosition: "0% 50%" },
          "50%": { backgroundPosition: "100% 50%" },
        },
      },
      animation: {
        gradient: "gradient 8s ease infinite",
      },
    },
  },

  plugins: [],
  darkMode: 'class'
};

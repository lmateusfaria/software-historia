/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}', './src/index.html'],
  theme: {
    extend: {
      colors: {
        // Paleta Oficial UNIFEF
        unifef: {
          teal: '#00A79D',
          'teal-dark': '#00877E',
          'teal-light': '#33BBAF',
          orange: '#F7941D',
          'orange-dark': '#D97A0E',
          'orange-light': '#FAA84A',
        },
        primary: {
          DEFAULT: '#00A79D',
          dark: '#00877E',
          light: '#33BBAF',
          accent: '#F7941D',
          text: {
            light: '#111827',
            dark: '#F9FAFB',
          },
        },
        success: { DEFAULT: '#16a34a', light: '#22c55e', dark: '#166534' },
        warning: { DEFAULT: '#f59e0b', light: '#fbbf24', dark: '#b45309' },
        danger: { DEFAULT: '#dc2626', light: '#ef4444', dark: '#991b1b' },
        info: { DEFAULT: '#3b82f6', light: '#60a5fa', dark: '#1e40af' },
        background: {
          light: '#F8FAF9',
          dark: '#0B1210',
        },
        surface: {
          light: '#FFFFFF',
          dark: '#121A19',
        },
        text: {
          light: '#111827',
          dark: '#F9FAFB',
          muted: {
            light: '#6B7280',
            dark: '#9CA3AF',
          },
        },
        border: {
          light: '#E5E7EB',
          dark: '#1F3330',
        },
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      keyframes: {
        gradient: {
          '0%, 100%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-20px)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% center' },
          '100%': { backgroundPosition: '200% center' },
        },
      },
      animation: {
        gradient: 'gradient 8s ease infinite',
        float: 'float 6s ease-in-out infinite',
        shimmer: 'shimmer 2.5s linear infinite',
      },
    },
  },
  plugins: [],
  darkMode: 'class',
};

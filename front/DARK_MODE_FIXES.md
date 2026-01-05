# Correção dos Textos no Modo Escuro

## Problema Identificado
Alguns textos estavam aparecendo em preto no modo escuro devido a:
1. `text-primary` usa preto (#000000) que fica invisível no fundo escuro
2. `text-text-muted` usa cor fixa cinza que não se adapta ao tema

## Soluções Implementadas

### 1. **Atualização do Tailwind Config**

#### Cores de Texto Adaptáveis do Primary
```javascript
primary: {
  // ... outras cores
  text: {
    light: '#000000', // preto no tema claro
    dark: '#ffffff',  // branco no tema escuro
  },
}
```

#### Cores Muted Adaptáveis
```javascript
text: {
  light: '#000000',   // preto (texto no tema claro)
  dark: '#ffffff',    // branco (texto no tema escuro)
  muted: {
    light: '#6b7280', // cinza médio (tema claro)
    dark: '#9ca3af',  // cinza claro (tema escuro)
  },
}
```

### 2. **Classes CSS Utilitárias**
Adicionadas no `styles.css`:

```css
.text-adaptive {
  @apply text-text-light dark:text-text-dark;
}

.text-muted-adaptive {
  @apply text-text-muted-light dark:text-text-muted-dark;
}

.input-adaptive {
  @apply border border-border-light dark:border-border-dark 
         bg-background-light dark:bg-background-dark 
         text-text-light dark:text-text-dark 
         focus:border-primary outline-none transition-colors;
}
```

### 3. **Substituições de Classes**

#### Textos Principais (Títulos)
- `text-primary` → `text-primary-text-light dark:text-primary-text-dark`
- Aplicado em: títulos de páginas, cabeçalhos de seções

#### Textos Secundários
- `text-text-muted` → `text-text-muted-light dark:text-text-muted-dark`  
- Aplicado em: descrições, labels, textos auxiliares

## Arquivos Corrigidos

### ✅ **Dashboard** (`src/app/features/dashboard/dashboard.html`)
- Título principal
- Títulos de seções (Nova Transação, Lançamentos)
- Labels de filtros
- Textos de ajuda e mensagens
- Datas e descrições de transações

### ✅ **Home** (`src/app/features/home/home.html`)
- Título principal
- Títulos dos cards de funcionalidades
- Descrições dos recursos

### ✅ **Login** (`src/app/features/usuarios/login.html`)
- Título da página
- Texto do botão secundário

### ✅ **Register** (`src/app/features/usuarios/register.html`)
- Título da página
- Texto do botão secundário

## Classes de Substituição

| Classe Antiga | Classe Nova | Contexto |
|---------------|-------------|----------|
| `text-primary` | `text-primary-text-light dark:text-primary-text-dark` | Títulos importantes |
| `text-text-muted` | `text-text-muted-light dark:text-text-muted-dark` | Textos secundários |

## Resultado

✅ **Textos visíveis** em ambos os temas  
✅ **Contraste adequado** - preto no claro, branco no escuro  
✅ **Textos secundários** com cinza apropriado para cada tema  
✅ **Transições suaves** entre temas  
✅ **Consistência visual** mantida  

## Uso para Novos Componentes

```html
<!-- Títulos principais -->
<h1 class="text-primary-text-light dark:text-primary-text-dark">Título</h1>

<!-- Textos secundários -->
<p class="text-text-muted-light dark:text-text-muted-dark">Descrição</p>

<!-- Ou use as classes utilitárias -->
<h1 class="text-adaptive">Título</h1>
<p class="text-muted-adaptive">Descrição</p>
```

Agora todos os textos devem estar perfeitamente visíveis tanto no tema claro quanto no escuro!
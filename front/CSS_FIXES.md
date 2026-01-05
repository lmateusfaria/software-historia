# Correção das Classes CSS Tailwind

## Problema Identificado
O erro de build ocorreu porque removemos as classes `neutral` do Tailwind config, mas havia referências a essas classes em vários arquivos.

## Correções Realizadas

### 1. **toast.css** - Atualizado sistema de cores
```css
// ANTES
.toast { @apply text-neutral-dark; }
.close { @apply text-neutral cursor-pointer; }
.close:hover { @apply text-neutral-dark; }

// DEPOIS  
.toast { @apply text-text-light dark:text-text-dark; }
.close { @apply text-text-muted cursor-pointer; }
.close:hover { @apply text-text-light dark:text-text-dark; }
```

### 2. **dashboard.html** - Substituições completas

#### Classes de Texto
- `text-neutral-dark` → `text-text-muted`
- Aplicado em labels, placeholders e textos secundários

#### Classes de Fundo e Border  
- `bg-neutral-light` → `bg-surface-light` ou `surface`
- `border-neutral-light` → `border-border-light`
- `bg-neutral-light/50` → `bg-surface-light/50`

#### Botões Neutros
- `bg-neutral` → `surface` (para botões secundários)
- `hover:bg-neutral-dark` → `hover:opacity-80`
- Mantém semântica de botão secundário sem cores fixas

#### Elementos de Lista
- Listas de contas e transações agora usam `.surface` e `.card`
- Consistência visual mantida

### 3. **Mapeamento de Classes**

| Classe Antiga | Classe Nova | Uso |
|---------------|-------------|-----|
| `text-neutral-dark` | `text-text-muted` | Textos secundários |
| `bg-neutral-light` | `surface` | Fundos de cards |
| `border-neutral-light` | `border-border-light` | Bordas |
| `bg-neutral` | `surface` | Botões secundários |
| `hover:bg-neutral-dark` | `hover:opacity-80` | Hover states |

## Benefícios das Correções

✅ **Compatibilidade** - Todas as classes agora existem no Tailwind config  
✅ **Consistência** - Sistema unificado de cores claro/escuro  
✅ **Semântica** - Classes com nomes que refletem sua função  
✅ **Manutenibilidade** - Fácil modificação futura do tema  

## Arquivos Corrigidos

- `src/app/shared/toast/toast.css`
- `src/app/features/dashboard/dashboard.html`

## Estado Atual

Todos os erros de build relacionados a classes CSS inexistentes foram resolvidos. A aplicação deve compilar corretamente e o sistema de tema claro/escuro deve funcionar perfeitamente.
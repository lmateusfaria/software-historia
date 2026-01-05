# Sistema de Tema Claro/Escuro

## Visão Geral

O sistema implementa um tema completo em branco e preto com capacidade de alternância entre modo claro e escuro.

## Recursos Implementados

### 1. **ThemeService** (`src/app/core/theme.service.ts`)
- Gerencia o estado do tema (claro/escuro)
- Persiste a preferência no localStorage
- Detecta automaticamente a preferência do sistema
- Aplica as classes CSS necessárias no documento

### 2. **ThemeToggleComponent** (`src/app/shared/theme-toggle/theme-toggle.component.ts`)
- Botão para alternar entre temas
- Ícones animados (sol/lua)
- Integrado ao navbar
- Acessibilidade completa

### 3. **Sistema de Cores Tailwind**

#### Cores Principais
- **primary**: Sistema baseado em preto (#000000)
- **background**: Branco puro (claro) / Preto puro (escuro)
- **surface**: Cinza claro (claro) / Cinza escuro (escuro)
- **text**: Preto (claro) / Branco (escuro)
- **border**: Bordas adaptáveis para cada tema

#### Cores Semânticas (mantidas para funcionalidade)
- **success**: Verde para ações positivas
- **warning**: Âmbar para avisos
- **danger**: Vermelho para ações destrutivas
- **info**: Azul para informações

### 4. **Classes Utilitárias CSS**

#### Classes Automáticas
- `.surface`: Aplica cores de fundo e borda automaticamente
- `.card`: Combina surface + sombra + padding
- `.btn-theme-toggle`: Estilo específico para o botão de tema

#### Uso nos Templates
```html
<!-- Fundo adaptável -->
<div class="bg-background-light dark:bg-background-dark">

<!-- Texto adaptável -->
<p class="text-text-light dark:text-text-dark">

<!-- Surface com adaptação automática -->
<div class="surface">

<!-- Card completo -->
<div class="card">
```

## Como Usar

### 1. **Importar o ThemeToggleComponent**
```typescript
import { ThemeToggleComponent } from '../shared/theme-toggle/theme-toggle.component';

@Component({
  imports: [ThemeToggleComponent],
  // ...
})
```

### 2. **Adicionar no Template**
```html
<app-theme-toggle></app-theme-toggle>
```

### 3. **Usar Classes de Tema nos Componentes**
```html
<!-- Container principal -->
<div class="bg-background-light dark:bg-background-dark transition-colors duration-300">

<!-- Cards e surfaces -->
<div class="card">
  <h2 class="text-primary">Título</h2>
  <p class="text-text-muted">Texto secundário</p>
</div>

<!-- Inputs -->
<input class="border border-border-light dark:border-border-dark bg-background-light dark:bg-background-dark text-text-light dark:text-text-dark">
```

## Características Técnicas

### Transições Suaves
- Todas as mudanças de cor têm transição de 300ms
- Aplicado via `transition-colors duration-300`

### Persistência
- Preferência salva automaticamente no localStorage
- Restaurada na próxima sessão

### Detecção do Sistema
- Se não há preferência salva, usa `prefers-color-scheme`
- Fallback para tema claro

### Performance
- Classes CSS aplicadas diretamente no `<html>`
- Sem re-renderizações desnecessárias
- Uso do BehaviorSubject para reatividade

## Páginas Atualizadas

✅ **Navbar** - Completamente adaptado
✅ **Home** - Tema aplicado em cards e texto
✅ **Login** - Formulário com tema
✅ **Register** - Formulário com tema  
✅ **Dashboard** - Cards e modais adaptados
✅ **App principal** - Container global atualizado

## Extensibilidade

Para adicionar novos componentes:

1. Use as classes definidas no Tailwind config
2. Sempre inclua `transition-colors duration-300` para transições
3. Teste em ambos os temas
4. Considere usar `.surface` e `.card` quando possível

## Manutenção

- **Tailwind Config**: `tailwind.config.js` - definir novas cores
- **Estilos Globais**: `src/styles.css` - classes utilitárias
- **ThemeService**: `src/app/core/theme.service.ts` - lógica do tema
- **ThemeToggle**: `src/app/shared/theme-toggle/` - componente de alternância
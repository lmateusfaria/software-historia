export function statusBadgeClass(status: string | undefined | null): string {
    switch (status) {
        case 'PENDENTE_OCR':
            return 'bg-red-500/20 text-red-500 border-red-500/30';
        case 'PROCESSANDO_OCR':
        case 'PROCESSANDO':
            return 'bg-blue-500/20 text-blue-500 border-blue-500/30';
        case 'AGUARDANDO_APROVACAO':
            return 'bg-amber-500/20 text-amber-500 border-amber-500/30';
        case 'APROVADO':
            return 'bg-green-500/20 text-green-500 border-green-500/30';
        case 'REJEITADO':
        case 'ERRO':
            return 'bg-red-500/20 text-red-500 border-red-500/30';
        default:
            return 'bg-neutral-500/20 text-neutral-400 border-neutral-500/30';
    }
}

export function statusBadgeClassLight(status: string | undefined | null): string {
    switch (status) {
        case 'PENDENTE_OCR':
            return 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400';
        case 'PROCESSANDO_OCR':
        case 'PROCESSANDO':
            return 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400';
        case 'AGUARDANDO_APROVACAO':
            return 'bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400';
        case 'APROVADO':
            return 'bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400';
        case 'REJEITADO':
        case 'ERRO':
            return 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400';
        default:
            return 'bg-gray-50 dark:bg-gray-800 text-gray-600 dark:text-gray-400';
    }
}

#!/bin/bash

# Script de Manutenção Automática do Docker
# Descrição: Limpa containers parados, imagens órfãs, redes não utilizadas e volumes desconectados.
# Também trunca os logs dos containers para evitar crescimento excessivo.

echo "--- Iniciando Manutenção do Docker: $(date) ---"

# 1. Limpeza de Containers, Redes e Imagens órfãs (Dangling)
echo "Limpando containers parados e imagens órfãs..."
docker system prune -f

# 2. Limpeza de Volumes não utilizados (Dangling)
echo "Limpando volumes não utilizados..."
docker volume prune -f

# 3. Truncar Logs dos Containers
# Garante que os arquivos de log não cresçam indefinidamente
echo "Truncando logs de containers..."
find /var/lib/docker/containers/ -name "*.log" -exec truncate -s 0 {} +

echo "--- Manutenção Concluída: $(date) ---"
echo ""

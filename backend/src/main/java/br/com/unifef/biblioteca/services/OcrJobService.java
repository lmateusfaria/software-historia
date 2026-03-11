package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OcrJobService {

    @Autowired
    private DocumentoRepository repository;

    // Executa a cada 3 minutos (180.000 milissegundos)
    @Scheduled(fixedRate = 180000)
    public void processPendingOcr() {
        List<Documento> pendentes = repository.findByStatus(StatusDocumento.PENDENTE_OCR);
        
        if (pendentes.isEmpty()) {
            return;
        }

        System.out.println("Sincronizador OCR: Identificados " + pendentes.size() + " documentos para processar.");

        for (Documento doc : pendentes) {
            try {
                processarDocumento(doc);
            } catch (Exception e) {
                System.err.println("Erro ao processar documento ID " + doc.getId() + ": " + e.getMessage());
            }
        }
    }

    private void processarDocumento(Documento doc) {
        // 1. Marca como processando
        doc.setStatus(StatusDocumento.PROCESSANDO_OCR);
        repository.save(doc);
        
        System.out.println("Processando OCR para: " + doc.getDescricao());

        // 2. Simulação de processamento (Aqui entrará a chamada para Tesseract ou IA no futuro)
        // Por enquanto, apenas simulamos um atraso ou sucesso imediato
        String textoExtraido = "Texto extraído automaticamente via OCR simulado para o documento: " + doc.getDescricao();
        
        doc.setConteudoOcr(textoExtraido);
        
        // 3. Finaliza enviando para revisão do professor/digitalizador
        doc.setStatus(StatusDocumento.AGUARDANDO_APROVACAO);
        repository.save(doc);
        
        System.out.println("OCR concluído para: " + doc.getDescricao() + ". Status alterado para AGUARDANDO_APROVACAO.");
    }
}

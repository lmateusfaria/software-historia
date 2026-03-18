package br.com.unifef.biblioteca.listeners;

import br.com.unifef.biblioteca.events.DocumentoCriadoEvent;
import br.com.unifef.biblioteca.services.DocumentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DocumentoEventListener {

    @Autowired
    private DocumentoService documentoService;

    @Async
    @EventListener
    public void handleDocumentoCriado(DocumentoCriadoEvent event) {
        log.info("Iniciando processamento assíncrono para o documento: {}", event.getDocumentoId());
        try {
            documentoService.processarDocumentoAssincrono(event.getDocumentoId(), event.getPreUploadedFiles());
            log.info("Processamento assíncrono concluído para o documento: {}", event.getDocumentoId());
        } catch (Exception e) {
            log.error("Erro crítico no processamento assíncrono do documento {}: {}", event.getDocumentoId(), e.getMessage(), e);
        }
    }
}

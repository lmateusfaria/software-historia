package br.com.unifef.biblioteca.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.io.File;
import java.util.List;

@Getter
public class DocumentoCriadoEvent extends ApplicationEvent {
    private final Long documentoId;
    private final List<String> preUploadedFiles;

    public DocumentoCriadoEvent(Object source, Long documentoId, List<String> preUploadedFiles) {
        super(source);
        this.documentoId = documentoId;
        this.preUploadedFiles = preUploadedFiles;
    }
}

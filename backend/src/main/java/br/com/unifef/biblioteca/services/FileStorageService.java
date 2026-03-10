package br.com.unifef.biblioteca.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileStorageService {
    String save(MultipartFile file);
    InputStream fetch(String filename);
    void delete(String filename);
}

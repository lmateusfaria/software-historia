package br.com.unifef.biblioteca.services;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class MinioStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String save(MultipartFile file) {
        try {
            ensureBucketExists();
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            log.info("Enviando para o MinIO: {} | Tamanho: {} bytes | ContentType: {}", filename, file.getSize(), file.getContentType());
            
            long start = System.currentTimeMillis();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            long end = System.currentTimeMillis();
            log.info("Arquivo salvo no MinIO em {}ms: {}", (end - start), filename);
            
            return filename;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar arquivo no MinIO", e);
        }
    }

    @Override
    public InputStream fetch(String filename) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar arquivo no MinIO", e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar arquivo no MinIO", e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}

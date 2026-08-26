package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.dtos.ServiceHealthDTO;
import br.com.unifef.biblioteca.domains.dtos.OperationalSummaryDTO;
import br.com.unifef.biblioteca.domains.dtos.SystemHealthDTO;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import br.com.unifef.biblioteca.repositories.UsuarioRepository;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemHealthService {

    private final DataSource dataSource;
    private final Driver neo4jDriver;
    private final MinioClient minioClient;
    private final DocumentoRepository documentoRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public SystemHealthService(
            DataSource dataSource,
            Driver neo4jDriver,
            MinioClient minioClient,
            DocumentoRepository documentoRepository,
            UsuarioRepository usuarioRepository) {
        this.dataSource = dataSource;
        this.neo4jDriver = neo4jDriver;
        this.minioClient = minioClient;
        this.documentoRepository = documentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public SystemHealthDTO getHealth() {
        List<ServiceHealthDTO> services = new ArrayList<>();
        services.add(checkPostgres());
        services.add(checkNeo4j());
        services.add(checkMinio());

        boolean anyDown = services.stream().anyMatch(service -> "DOWN".equals(service.getStatus()));
        boolean anyDegraded = services.stream().anyMatch(service -> "DEGRADED".equals(service.getStatus()));
        String overallStatus = anyDown ? "DOWN" : anyDegraded ? "DEGRADED" : "UP";

        return new SystemHealthDTO(
                overallStatus,
                OffsetDateTime.now().toString(),
                services
        );
    }

    public OperationalSummaryDTO getOperationalSummary() {
        OperationalSummaryDTO summary = new OperationalSummaryDTO();
        summary.setTotalDocumentos(documentoRepository.count());
        summary.setAguardandoAprovacao(documentoRepository.countByStatus(StatusDocumento.AGUARDANDO_APROVACAO));
        summary.setProcessando(
                documentoRepository.countByStatus(StatusDocumento.PROCESSANDO)
                        + documentoRepository.countByStatus(StatusDocumento.PROCESSANDO_OCR)
        );
        summary.setPendenteOcr(documentoRepository.countByStatus(StatusDocumento.PENDENTE_OCR));
        summary.setErro(documentoRepository.countByStatus(StatusDocumento.ERRO));
        summary.setTotalUsuarios(usuarioRepository.count());
        summary.setProfessores(usuarioRepository.countByPerfil(Perfil.PROFESSOR));
        summary.setAlunos(usuarioRepository.countByPerfil(Perfil.ALUNO));
        summary.setPesquisadores(usuarioRepository.countByPerfil(Perfil.PESQUISADOR));
        summary.setCheckedAt(OffsetDateTime.now().toString());
        return summary;
    }

    private ServiceHealthDTO checkPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (valid) {
                return new ServiceHealthDTO("PostgreSQL", "UP", "Conexao validada com sucesso.");
            }
            return new ServiceHealthDTO("PostgreSQL", "DOWN", "Conexao respondeu, mas foi considerada invalida.");
        } catch (Exception e) {
            return new ServiceHealthDTO("PostgreSQL", "DOWN", "Falha na conexao: " + e.getMessage());
        }
    }

    private ServiceHealthDTO checkNeo4j() {
        try {
            neo4jDriver.verifyConnectivity();
            return new ServiceHealthDTO("Neo4j", "UP", "Driver respondeu com conectividade valida.");
        } catch (Exception e) {
            return new ServiceHealthDTO("Neo4j", "DOWN", "Falha na conectividade: " + e.getMessage());
        }
    }

    private ServiceHealthDTO checkMinio() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (bucketExists) {
                return new ServiceHealthDTO("MinIO", "UP", "Bucket '" + bucketName + "' disponivel.");
            }

            return new ServiceHealthDTO("MinIO", "DEGRADED", "MinIO respondeu, mas o bucket '" + bucketName + "' nao existe.");
        } catch (Exception e) {
            return new ServiceHealthDTO("MinIO", "DOWN", "Falha na verificacao: " + e.getMessage());
        }
    }
}

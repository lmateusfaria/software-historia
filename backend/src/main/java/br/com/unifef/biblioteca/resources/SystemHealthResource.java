package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.domains.dtos.SystemHealthDTO;
import br.com.unifef.biblioteca.domains.dtos.OperationalSummaryDTO;
import br.com.unifef.biblioteca.services.SystemHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemHealthResource {

    private final SystemHealthService systemHealthService;

    public SystemHealthResource(SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    @GetMapping("/health")
    public ResponseEntity<SystemHealthDTO> health() {
        return ResponseEntity.ok(systemHealthService.getHealth());
    }

    @GetMapping("/summary")
    public ResponseEntity<OperationalSummaryDTO> summary() {
        return ResponseEntity.ok(systemHealthService.getOperationalSummary());
    }
}

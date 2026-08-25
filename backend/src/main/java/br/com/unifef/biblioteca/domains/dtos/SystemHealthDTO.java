package br.com.unifef.biblioteca.domains.dtos;

import java.util.List;

public class SystemHealthDTO {

    private String status;
    private String checkedAt;
    private List<ServiceHealthDTO> services;

    public SystemHealthDTO() {
    }

    public SystemHealthDTO(String status, String checkedAt, List<ServiceHealthDTO> services) {
        this.status = status;
        this.checkedAt = checkedAt;
        this.services = services;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }

    public List<ServiceHealthDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceHealthDTO> services) {
        this.services = services;
    }
}

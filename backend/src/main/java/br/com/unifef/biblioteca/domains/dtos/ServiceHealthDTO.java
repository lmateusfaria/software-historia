package br.com.unifef.biblioteca.domains.dtos;

public class ServiceHealthDTO {

    private String name;
    private String status;
    private String details;

    public ServiceHealthDTO() {
    }

    public ServiceHealthDTO(String name, String status, String details) {
        this.name = name;
        this.status = status;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}

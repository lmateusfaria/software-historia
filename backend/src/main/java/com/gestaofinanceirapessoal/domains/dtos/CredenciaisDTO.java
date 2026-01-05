package com.gestaofinanceirapessoal.domains.dtos;

public class CredenciaisDTO {

    private String login;
    private String password;


    public String getLogin() {
        return login;
    }

    public void setLogin(String userName) {
        this.login = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

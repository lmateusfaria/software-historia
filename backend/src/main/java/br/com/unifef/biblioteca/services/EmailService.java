package br.com.unifef.biblioteca.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:suporte@unifef.com.br}")
    private String senderEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("Recuperação de Senha - Biblioteca Digital UNIFEF");
        message.setText("Olá,\n\n" +
                "Você solicitou a recuperação da sua senha na plataforma da Biblioteca Digital UNIFEF.\n\n" +
                "Clique no link abaixo para redefini-la:\n" +
                resetLink + "\n\n" +
                "Se você não solicitou essa redefinição, por favor ignore este e-mail.");

        mailSender.send(message);
    }
}

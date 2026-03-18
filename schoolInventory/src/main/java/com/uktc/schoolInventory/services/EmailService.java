package com.uktc.schoolInventory.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRentalConfirmation(String toEmail, String equipmentName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Ново наемане");
        message.setText("Здравейте, успешно наехте: " + equipmentName);
        mailSender.send(message);
    }

    public void sendOverdueReminder(String toEmail, String equipmentName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("⚠️ ПРОСРОЧЕНА ТЕХНИКА");
        message.setText("Здравейте, срокът за връщане на " + equipmentName + " е изтекъл!");
        mailSender.send(message);
    }
}
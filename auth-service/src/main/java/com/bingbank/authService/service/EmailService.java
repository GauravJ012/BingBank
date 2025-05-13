package com.bingbank.authService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setFrom("no-reply@netbanking.com");
        helper.setTo(to);
        helper.setSubject("Your NetBanking OTP");
        
        String content = "<html><body>"
                + "<h2>NetBanking Authentication</h2>"
                + "<p>Your One Time Password (OTP) for NetBanking login is:</p>"
                + "<h3 style='color:blue;'>" + otp + "</h3>"
                + "<p>This OTP is valid for 5 minutes.</p>"
                + "<p>If you didn't request this OTP, please ignore this email.</p>"
                + "<p>Regards,<br/>NetBanking Team</p>"
                + "</body></html>";
        
        helper.setText(content, true);
        
        mailSender.send(message);
    }
}
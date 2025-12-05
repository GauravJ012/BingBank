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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("no-reply@netbanking.com");
            helper.setTo(to);
            helper.setSubject("BingBank - Login OTP");

            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head><style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; }"
                    + ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".header h1 { margin: 0; font-size: 32px; font-weight: bold; }"
                    + ".content { background: #f9f9f9; padding: 40px 30px; border-radius: 0 0 10px 10px; }"
                    + ".content h2 { color: #333; margin-top: 0; }"
                    + ".otp-box { background: white; border: 3px dashed #667eea; padding: 30px; text-align: center; margin: 30px 0; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                    + ".otp { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace; }"
                    + ".validity { color: #666; font-size: 14px; margin-top: 15px; }"
                    + ".info-box { background: #e3f2fd; padding: 20px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }"
                    + ".info-box p { margin: 0; color: #1976d2; }"
                    + ".warning { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }"
                    + ".warning strong { color: #856404; }"
                    + ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; margin-top: 20px; border-top: 1px solid #ddd; }"
                    + ".footer p { margin: 5px 0; }"
                    + "</style></head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h1>🏦 BingBank</h1>"
                    + "<p style='margin: 10px 0 0 0; font-size: 16px;'>Secure Banking Made Simple</p>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<h2>Login Authentication</h2>"
                    + "<p>Hello,</p>"
                    + "<p>You are attempting to log in to your BingBank account. Please use the One-Time Password (OTP) below to complete your login:</p>"
                    + "<div class='otp-box'>"
                    + "<div class='otp'>" + otp + "</div>"
                    + "<p class='validity'>⏱️ This OTP is valid for <strong>5 minutes</strong></p>"
                    + "</div>"
                    + "<div class='info-box'>"
                    + "<p><strong>🔒 Security Tip:</strong> Never share your OTP with anyone, including BingBank staff. We will never ask for your OTP via phone, email, or text.</p>"
                    + "</div>"
                    + "<div class='warning'>"
                    + "<strong>⚠️ Important:</strong> If you did not request this OTP, please ignore this email and ensure your account is secure. Consider changing your password immediately."
                    + "</div>"
                    + "<p style='margin-top: 30px;'>Thank you for banking with us!</p>"
                    + "<p style='margin-bottom: 0;'><strong>Best regards,</strong><br/>BingBank Team</p>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>This is an automated email. Please do not reply.</p>"
                    + "<p>&copy; 2025 BingBank. All rights reserved.</p>"
                    + "<p style='margin-top: 15px; font-size: 10px; color: #999;'>BingBank - Your Trusted Banking Partner</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("Login OTP email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Error sending login OTP email: " + e.getMessage());
            throw new RuntimeException("Failed to send login OTP email");
        }
    }
    
    /**
     * Send password reset OTP
     */
    public void sendPasswordResetOTP(String toEmail, String otp, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("no-reply@netbanking.com");
            helper.setTo(toEmail);
            helper.setSubject("BingBank - Password Reset OTP");

            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head><style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; }"
                    + ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".header h1 { margin: 0; font-size: 32px; font-weight: bold; }"
                    + ".content { background: #f9f9f9; padding: 40px 30px; border-radius: 0 0 10px 10px; }"
                    + ".content h2 { color: #333; margin-top: 0; }"
                    + ".otp-box { background: white; border: 3px dashed #667eea; padding: 30px; text-align: center; margin: 30px 0; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                    + ".otp { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace; }"
                    + ".validity { color: #666; font-size: 14px; margin-top: 15px; }"
                    + ".info-box { background: #e3f2fd; padding: 20px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }"
                    + ".info-box p { margin: 0; color: #1976d2; }"
                    + ".warning { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }"
                    + ".warning strong { color: #856404; }"
                    + ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; margin-top: 20px; border-top: 1px solid #ddd; }"
                    + ".footer p { margin: 5px 0; }"
                    + "</style></head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h1>🏦 BingBank</h1>"
                    + "<p style='margin: 10px 0 0 0; font-size: 16px;'>Secure Banking Made Simple</p>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<h2>Password Reset Request</h2>"
                    + "<p>Hello " + firstName + ",</p>"
                    + "<p>We received a request to reset your password. Please use the One-Time Password (OTP) below to complete the password reset process:</p>"
                    + "<div class='otp-box'>"
                    + "<div class='otp'>" + otp + "</div>"
                    + "<p class='validity'>⏱️ This OTP is valid for <strong>10 minutes</strong></p>"
                    + "</div>"
                    + "<div class='info-box'>"
                    + "<p><strong>🔒 Security Tip:</strong> Never share your OTP with anyone, including BingBank staff. We will never ask for your OTP via phone, email, or text.</p>"
                    + "</div>"
                    + "<div class='warning'>"
                    + "<strong>⚠️ Security Alert:</strong> If you did not request a password reset, please ignore this email and ensure your account is secure. Consider changing your password immediately if you suspect unauthorized access."
                    + "</div>"
                    + "<p style='margin-top: 30px;'>Thank you for banking with us!</p>"
                    + "<p style='margin-bottom: 0;'><strong>Best regards,</strong><br/>BingBank Team</p>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>This is an automated email. Please do not reply.</p>"
                    + "<p>&copy; 2025 BingBank. All rights reserved.</p>"
                    + "<p style='margin-top: 15px; font-size: 10px; color: #999;'>BingBank - Your Trusted Banking Partner</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("Password reset OTP email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Error sending password reset OTP email: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }
}
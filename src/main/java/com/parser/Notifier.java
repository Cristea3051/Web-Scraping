package com.parser;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

public class Notifier {
    private final String fromEmail = "your_email@gmail.com"; // Înlocuiește
    private final String password = "your_app_password"; // Înlocuiește
    private final String toEmail = "recipient_email@example.com"; // Înlocuiește

    public void sendEmailNotification(SiteConfig config, List<Document> newDocuments) {
        if (newDocuments.isEmpty()) {
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Actualizări noi pe " + config.getName());

            StringBuilder body = new StringBuilder("S-au găsit " + newDocuments.size() + " documente noi pe " + config.getName() + ":\n\n");
            for (Document doc : newDocuments) {
                body.append("Titlu: ").append(doc.getTitle()).append("\nLink: ").append(doc.getLink()).append("\n\n");
            }

            message.setText(body.toString());
            Transport.send(message);
            System.out.println("Notificare trimisă pentru " + config.getName());
        } catch (MessagingException e) {
            System.err.println("Eroare la trimiterea e-mailului pentru " + config.getName() + ": " + e.getMessage());
        }
    }
}
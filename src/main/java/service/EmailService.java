package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EmailService {
    private static final String FROM_EMAIL = "edayetna2025@gmail.com";
    private static final String APP_PASSWORD = "xhxmmmnldgemrlyj";
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    public void sendEmail(String toEmail, String subject, String content) {
        // Vérification des paramètres
        if (toEmail == null || toEmail.isEmpty()) {
            throw new IllegalArgumentException("L'adresse email du destinataire est invalide");
        }

        // Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true"); // important !
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.debug", "true");


        try {
            // Création de la session
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // HTML content
            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                    ".header { text-align: center; margin-bottom: 30px; }" +
                    ".logo { max-width: 150px; height: auto; }" +
                    ".content { background-color: #f9f9f9; padding: 20px; border-radius: 5px; }" +
                    ".code { font-size: 24px; font-weight: bold; color: #3498db; text-align: center; margin: 20px 0; }" +
                    ".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #777; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='cid:logo' class='logo' alt='DevElite Logo'>" +
                    "</div>" +
                    "<div class='content'>" +
                    "<h2>Réinitialisation de votre mot de passe</h2>" +
                    "<p>Bonjour,</p>" +
                    "<p>Vous avez demandé la réinitialisation de votre mot de passe. Voici votre code de vérification :</p>" +
                    "<div class='code'>" + content + "</div>" +
                    "<p>Ce code expirera dans 1 minute. Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>" +
                    "</div>" +
                    "<div class='footer'>" +
                    "<p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                    "<p>© 2024 DevElite. Tous droits réservés.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            // Création du message multipart
            MimeMultipart multipart = new MimeMultipart("related");

            // Partie HTML
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            // Partie image (logo)
            try {
                File logoFile = new File("src/main/resources/logo1.png");
                if (logoFile.exists()) {
                    MimeBodyPart imagePart = new MimeBodyPart();
                    DataSource fds = new FileDataSource(logoFile);
                    imagePart.setDataHandler(new DataHandler(fds));
                    imagePart.setHeader("Content-ID", "<logo>");
                    multipart.addBodyPart(imagePart);
                } else {
                    LOGGER.warning("Le fichier logo.png n'a pas été trouvé dans le chemin spécifié");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de l'ajout du logo", e);
            }

            message.setContent(multipart);

            // Envoi de l'email
            Transport.send(message);
            LOGGER.info("Email envoyé avec succès à " + toEmail);

        } catch (AuthenticationFailedException e) {
            LOGGER.severe("Échec de l'authentification SMTP: " + e.getMessage());
            throw new RuntimeException("Erreur d'authentification SMTP. Vérifiez les identifiants.", e);
        } catch (MessagingException e) {
            LOGGER.severe("Erreur lors de l'envoi de l'email: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email. Vérifiez la connexion internet et les paramètres SMTP.", e);
        } catch (Exception e) {
            LOGGER.severe("Erreur inattendue: " + e.getMessage());
            throw new RuntimeException("Une erreur inattendue s'est produite lors de l'envoi de l'email.", e);
        }
    }
    public void sendEmail2(String toEmail, String subject, String content) {
        // Vérification des paramètres
        if (toEmail == null || toEmail.isEmpty()) {
            throw new IllegalArgumentException("L'adresse email du destinataire est invalide");
        }

        // Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true"); // important !
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.debug", "true");

        try {
            // Création de la session
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // HTML content
            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                    ".header { text-align: center; margin-bottom: 30px; }" +
                    ".logo { max-width: 150px; height: auto; }" +
                    ".content { background-color: #f9f9f9; padding: 20px; border-radius: 5px; }" +
                    ".code { font-size: 24px; font-weight: bold; color: #3498db; text-align: center; margin: 20px 0; }" +
                    ".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #777; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='cid:logo' class='logo' alt='DevElite Logo'>" +
                    "</div>" +
                    "<div class='content'>" +
                    "<h2>Réponse de votre message</h2>" +
                    "<p>Bonjour,</p>" +
                    "<p>Vous avez envoyé un messaage et voici la réponse de Edayetna :</p>" +
                    "<div class='code'>" + content + "</div>" +
                    "<p>Cordialement.</p>" +
                    "</div>" +
                    "<div class='footer'>" +
                    "<p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                    "<p>© 2024 DevElite. Tous droits réservés.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            // Création du message multipart
            MimeMultipart multipart = new MimeMultipart("related");

            // Partie HTML
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            // Partie image (logo)
            try {
                File logoFile = new File("src/main/resources/logo1.png");
                if (logoFile.exists()) {
                    MimeBodyPart imagePart = new MimeBodyPart();
                    DataSource fds = new FileDataSource(logoFile);
                    imagePart.setDataHandler(new DataHandler(fds));
                    imagePart.setHeader("Content-ID", "<logo>");
                    multipart.addBodyPart(imagePart);
                } else {
                    LOGGER.warning("Le fichier logo.png n'a pas été trouvé dans le chemin spécifié");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de l'ajout du logo", e);
            }

            message.setContent(multipart);

            // Envoi de l'email
            Transport.send(message);
            LOGGER.info("Email envoyé avec succès à " + toEmail);

        } catch (AuthenticationFailedException e) {
            LOGGER.severe("Échec de l'authentification SMTP: " + e.getMessage());
            throw new RuntimeException("Erreur d'authentification SMTP. Vérifiez les identifiants.", e);
        } catch (MessagingException e) {
            LOGGER.severe("Erreur lors de l'envoi de l'email: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email. Vérifiez la connexion internet et les paramètres SMTP.", e);
        } catch (Exception e) {
            LOGGER.severe("Erreur inattendue: " + e.getMessage());
            throw new RuntimeException("Une erreur inattendue s'est produite lors de l'envoi de l'email.", e);
        }
    }
} 
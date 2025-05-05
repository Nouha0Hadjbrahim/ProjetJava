package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import model.Commande;
import model.LigneDeCommande;
import model.Produit;
import service.ProduitService;

import java.util.List;
import java.util.Properties;


public class EmailcService {
    private static final String USERNAME = "emenmrad@gmail.com";
    private static final String PASSWORD = "ridqscsroxqtmqka";
    private static final ProduitService produitService = new ProduitService();

    private static void sendEmail(String recipient, String subject, String content) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(content, "text/html");

            Transport.send(message);
            System.out.println("✅ Email envoyé à " + recipient);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ Échec de l'envoi de l'email: " + e.getMessage());
        }
    }

    public static void sendOrderConfirmation(String recipient, String userName, Commande commande, List<LigneDeCommande> items) {
        String subject = "Confirmation de votre commande #" + commande.getId();
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>")
                .append("<h2 style='color: blue;'>Bonjour ").append(userName).append(",</h2>")
                .append("<p>Votre commande a été ").append(commande.getStatut().equals("Payée") ? "payée" : "reçue").append(" avec succès !</p>")
                .append("<p><strong>Commande #").append(commande.getId()).append("</strong></p>")
                .append("<p><strong>Date :</strong> ").append(commande.getDateCommande()).append("</p>")
                .append("<p><strong>Montant total :</strong> ").append(String.format("%.2f TND", commande.getMontantTotal())).append("</p>")
                .append("<p><strong>Adresse de livraison :</strong> ").append(commande.getAdresseLivraison()).append("</p>")
                .append("<p><strong>Méthode de paiement :</strong> ").append(commande.getPaiement()).append("</p>")
                .append("<h3>Articles commandés :</h3>")
                .append("<ul>");

        for (LigneDeCommande item : items) {
            Produit produit = produitService.getProduitById(item.getIdProduit());
            String productName = produit != null ? produit.getNomProduit() : "Produit inconnu";
            emailContent.append("<li>")
                    .append(productName)
                    .append(" - Quantité: ").append(item.getQuantite())
                    .append(" - Prix unitaire: ").append(String.format("%.2f TND", item.getPrixUnitaire()))
                    .append(" - Sous-total: ").append(String.format("%.2f TND", item.getPrixUnitaire() * item.getQuantite()))
                    .append("</li>");
        }

        emailContent.append("</ul>")
                .append("<p>Merci pour votre achat ! 😊</p>")
                .append("<p>Pour toute question, contactez-nous à support@votreplateforme.com.</p>")
                .append("<hr>")
                .append("<p style='color:gray; font-size:12px;'>Ceci est un email automatique, veuillez ne pas répondre.</p>")
                .append("</body></html>");

        sendEmail(recipient, subject, emailContent.toString());
    }
}

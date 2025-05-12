package service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import model.LigneDeCommande;
import model.Produit;
import service.ProduitService;

import java.util.List;

public class PaymentService {

    private static final ProduitService produitService = new ProduitService();

    static {
        Stripe.apiKey = "sk_test_51QCAHpBDW3LkkcbKFv9eNqLWddEC9JLkjAoZkB6VC6e52cHjojXS5vlEoWlZQWCeeAaN67bpZllUrR9RWehULuup00lCYHx3bX"; // Replace with your Stripe Secret Key
    }

    public static String createCheckoutSession(double amount, List<LigneDeCommande> items) throws StripeException {
        // Convert TND to USD (example exchange rate: 1 TND = 0.32 USD)
        double exchangeRate = 0.32; // Replace with a dynamic rate from an API if needed
        double amountInUSD = amount * exchangeRate;
        long amountInCents = (long) (amountInUSD * 100); // Convert USD to cents

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://yourdomain.com/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("https://yourdomain.com/cancel")
                .setCurrency("usd"); // Use USD since TND is not supported

        // Add line items dynamically based on LigneDeCommande
        for (LigneDeCommande item : items) {
            Produit produit = produitService.getProduitById(item.getIdProduit());
            String productName = (produit != null && produit.getNomProduit() != null) ? produit.getNomProduit() : "Produit inconnu";
            double unitPriceInUSD = item.getPrixUnitaire() * exchangeRate;

            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantite())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount((long) (unitPriceInUSD * 100)) // Convert price to cents
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(productName)
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getUrl(); // Return the Stripe checkout URL
    }
}
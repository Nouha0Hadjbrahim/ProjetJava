package service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class TwilioService {

    // ✅ Replace with your Twilio credentials
    private static final String ACCOUNT_SID = "ACba55211018f9c38bde44aa6f7d0fcc8d";
    private static final String AUTH_TOKEN = "d9d2dc1a2d7b628e8d25f7565b1ebb9d";
    private static final String TWILIO_PHONE_NUMBER = "+16203309715";

    public TwilioService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSMS(String toPhoneNumber, String messageText) {
        try {
            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(toPhoneNumber),
                    new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
                    messageText
            ).create();

            System.out.println("✅ SMS sent successfully! SID: " + message.getSid());
        } catch (Exception e) {
            System.out.println("❌ Error sending SMS: " + e.getMessage());
        }
    }
}

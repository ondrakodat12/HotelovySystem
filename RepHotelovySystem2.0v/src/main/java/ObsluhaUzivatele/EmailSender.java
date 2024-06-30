package ObsluhaUzivatele;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    private static final String USERNAME = "hotel.system.booking@gmail.com";
    private static final String PASSWORD = "bxlq taws xixd nmks";

    public static void sendEmail(String toEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email was sent successfully!");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // Metoda pro odeslání registračního kódu na email
    public static void sendRegistrationCode(String toEmail, String code) {
        String subject = "Registrace do systému";
        String body = "Váš registrační kód je: " + code;

        sendEmail(toEmail, subject, body);
    }

    // Metoda pro odeslání verifikačního kódu pro zapomenuté heslo
    public static void sendVerificationCode(String toEmail, String code) {
        String subject = "Obnova hesla - verifikační kód";
        String body = "Váš verifikační kód pro obnovu hesla je: " + code;

        sendEmail(toEmail, subject, body);
    }
}

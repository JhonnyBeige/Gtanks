package gtanks.services.email;

import gtanks.logger.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailHandler {
    private static final EmailHandler instance = new EmailHandler();
    private final String username = "no-replygtanksonline@mail.ru";
    private final String password = "F37kjTnadMFN8UpU09iZ";
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 32;
    public String VerificationKey;
    public String toEmail;

    public static EmailHandler getInstance() {
        return instance;
    }

    public String generateKey() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder keyBuilder = new StringBuilder(32);
        for (int i = 0; i < 32; ++i) {
            int randomCharIndex = secureRandom.nextInt(ALLOWED_CHARACTERS.length());
            keyBuilder.append(ALLOWED_CHARACTERS.charAt(randomCharIndex));
        }
        return keyBuilder.toString();
    }

    public CompletableFuture<Void> sendEmailAsync(String toEmail, String confirmationCode, String userNickname) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.mail.ru");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props, new Authenticator(){

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("no-replygtanksonline@mail.ru", "F37kjTnadMFN8UpU09iZ");
                }
            });
            try {
                MimeMessage message = new MimeMessage(session);
                ((Message)message).setFrom(new InternetAddress("no-replygtanksonline@mail.ru"));
                ((Message)message).setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                ((Message)message).setSubject("GTanks Привязка почты/Смена пароля");
                String emailContent = "<html><body><p><b>%s!</b> Вы получили это письмо, так как запросили привязку почты или смену пароля для вашей учетной записи. Для завершения процедуры привязки почты/изменения пароля, воспользуйтесь следующей информацией:</p><p>Введите ваш уникальный код подтверждения в диалоговое окно: <span style='font-weight:bold'>%s</span></b></p><p>Если вы не запрашивали привязки почты/изменение пароля немедленно свяжитесь с нашей службой поддержки отправив тикет \"Взлом аккаунта\".</p><p>Спасибо за внимание к безопасности вашей учетной записи!</p><p><b>С уважением,<br/>Команда GTanks Online!</b></p></body></html>".formatted(userNickname, confirmationCode);
                message.setContent(emailContent, "text/html; charset=utf-8");
                Transport.send(message);
                Logger.debug("[Email Handler]: Email sent successfully to " + toEmail);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
    }
}

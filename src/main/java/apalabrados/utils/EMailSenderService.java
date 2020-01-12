package apalabrados.utils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EMailSenderService {
	private final Properties properties = new Properties();
	private String smtpHost, startTTLS, smtpPort;
	private String remitente, serverUser, userAutentication, pwd;
	private String serverIp;
	
	public EMailSenderService() {
		this.smtpHost="smtp.gmail.com";
		this.startTTLS="true";
		this.smtpPort="465";
		this.remitente="apalabrados.tsw@gmail.com";
		this.serverUser="apalabrados.tsw@gmail.com";
		this.userAutentication="true";
		this.pwd="apalabrados123";

		try {
			this.serverIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {

			this.serverIp = "localhost";
		}
				
		properties.put("mail.smtp.host", this.smtpHost);  
        properties.put("mail.smtp.starttls.enable", this.startTTLS);  
        properties.put("mail.smtp.port", this.smtpPort);  
        properties.put("mail.smtp.mail.sender", this.remitente);  
        properties.put("mail.smtp.user", this.serverUser);  
        properties.put("mail.smtp.auth", this.userAutentication);
        properties.put("mail.smtp.socketFactory.port", this.smtpPort);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
	}
	
	
	public void enviarPorGmail(String destinatario, String codigo) throws MessagingException {        
        Authenticator auth = new autentificadorSMTP();
        Session session = Session.getInstance(properties, auth);

        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("Apalabrados-TSW - Recuperación de contraseña");
        msg.setText(("<p><strong>Hola</strong></p>"
        		+ "<p>Estás recibiendo este correo porque hiciste una solicitud de recuperación de contraseña para tu cuenta. Haga click en el"
        		+ " siguiente enlace para definir una nueva contraseña:</p>"
        		+  "http://"+ this.serverIp + ":8080/pwdReset.html?code=" + codigo
        		+ "<p>Si no realizaste esta solicitud, ignore este mensaje</p>"
        		+ "<p>Saludos, Apalabrados-TSW</p>" 
        		), "UTF-8", "html");
        
        msg.setFrom(new InternetAddress(this.remitente));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
        Transport.send(msg);
	}
	
	private class autentificadorSMTP extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(remitente, pwd);
        }
    }
}
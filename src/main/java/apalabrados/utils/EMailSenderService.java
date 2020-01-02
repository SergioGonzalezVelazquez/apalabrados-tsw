package apalabrados.utils;
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
	private String smtpHost, startTTLS, port;
	private String remitente, serverUser, userAutentication, pwd;
	
	public void enviarPorGmail(String destinatario, long codigo) throws MessagingException {
		this.smtpHost="smtp.gmail.com";
		this.startTTLS="true";
		this.port="465";
		this.remitente="edu.uclm.esi.tysw@gmail.com";
		this.serverUser="edu.uclm.esi.tysw@gmail.com";
		this.userAutentication="true";
		this.pwd="tecnologiasysistemasweb123";
		properties.put("mail.smtp.host", this.smtpHost);  
        properties.put("mail.smtp.starttls.enable", this.startTTLS);  
        properties.put("mail.smtp.port", this.port);  
        properties.put("mail.smtp.mail.sender", this.remitente);  
        properties.put("mail.smtp.user", this.serverUser);  
        properties.put("mail.smtp.auth", this.userAutentication);
        properties.put("mail.smtp.socketFactory.port", this.port);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        
        Authenticator auth = new autentificadorSMTP();
        Session session = Session.getInstance(properties, auth);

        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("LaOca - recuperación de contraseña");
        msg.setText("Pulsa en el siguiente enlace para crear una nueva contraseña: http://...../crearpwd.jsp?code=" + codigo);
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
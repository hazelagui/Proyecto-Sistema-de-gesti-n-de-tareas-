package com.mycompany.sistemagestiondetareas.util;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Clase encargada del envío de correos electrónicos.
 * Utiliza el servicio SMTP de Gmail para enviar notificaciones.
 */
public class EmailSender {
    // Configuración del servidor SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "dummymail1899@gmail.com";  // Cuenta de correo del sistema
    private static final String PASSWORD = "wsud oiiq toyt vgns";      // Contraseña de aplicación
    
    private final Session session;  // Sesión de correo configurada
    
    /**
     * Constructor que configura la sesión de correo con las credenciales.
     */
    public EmailSender() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        // Configura la autenticación con el servidor SMTP
        session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });
    }
    
    /**
     * Envía un correo electrónico al destinatario especificado.
     * @param destinatario Dirección de correo del destinatario
     * @param asunto Asunto del correo
     * @param contenido Contenido del mensaje
     */
    public void enviarCorreo(String destinatario, String asunto, String contenido) {
        try {
            // Crea y configura el mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setText(contenido);
            
            // Envía el correo
            Transport.send(message);
            System.out.println("Correo enviado exitosamente a: " + destinatario);
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
        }
    }
} 
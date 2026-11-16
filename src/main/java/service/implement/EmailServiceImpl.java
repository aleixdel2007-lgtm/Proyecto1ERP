/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import service.interfaces.EmailService;

import java.util.Locale;

/**
 *
 * @author Sandy
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    /**
     * Idioma de los correos.
     *
     * Un correo no se envia dentro de la peticion del destinatario: la cuenta la activa un
     * gestor desde el panel, asi que el idioma de esa peticion es el del gestor, no el del
     * cliente. Como el idioma elegido vive solo en una cookie del navegador y no se guarda
     * en el usuario, no hay forma de saber en que idioma lo quiere leer: los correos salen
     * en castellano.
     *
     * El dia que la tabla usuario guarde el idioma, es el que hay que pasar aqui: el texto
     * ya esta traducido a los tres idiomas en los .properties.
     */
    private static final Locale IDIOMA_CORREOS = Locale.forLanguageTag("es");

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void enviarCuentaActivada(String destino, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("no-reply@luduserp.cat");
        mensaje.setTo(destino);
        mensaje.setSubject(texto("correo.activada.asunto"));
        mensaje.setText(texto("correo.activada.cuerpo", nombre));

        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
        }
    }

    // Este no va a un cliente sino a inventario, que trabaja en el panel y lo tiene todo en
    // castellano: se queda sin traducir hasta que se traduzca el panel
    @Override
    public void enviarAvisoStockBajo(String destino, String titulo, long quedan) {

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("no-reply@luduserp.cat");
        mensaje.setTo(destino);
        mensaje.setSubject("[LudusERP] Stock bajo: " + titulo + " (quedan " + quedan + ")");
        mensaje.setText("Quedan " + quedan + " claves disponibles de \"" + titulo + "\".\n\n"
                + "Conviene reponer stock.\n\nLudusERP");

        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
        }
    }

    // Los saltos de linea van como \n dentro del .properties
    private String texto(String clave, Object... args) {
        return messageSource.getMessage(clave, args, clave, IDIOMA_CORREOS);
    }
}

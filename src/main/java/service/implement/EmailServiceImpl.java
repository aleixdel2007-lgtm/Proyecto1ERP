/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import service.interfaces.EmailService;

/**
 *
 * @author Sandy
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void enviarCuentaActivada(String destino, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("no-reply@luduserp.cat");
        mensaje.setTo(destino);
        mensaje.setSubject("Tu cuenta de LudusERP ya está activa");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "Un gestor ha activado tu cuenta. Ya puedes iniciar sesión en LudusERP.\n\n"
                + "Un saludo,\nEl equipo de LudusERP");

        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
        }
    }

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
}

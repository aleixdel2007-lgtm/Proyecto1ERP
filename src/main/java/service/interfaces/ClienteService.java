/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service.interfaces;

import java.time.LocalDate;
import model.seguridad.Cliente;

import java.util.List;

/**
 *
 * @author Sandy
 */
public interface ClienteService {

    List<Cliente> buscarFiltrado(String texto, String pais, LocalDate desde);

    List<String> listarPaises();

    Cliente buscarClienteXId(Long id);

    void cambiarEstado(Long id, boolean activo);

    void eliminarCliente(Long id);

    void solicitarActivacion(String email);
}

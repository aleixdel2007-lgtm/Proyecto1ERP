/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service.interfaces;

import java.time.LocalDate;
import model.seguridad.Cliente;
import java.util.Map;

import java.util.List;

/**
 *
 * @author Sandy
 */
public interface ClienteService {

    List<Cliente> buscarFiltrado(String texto, String pais, LocalDate desde);

    List<String> listarPaises();

    // id del cliente -> ruta de su foto de perfil; los que no tienen foto no salen en el mapa
    Map<Long, String> listarImagenesPerfil(List<Cliente> clientes);

    Cliente buscarClienteXId(Long id);

    void cambiarEstado(Long id, boolean activo);

    void eliminarCliente(Long id);

    void solicitarActivacion(String email);
    
    void comprobarCorreoLibre(String email);
}

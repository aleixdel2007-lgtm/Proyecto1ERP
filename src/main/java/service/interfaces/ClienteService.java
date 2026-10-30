/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service.interfaces;

import model.seguridad.Cliente;

import java.util.List;

/**
 *
 * @author Sandy
 */
public interface ClienteService {

    List<Cliente> listar();

    List<Cliente> buscarFiltrado(String texto);
}

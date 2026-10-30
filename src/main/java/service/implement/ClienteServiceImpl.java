/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.ClienteDao;
import java.util.List;
import model.seguridad.Cliente;
import service.interfaces.ClienteService;

/**
 *
 * @author Sandy
 */
public class ClienteServiceImpl implements ClienteService {

    private ClienteDao clienteDao;

    @Override
    public List<Cliente> listar() {
        return clienteDao.findAll();
    }

    @Override
    public List<Cliente> buscarFiltrado(String texto) {
        return clienteDao.buscarFiltrado(texto);
    }

}

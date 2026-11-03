/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.ClienteDao;
import java.util.List;
import model.seguridad.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ClienteService;

/**
 *
 * @author Sandy
 */
@Service
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteDao clienteDao;

    @Override
    public List<Cliente> listar() {
        return clienteDao.findAll();
    }

    @Override
    public List<Cliente> buscarFiltrado(String texto) {
        if (texto == null || texto.isBlank()) { //si el texto es en blanco, es que no hay filtrado
            return clienteDao.findAll(); // y retorno la lista normal
        }
        return clienteDao.buscarFiltrado(texto.trim());
    }

}

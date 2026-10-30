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
        return clienteDao.buscarFiltrado(texto);
    }

}

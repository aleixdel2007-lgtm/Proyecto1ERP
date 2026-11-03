/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.ClienteDao;
import exception.ClienteConDatosAsociadosException;
import exception.ClienteNoEncontradoException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.seguridad.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
    public List<Cliente> buscarFiltrado(String texto, String pais, LocalDate desde) {
        String textoLimpio = (texto == null || texto.isBlank()) ? null : texto.trim();
        String paisLimpio = (pais == null || pais.isBlank()) ? null : pais;
        LocalDateTime desdeFecha = (desde == null) ? null : desde.atStartOfDay(); //El atStartOfDay() es porque el usuario elige un día (LocalDate)
        return clienteDao.buscarFiltrado(textoLimpio, paisLimpio, desdeFecha);    // pero en la base de datos la fecha de alta lleva hora (LocalDateTime). 
    }                                                                             //Convierte “03/11” en “03/11 a las 00:00”

    @Override
    public List<String> listarPaises() {
        return clienteDao.listarPaises();
    }

    @Override
    public Cliente buscarClienteXId(Long id) {
        Cliente cliente = clienteDao.findById(id).orElse(null);

        if (cliente == null) {
            throw new ClienteNoEncontradoException(id);
        }
        return cliente;
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Cliente cliente = buscarClienteXId(id);
        cliente.setActivo(activo); // el boolean llega desde el html: es lo contrario al estado actual
        clienteDao.save(cliente);
    }

    @Override
    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = buscarClienteXId(id);
        try {
            clienteDao.delete(cliente);
            clienteDao.flush(); // fuerza a mandar el delete a la bd ya, para que el error salte dentro del try
        } catch (DataIntegrityViolationException e) {
            throw new ClienteConDatosAsociadosException();
        }

    }
}

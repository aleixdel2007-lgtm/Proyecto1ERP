/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.ClienteDao;
import dao.UsuarioDao;
import exception.cliente.ClienteConDatosAsociadosException;
import exception.cliente.ClienteNoEncontradoException;
import exception.email.CorreoYaRegistradoException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.seguridad.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ClienteService;
import service.interfaces.EmailService;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author Sandy
 */
@Service
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteDao clienteDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private dao.ImagenUsuarioDao imagenUsuarioDao;

    @Override
    public Map<Long, String> listarImagenesPerfil(List<Cliente> clientes) {

        Map<Long, String> fotos = new HashMap<>();

        if (clientes == null || clientes.isEmpty()) {
            return fotos;
        }

        List<Long> ids = new ArrayList<>();
        for (Cliente cliente : clientes) {
            ids.add(cliente.getId());
        }

        for (Object[] fila : imagenUsuarioDao.buscarPorUsuarios(ids)) {
            fotos.put((Long) fila[0], (String) fila[1]);
        }
        return fotos;
    }

    @Autowired
    private EmailService emailService;

    @Override
    public List<Cliente> buscarFiltrado(String texto, String pais, LocalDate desde) {
        String textoLimpio = (texto == null || texto.isBlank()) ? null : texto.trim();
        String paisLimpio = (pais == null || pais.isBlank()) ? null : pais;
        LocalDateTime desdeFecha = (desde == null) ? null : desde.atStartOfDay(); //El atStartOfDay() es porque el usuario elige un día (LocalDate)
        return clienteDao.buscarFiltrado(textoLimpio, paisLimpio, desdeFecha);
    }

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
        boolean estabaInactivo = !cliente.isActivo();

        cliente.setActivo(activo);
        cliente.setFechaSolicitud(null);
        clienteDao.save(cliente);

        if (activo && estabaInactivo) {
            emailService.enviarCuentaActivada(cliente.getEmail(), cliente.getNombre());
        }
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

    @Override
    @Transactional
    public void solicitarActivacion(String email) {
        Cliente cliente = clienteDao.findByEmail(email.trim().toLowerCase()).orElse(null);
        // solo guardamos la solicitud si el cliente existe, esta inactivo y no tiene ya una pendiente
        // si no se cumple, no hacemos nada ni avisamos, para no revelar que cuentas existen
        if (cliente == null || cliente.isActivo() || cliente.getFechaSolicitud() != null) {
            return;
        }
        cliente.setFechaSolicitud(LocalDateTime.now());
        clienteDao.save(cliente);
    }

    @Override
    public void comprobarCorreoLibre(String email) {
        // miramos en todos los usuarios (clientes, empleados...) porque el correo es unico para todos
        if (usuarioDao.findByEmail(email).isPresent()) {
            throw new CorreoYaRegistradoException();
        }
    }
}

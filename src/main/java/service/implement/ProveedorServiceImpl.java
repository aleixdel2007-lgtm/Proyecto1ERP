package service.implement;

import config.IndiceCiego;
import dao.ProveedorDao;
import exception.CifDuplicadoException;
import exception.ProveedorConCuentaException;
import exception.ProveedorConReferenciasException;
import exception.ProveedorNoEncontradoException;
import java.util.List;
import model.aprovisionamiento.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import service.interfaces.ProveedorService;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    @Autowired
    private ProveedorDao proveedorDao;

    @Override
    public List<Proveedor> listarProveedores(String texto, String pais, Boolean activo) {
        return proveedorDao.listarFiltrado(normalizarTexto(texto), normalizarTexto(pais), activo);
    }

    @Override
    public List<String> listarPaises() {
        return proveedorDao.listarPaises();
    }

    @Override
    public Proveedor buscarProveedorXId(Long id) {

        Proveedor proveedor = proveedorDao.findById(id).orElse(null);

        if (proveedor == null) {
            throw new ProveedorNoEncontradoException(id);
        }
        return proveedor;
    }

    @Override
    public Proveedor guardarProveedor(Proveedor proveedor) {
        validarCifRepetido(proveedor);

        if (proveedor.getId() == null) {
            proveedor.setCifHash(IndiceCiego.hash(proveedor.getCif()));
            return proveedorDao.save(proveedor);
        }

        Proveedor existente = buscarProveedorXId(proveedor.getId());

        copiarDatos(proveedor, existente);

        return proveedorDao.save(existente);
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private void validarCifRepetido(Proveedor proveedor) {
        String hash = IndiceCiego.hash(proveedor.getCif());
        boolean repetido;

        if (proveedor.getId() == null) {
            repetido = proveedorDao.existsByCifHash(hash);
        } else {
            repetido = proveedorDao.existsByCifHashAndIdNot(hash, proveedor.getId());
        }

        if (repetido) {
            throw new CifDuplicadoException();
        }
    }

    private void copiarDatos(Proveedor origen, Proveedor destino) {
        destino.setNombreComercial(origen.getNombreComercial());
        destino.setRazonSocial(origen.getRazonSocial());
        destino.setTipo(origen.getTipo());
        destino.setCif(origen.getCif());
        destino.setCifHash(IndiceCiego.hash(origen.getCif()));
        destino.setIban(origen.getIban());
        destino.setEmail(origen.getEmail());
        destino.setTelefono(origen.getTelefono());
        destino.setPais(origen.getPais());
        destino.setActivo(origen.isActivo());
    }

    @Override
    public void eliminarProveedor(Long id) {

        Proveedor proveedor = buscarProveedorXId(id);

        if (proveedor.getCuenta() != null) {
            throw new ProveedorConCuentaException();
        }

        try {
            proveedorDao.delete(proveedor);

        } catch (DataIntegrityViolationException e) {
            throw new ProveedorConReferenciasException();
        }
    }
}

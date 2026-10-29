
package service.implement;

import config.IndiceCiego;
import dao.ProveedorDao;
import dto.ProveedorFormulario;
import exception.CifDuplicadoException;
import exception.ProveedorNoEncontradoException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.Proveedor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ProveedorService;;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService{
    
    private final ProveedorDao proveedorDao;
    
    @Override
    public List<Proveedor> listar() {
        return proveedorDao.findAllByOrderByNombreComercialAsc();
    }

    @Override
    public ProveedorFormulario buscarFormulario(Long id) {
        Proveedor proveedor = proveedorDao.findById(id).orElseThrow(() -> new ProveedorNoEncontradoException(id));
        return aFormulario(proveedor);
    }
    
    @Override
    public ProveedorFormulario aFormulario(Proveedor p) {
        ProveedorFormulario f = new ProveedorFormulario();
        
        f.setId(p.getId());
        f.setNombreComercial(p.getNombreComercial());
        f.setRazonSocial(p.getRazonSocial());
        f.setTipo(p.getTipo());
        f.setCif(p.getCif());
        f.setIban(p.getIban());
        f.setEmail(p.getEmail());
        f.setTelefono(p.getTelefono());
        f.setPais(p.getPais());
        f.setActivo(p.isActivo());
        return f;
    }
   
    
    @Override
    @Transactional
    public void guardarProveedor(ProveedorFormulario formulario) {
        String hash = IndiceCiego.hash(formulario.getCif());
        
        boolean duplicado = formulario.getId() == null ? proveedorDao.existsByCifHash(hash) : proveedorDao.existsByCifHashAndIdNot(hash,formulario.getId());
        
        if(duplicado){
            throw new CifDuplicadoException();
        }
        Proveedor proveedor = formulario.getId() == null ? new Proveedor() : proveedorDao.findById(formulario.getId()).orElseThrow(() -> new ProveedorNoEncontradoException(formulario.getId()));
    
        proveedor.setNombreComercial(formulario.getNombreComercial());
        proveedor.setRazonSocial(formulario.getRazonSocial());
        proveedor.setTipo(formulario.getTipo());
        proveedor.setCif(formulario.getCif());
        proveedor.setCifHash(hash);
        proveedor.setIban(formulario.getIban());
        proveedor.setEmail(formulario.getEmail());
        proveedor.setTelefono(formulario.getTelefono());
        proveedor.setPais(formulario.getPais());
        proveedor.setActivo(formulario.isActivo());
        
        proveedorDao.save(proveedor);
    }   

    @Override
    @Transactional
    public void cambiarActivo(Long id, boolean activo) {
        Proveedor proveedor = proveedorDao.findById(id).orElseThrow(() -> new ProveedorNoEncontradoException(id));
        proveedor.setActivo(activo);
    }
}


package service.implement;

import dao.ProveedorDao;
import dto.ProveedorFormulario;
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
    }
   
    
    @Override
    public void guardarProveedor(ProveedorFormulario formulario) {
    }

    @Override
    public void cambiarActivo(Long id, boolean activo) {
    }
}

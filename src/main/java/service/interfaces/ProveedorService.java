package service.interfaces;

import dto.ProveedorFormulario;
import java.util.List;
import model.aprovisionamiento.Proveedor;

public interface ProveedorService {
    List<Proveedor> listar();
    ProveedorFormulario buscarFormulario(Long id);
    ProveedorFormulario aFormulario(Proveedor p);
    void guardarProveedor(ProveedorFormulario formulario);
    void cambiarActivo(Long id, boolean activo);
    
}

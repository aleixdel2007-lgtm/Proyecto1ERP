package service.interfaces;

import java.util.List;
import model.aprovisionamiento.Proveedor;

public interface ProveedorService {

    List<Proveedor> listarProveedores(String texto, String pais, Boolean activo);

    List<String> listarPaises();

    Proveedor buscarProveedorXId(Long id);

    Proveedor guardarProveedor(Proveedor proveedor);

    void eliminarProveedor(Long id);
}

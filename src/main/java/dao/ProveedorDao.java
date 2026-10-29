package dao;

import model.aprovisionamiento.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorDao extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findAllByOrderByNombreComercialAsc();

    boolean existsByCifHash(String cifHash);

    boolean existsByCifHashAndIdNot(String cifHash, Long id);
}

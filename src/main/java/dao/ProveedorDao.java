package dao;

import model.aprovisionamiento.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProveedorDao extends JpaRepository<Proveedor, Long> {

    // Lista los proveedores saltandose cada filtro que llegue a null. El CIF no se puede
    // buscar porque esta cifrado y en la base no queda texto que comparar
    @Query("select p from Proveedor p "
            + "where (:texto is null "
            + "       or lower(p.nombreComercial) like lower(concat('%', :texto, '%')) "
            + "       or lower(p.razonSocial) like lower(concat('%', :texto, '%'))) "
            + "and (:pais is null or p.pais = :pais) "
            + "and (:activo is null or p.activo = :activo) "
            + "order by p.nombreComercial")
    List<Proveedor> listarFiltrado(@Param("texto") String texto,
                                   @Param("pais") String pais,
                                   @Param("activo") Boolean activo);

    // Los paises que de verdad hay guardados, para llenar el desplegable del filtro
    @Query("select distinct p.pais from Proveedor p where p.pais is not null order by p.pais")
    List<String> listarPaises();

    boolean existsByCifHash(String cifHash);

    boolean existsByCifHashAndIdNot(String cifHash, Long id);
}

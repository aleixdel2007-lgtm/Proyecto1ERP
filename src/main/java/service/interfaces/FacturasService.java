package service.interfaces;
import java.util.List;
import model.ventas.Factura;
public interface FacturasService {

    List<Factura> listarFiltrado(String texto);

    byte[] generarPdf(Long facturaId);
}

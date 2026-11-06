package service.implement;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.FacturasDAO;
import exception.factura.FacturaNoEncontradaException;
import model.ventas.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import service.interfaces.FacturasService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class FacturaServiceImpl implements FacturasService {
    @Autowired
    private FacturasDAO facturasDAO;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public List<Factura> listarFiltrado(String texto) {
        return facturasDAO.listarFiltrado(texto);
    }

    @Override
    public byte[] generarPdf(Long facturaId) {

        Factura factura = facturasDAO.buscarConDetalle(facturaId).orElse(null);

        if (factura == null) {
            throw new FacturaNoEncontradaException(facturaId);
        }

        Context contexto = new Context();
        contexto.setVariable("factura", factura);

        String html = templateEngine.process("ventas/facturaPdf", contexto);

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfRendererBuilder constructor = new PdfRendererBuilder();
            constructor.useFastMode();
            constructor.withHtmlContent(html, null);
            constructor.toStream(salida);
            constructor.run();
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", e);
        }
    }
}

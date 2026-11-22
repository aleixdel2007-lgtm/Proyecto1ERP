package service.implement;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;
import service.interfaces.PdfService;

@Service
public class PdfServiceImpl implements PdfService {

    // El PDF se arma entero en memoria, que para un documento de una o dos hojas sobra
    @Override
    public byte[] desdeHtml(String html) {

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfRendererBuilder constructor = new PdfRendererBuilder();
            constructor.useFastMode();
            constructor.withHtmlContent(html, null);
            constructor.toStream(salida);
            constructor.run();
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
    }
}

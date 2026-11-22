package service.interfaces;

public interface PdfService {

    // Convierte un HTML ya renderizado en los bytes de un PDF; lo comparten la factura y el albaran
    byte[] desdeHtml(String html);
}

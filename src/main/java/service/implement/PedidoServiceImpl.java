package service.implement;

import dao.PedidoDao;
import exception.PedidoNoEncontradoException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.ventas.EstadoPedido;
import model.ventas.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.interfaces.PedidoService;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoDao pedidoDao;

    @Override
    public List<Pedido> listarPedidos(String texto, EstadoPedido estado, LocalDate desde, LocalDate hasta) {

        LocalDateTime inicio = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2999, 1, 1, 0, 0);

        if (desde != null) {
            inicio = desde.atStartOfDay();
        }
        if (hasta != null) {
            fin = hasta.plusDays(1).atStartOfDay();
        }

        return pedidoDao.listarFiltrado(normalizarTexto(texto), estado, inicio, fin);
    }

    @Override
    public Pedido buscarPedidoXId(Long id) {

        Pedido pedido = pedidoDao.buscarConDetalle(id).orElse(null);

        if (pedido == null) {
            throw new PedidoNoEncontradoException(id);
        }
        return pedido;
    }

    @Override
    public long contarPorEstado(EstadoPedido estado) {
        return pedidoDao.countByEstado(estado);
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}

package service.implement;

import dao.PedidoDao;
import exception.cesta.CestaVaciaException;
import exception.cesta.VideojuegoNoDisponibleException;
import exception.pedido.PedidoNoEncontradoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.catalogo.Videojuego;
import model.ventas.Cesta;
import model.ventas.EstadoPedido;
import model.ventas.LineaCesta;
import model.ventas.LineaPedido;
import model.ventas.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.CestaService;
import service.interfaces.PedidoService;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoDao pedidoDao;

    @Autowired
    private CestaService cestaService;

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

    @Override
    @Transactional
    public Pedido crearPedidoDesdeCesta(String email) {

        Cesta cesta = cestaService.buscarOCrearCesta(email);

        if (cesta.getLineas().isEmpty()) {
            throw new CestaVaciaException();
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cesta.getCliente());
        pedido.setNumero(generarNumero());

        BigDecimal total = BigDecimal.ZERO;

        for (LineaCesta lineaCesta : cesta.getLineas()) {
            Videojuego videojuego = lineaCesta.getVideojuego();

            if (!videojuego.isActivo()) {
                throw new VideojuegoNoDisponibleException();
            }

            // el precio se copia ahora: si luego cambia en el catalogo, el pedido no se altera
            LineaPedido linea = new LineaPedido();
            linea.setPedido(pedido);
            linea.setVideojuego(videojuego);
            linea.setPrecioUnitario(videojuego.getPrecio());
            linea.setDescuento(calcularDescuento(videojuego));
            pedido.getLineas().add(linea);

            total = total.add(linea.getPrecioUnitario().subtract(linea.getDescuento()));
        }

        // el precio ya lleva el IVA (21%) incluido: la base sale de dividir entre 1,21
        BigDecimal base = total.divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_UP);
        pedido.setTotal(total);
        pedido.setBaseImponible(base);
        pedido.setIva(total.subtract(base));

        return pedidoDao.save(pedido);
    }

    // PED-2026-00001, PED-2026-00002...
    private String generarNumero() {
        long siguiente = pedidoDao.count() + 1;
        return String.format("PED-%d-%05d", java.time.LocalDate.now().getYear(), siguiente);
    }

    // lo que se ha rebajado: precio normal menos precio de oferta (0 si no hay oferta)
    private BigDecimal calcularDescuento(Videojuego videojuego) {

        if (videojuego.getPrecioDescuento() == null) {
            return BigDecimal.ZERO;
        }
        return videojuego.getPrecio().subtract(videojuego.getPrecioDescuento());
    }

}

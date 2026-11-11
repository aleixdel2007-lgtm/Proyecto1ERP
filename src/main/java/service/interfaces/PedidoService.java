package service.interfaces;

import java.time.LocalDate;
import java.util.List;
import model.ventas.EstadoPedido;
import model.ventas.Pedido;

public interface PedidoService {

    List<Pedido> listarPedidos(String texto, EstadoPedido estado, LocalDate desde, LocalDate hasta);

    Pedido buscarPedidoXId(Long id);

    long contarPorEstado(EstadoPedido estado);
    
    Pedido crearPedidoDesdeCesta(String email);
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.CestaDao;
import dao.ClaveLicenciaDao;
import dao.ClienteDao;
import dao.VideojuegoDao;
import exception.cesta.JuegoNoComprableException;
import exception.cesta.LineaCestaNoEncontradaException;
import exception.cesta.VideojuegoNoDisponibleException;
import exception.usuario.UsuarioNoEncontradoException;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.math.BigDecimal;
import model.catalogo.Videojuego;
import model.seguridad.Cliente;
import model.ventas.Cesta;
import model.ventas.LineaCesta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.CestaService;

/**
 *
 * @author Sandy
 */
@Service
@Transactional(readOnly = true)
public class CestaServiceImpl implements CestaService {

    @Autowired
    private CestaDao cestaDao;

    @Autowired
    private ClienteDao clienteDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private ClaveLicenciaDao claveLicenciaDao; //para usar el metodo de estaEnlaBiblio y saber si el cliente ya lo tiene

    @Override
    @Transactional
    public void anadirVideojuego(String email, Long videojuegoId) {

        Cesta cesta = buscarOCrearCesta(email);
        Videojuego videojuego = buscarVideojuegoDisponible(videojuegoId);

        // si no se puede comprar, lanza un error con el motivo y aqui se para
        comprobarQueSePuedeComprar(email, cesta, videojuego);

        // como mucho 1 unidad de cada juego: siempre se crea una linea nueva
        LineaCesta linea = new LineaCesta();
        linea.setCesta(cesta);
        linea.setVideojuego(videojuego);
        linea.setCantidad(1); //siempre sera 1 pq lo que compra  es una licencia, una clave por cuenta,no un producto fisico, nucna se suma otra unidad del mismo juego
        cesta.getLineas().add(linea);

        cestaDao.save(cesta);
    }

    @Override
    public Cesta buscarOCrearCesta(String email) {

        Cesta cesta = cestaDao.buscarConLineasXEmail(email).orElse(null);

        if (cesta == null) {
            // no tiene cesta: la creo en memoria, sin guardar
            Cliente cliente = clienteDao.findByEmail(email).orElse(null);

            if (cliente == null) {
                throw new UsuarioNoEncontradoException(email);
            }

            cesta = new Cesta();
            cesta.setCliente(cliente);
        }
        return cesta;
    }

    @Override
    @Transactional
    public void quitarLinea(String email, Long lineaId) {

        Cesta cesta = buscarOCrearCesta(email);
        // solo busca entre las lineas de ESTA cesta, asi nadie toca las de otro cliente
        LineaCesta linea = buscarLineaXId(cesta, lineaId);

        cesta.getLineas().remove(linea);
        cestaDao.save(cesta);
    }

    @Override
    @Transactional
    public void vaciarCesta(String email) {

        Cesta cesta = cestaDao.findByClienteEmail(email).orElse(null);

        // si no tiene cesta no hay nada que vaciar
        if (cesta == null) {
            return;
        }

        cesta.getLineas().clear();
        cestaDao.save(cesta);
    }

    @Override
    public BigDecimal calcularTotal(Cesta cesta) {

        BigDecimal total = BigDecimal.ZERO;

        for (LineaCesta linea : cesta.getLineas()) {
            total = total.add(precioAPagar(linea.getVideojuego()));
        }
        return total;
    }

    // ---------- metodos privados (solo se usan dentro de esta clase) ----------
    private Videojuego buscarVideojuegoDisponible(Long id) {

        Videojuego videojuego = videojuegoDao.findById(id).orElse(null);

        if (videojuego == null) {
            throw new VideojuegoNoEncontradoException(id);
        }
        if (!videojuego.isActivo()) {
            throw new VideojuegoNoDisponibleException();
        }
        return videojuego;
    }

    // las 3 reglas para poder meter un juego en la cesta
    private void comprobarQueSePuedeComprar(String email, Cesta cesta, Videojuego videojuego) {

        if (precioAPagar(videojuego).signum() == 0) {
            throw new JuegoNoComprableException("Los juegos gratis se descargan directamente, no van a la cesta");
        }
        if (claveLicenciaDao.estaEnBiblioteca(email, videojuego.getId())) {
            throw new JuegoNoComprableException("Ya tienes este juego en tu biblioteca");
        }
        if (buscarLineaXVideojuego(cesta, videojuego.getId()) != null) {
            throw new JuegoNoComprableException("Este juego ya está en tu cesta");
        }
    }

    // el precio que paga el cliente: el de oferta si tiene, y si no el normal
    private BigDecimal precioAPagar(Videojuego videojuego) {

        if (videojuego.getPrecioDescuento() != null) {
            return videojuego.getPrecioDescuento();
        }
        return videojuego.getPrecio();
    }

    // devuelve la linea de la cesta que tiene ese videojuego, o null si no esta
    private LineaCesta buscarLineaXVideojuego(Cesta cesta, Long videojuegoId) {

        for (LineaCesta linea : cesta.getLineas()) {
            if (linea.getVideojuego().getId().equals(videojuegoId)) {
                return linea;
            }
        }
        return null;
    }

    // devuelve la linea con ese id, buscando solo dentro de esta cesta
    private LineaCesta buscarLineaXId(Cesta cesta, Long lineaId) {

        for (LineaCesta linea : cesta.getLineas()) {
            if (linea.getId().equals(lineaId)) {
                return linea;
            }
        }
        throw new LineaCestaNoEncontradaException(lineaId);
    }

}

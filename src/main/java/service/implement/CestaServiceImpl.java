/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.CestaDao;
import dao.ClienteDao;
import dao.VideojuegoDao;
import exception.cesta.CantidadInvalidaException;
import exception.cesta.LineaCestaNoEncontradaException;
import exception.cesta.VideojuegoNoDisponibleException;
import exception.usuario.UsuarioNoEncontradoException;
import exception.videojuego.VideojuegoNoEncontradoException;
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

    @Override
    @Transactional
    public void anadirVideojuego(String email, Long videojuegoId) {

        Cesta cesta = buscarOCrearCesta(email);
        Videojuego videojuego = buscarVideojuegoDisponible(videojuegoId);
        LineaCesta linea = buscarLineaXVideojuego(cesta, videojuegoId);

        if (linea == null) {
            // el juego no estaba en la cesta: creo la linea
            linea = new LineaCesta();
            linea.setCesta(cesta);
            linea.setVideojuego(videojuego);
            linea.setCantidad(1); //para añadir de uno en uno
            cesta.getLineas().add(linea);
        } else {
            // ya estaba: le sumo uno
            linea.setCantidad(linea.getCantidad() + 1);
        }

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
    public void cambiarCantidad(String email, Long lineaId, int cantidad) {

        if (cantidad < 1) {
            throw new CantidadInvalidaException();
        }

        Cesta cesta = buscarOCrearCesta(email);
        // solo busca entre las lineas de ESTA cesta, asi nadie toca las de otro cliente
        LineaCesta linea = buscarLineaXId(cesta, lineaId);

        linea.setCantidad(cantidad);
        cestaDao.save(cesta);
    }

    @Override
    @Transactional
    public void quitarLinea(String email, Long lineaId) {

        Cesta cesta = buscarOCrearCesta(email);
        LineaCesta linea = buscarLineaXId(cesta, lineaId);

        // al sacarla de la lista
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

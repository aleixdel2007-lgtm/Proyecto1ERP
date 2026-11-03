/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.time.LocalDate;
import java.util.List;
import model.seguridad.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import service.interfaces.ClienteService;

/**
 *
 * @author Sandy
 */
@Controller
public class ControllerCliente {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/clientes")
    public String listarClientes(@RequestParam(required = false) String buscar,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            Model model) {
        model.addAttribute("clientes", clienteService.buscarFiltrado(buscar, pais, desde));
        model.addAttribute("paises", clienteService.listarPaises());
        model.addAttribute("buscar", buscar);
        model.addAttribute("pais", pais);
        model.addAttribute("desde", desde);
        return "gestorCliente/listadoClientes";
    }

}

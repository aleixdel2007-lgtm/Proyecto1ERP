/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.util.List;
import model.seguridad.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String listarClientes(@RequestParam(required = false) String buscar, Model model) {
        List<Cliente> lista = clienteService.buscarFiltrado(buscar);
        model.addAttribute("clientes", lista);
        model.addAttribute("buscar", buscar);
        return "gestorCliente/listadoClientes";
    }

}

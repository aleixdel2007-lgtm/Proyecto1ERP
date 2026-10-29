/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service.interfaces;

import java.util.List;
import model.seguridad.Departamento;

/**
 *
 * @author Sandy
 */
public interface DepartamentoService {

    List<Departamento> listarDepartamentos(); //leer 

    Departamento buscarDepartamentoXId(Long id); //buscar

    Departamento guardarDepartamento(Departamento departamento); //save y update de uno ya existente

    void eliminarDepartamento(Long id); // delete 
}

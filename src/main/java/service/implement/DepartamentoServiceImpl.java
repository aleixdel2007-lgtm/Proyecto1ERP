/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.DepartamentoDAO;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import model.seguridad.Departamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.DepartamentoService;

/**
 *
 * @author Sandy
 */
@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    @Autowired
    private DepartamentoDAO departamentoDAO;

    @Override
    public List<Departamento> listarDepartamentos() {
        return departamentoDAO.findAll();
    }

    @Override
    public Departamento buscarDepartamentoXId(Long id) {

        Departamento departamento = departamentoDAO.findById(id).orElse(null);

        if (departamento == null) {
            throw new EntityNotFoundException("El departamento no existe");
        }
        return departamento;
    }

    @Override
    public Departamento guardarDepartamento(Departamento departamento) { //para modificar tambien 
        if (departamento.getNombre() == null || departamento.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (departamento.getNombre().length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 80 caracteres");
        }

        List<Departamento> departamentos = departamentoDAO.findAll(); //los listo todos

        for (Departamento dep : departamentos) {

            if (dep.getNombre().equalsIgnoreCase(departamento.getNombre()) //comparo por nombre y por id 
                    && !dep.getId().equals(departamento.getId())) { //si el nombre es igual, pero el id no es igual no se puede pq ya existiria
                throw new IllegalArgumentException("Ya existe un departamento con ese nombre");
            }

        }
        return departamentoDAO.save(departamento);
    }

    @Transactional
    @Override
    public void eliminarDepartamento(Long id) { //delete 
        Departamento departamento = buscarDepartamentoXId(id); //lo busco por id

        if (!departamento.getTrabajadores().isEmpty()) { //si ese id tiene trabajadores, no se puede borrar
            throw new IllegalStateException(
                    "No se puede eliminar un departamento con trabajadores asociados"
            );

        }
        departamentoDAO.delete(departamento);
    }
}

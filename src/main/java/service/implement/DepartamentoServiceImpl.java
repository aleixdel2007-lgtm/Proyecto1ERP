/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import dao.TrabajadorDao;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Trabajador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.DepartamentoService;
import dao.DepartamentoDao;

/**
 *
 * @author Sandy
 */
@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    @Autowired
    private DepartamentoDao departamentoDao;

    @Autowired
    private TrabajadorDao trabajadorDao;

    @Override
    public List<Departamento> listarDepartamentos() {
        return departamentoDao.findAll();
    }

    @Override
    public Departamento buscarDepartamentoXId(Long id) {

        Departamento departamento = departamentoDao.findById(id).orElse(null);

        if (departamento == null) {
            throw new EntityNotFoundException("El departamento no existe");
        }
        return departamento;
    }

    @Override
    public Departamento guardarDepartamento(Departamento departamento, Long responsableId) {

        validarNombre(departamento);
        validarNombreRepetido(departamento);
        asignarResponsable(departamento, responsableId);

        return departamentoDao.save(departamento);
    }

    private void validarNombre(Departamento departamento) {

        if (departamento.getNombre() == null || departamento.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (departamento.getNombre().length() > 80) {
            throw new IllegalArgumentException("El nombre no puede superar los 80 caracteres");
        }
    }

    private void validarNombreRepetido(Departamento departamento) {

        List<Departamento> departamentos = departamentoDao.findAll();

        for (Departamento dep : departamentos) {

            if (dep.getNombre().equalsIgnoreCase(departamento.getNombre())
                    && !dep.getId().equals(departamento.getId())) {

                throw new IllegalArgumentException("Ya existe un departamento con ese nombre");
            }
        }
    }

    private void asignarResponsable(Departamento departamento, Long responsableId) {

        if (responsableId != null) {

            Trabajador responsable = trabajadorDao.findById(responsableId).orElse(null);

            if (responsable == null) {
                throw new IllegalArgumentException("El responsable no existe");
            }

            departamento.setResponsable(responsable);

        } else {
            departamento.setResponsable(null);
        }
    }

    @Override
    @Transactional
    public void eliminarDepartamento(Long id) {

        Departamento departamento = buscarDepartamentoXId(id);

        validarTrabajadoresAsociados(departamento);

        departamentoDao.delete(departamento);
    }

    private void validarTrabajadoresAsociados(Departamento departamento) {

        if (!departamento.getTrabajadores().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar un departamento con trabajadores asociados"
            );
        }
    }

    @Override
    public List<Trabajador> listarTrabajadores() { //para listar los responsables 
        return trabajadorDao.findAll();
    }
}

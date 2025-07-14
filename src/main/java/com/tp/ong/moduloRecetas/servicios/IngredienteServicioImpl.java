package com.tp.ong.moduloRecetas.servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tp.ong.moduloRecetas.accesoDatos.IIngredienteRepo;
import com.tp.ong.moduloRecetas.accesoDatos.IRecetaRepo;
import com.tp.ong.moduloRecetas.entidades.Ingrediente;
import com.tp.ong.moduloRecetas.entidades.Receta;

@Service
public class IngredienteServicioImpl implements IIngredienteServicio{
	
		
	//Inyectamos el Repositorio para usarlo luego en los metodos
	@Autowired
	private IIngredienteRepo repo;
	
	//Metodos para listar
	@Override
	public List<Ingrediente> listarTodosLosIngredientes() {
		// TODO Auto-generated method stub
		return repo.findAll();
	}
	
    @Override
    public Optional<Ingrediente> buscarIngredientePorId(Long id) {
        return repo.findById(id);
    }
	
	//Metodos para buscar por nombre o palabra
	@Override
	public Optional<Ingrediente> buscarPorNombreSinCaseSensitive(String nombre) {
		// TODO Auto-generated method stub
		return repo.findByNombreIgnoreCase(nombre);
	}	

	@Override
	public List<Ingrediente> buscarPorNombreContienePalabra(String palabra) {
		// TODO Auto-generated method stub
		return repo.findByNombreContainingIgnoreCase(palabra);
	}
	

}

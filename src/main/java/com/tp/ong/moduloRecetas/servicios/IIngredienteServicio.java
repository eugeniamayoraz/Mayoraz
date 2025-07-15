package com.tp.ong.moduloRecetas.servicios;

import java.util.List;
import java.util.Optional;

import com.tp.ong.moduloRecetas.entidades.Ingrediente;
import com.tp.ong.moduloRecetas.entidades.Receta;

public interface IIngredienteServicio {
			
		//Metodo para listar todos los ingredientes - se usa mucho en el controller
	
	public List<Ingrediente> listarTodosLosIngredientes();
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Metodos para Buscar por id, Nombre o palabra
	
	//public Optional<Ingrediente> buscarIngredientePorId(Long id);
	
	//public Optional<Ingrediente> buscarPorNombreSinCaseSensitive(String nombre);
	
	//public List<Ingrediente> buscarPorNombreContienePalabra(String palabra);
}

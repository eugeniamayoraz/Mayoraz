package com.tp.ong.moduloRecetas.accesoDatos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tp.ong.moduloRecetas.entidades.*;


public interface IIngredienteRepo extends JpaRepository<Ingrediente, Long> {
	
	Optional<Ingrediente> findByNombre(String nombre);
	
	Optional<Ingrediente> findByNombreIgnoreCase(String nombre);
	 
	List<Ingrediente> findByNombreContainingIgnoreCase(String nombre);
	
	

}
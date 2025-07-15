package com.tp.ong.moduloRecetas.accesoDatos;

import com.tp.ong.moduloRecetas.entidades.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRecetaRepo extends JpaRepository<Receta, Long> {

// No pones nada si vas a usar los metodos basicos, podes escribir metodos personalizados si lo necesitas.-
	
//----Metodos de Busqueda de una receta

    // Con esto busco por nombre, ignoro may y minusculas, y pido que NO esté eliminada lógicamente.
    //lo uso para la validacion de nombre existente
	Optional<Receta> findByNombreIgnoreCaseAndDeletedFalse(String nombre);

    // Metodo para buscar Receta por ID lo uso para la eliminacion logica.
    // obtengo una receta por ID, solo si NO está eliminada lógicamente.
    // JpaRepository ya nos da un findById(ID), pero aca personalizo para el borrado lógico.
    Optional<Receta> findByIdAndDeletedFalse(Long id);
    
    //lo uso para la carga de datos.-
    Optional<Receta> findByNombreAndDeletedFalse(String nombreReceta); 


// --- Métodos de Listado (siempre filtran por deleted = false) ---

    // Método para buscar todas las recetas que NO estén eliminadas lógicamente o sea las Activas
    List<Receta> findByDeletedFalse();
    
    // Método para buscar recetas cuyo nombre contenga una palabra, ignorando mayúsculas/minúsculas,
    // y que NO estén eliminadas lógicamente.
    List<Receta> findByNombreContainingIgnoreCaseAndDeletedFalse(String nombre);

    // MÉTODO: para buscar recetas por rango de calorías, y que NO estén eliminadas lógicamente.
    // Spring Data JPA lo traduce a algo como WHERE caloriasTotales BETWEEN ?1 AND ?2 AND deleted = false..
    List<Receta> findByCaloriasTotalesBetweenAndDeletedFalse(Integer minCalorias, Integer maxCalorias);
    
    
}
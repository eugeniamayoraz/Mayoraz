package com.tp.ong.moduloRecetas.accesoDatos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; 
       
import com.tp.ong.moduloRecetas.entidades.ItemReceta; //hay que importar la clase que esta mapeada con entity


//El primer parámetro de JpaRepository es la entidad/El segundo parámetro es el tipo de la clave primaria (por ej. Long).
public interface IItemRecetaRepo extends JpaRepository<ItemReceta, Long> {

 //Busca todos los ItemReceta asociados a una Receta específica por su ID --lo usa la carga de datos	
 List<ItemReceta> findByRecetaId(Long recetaId);

	
	
}

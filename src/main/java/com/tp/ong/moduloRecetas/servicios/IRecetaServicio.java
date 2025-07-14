package com.tp.ong.moduloRecetas.servicios;

import java.util.List;
import java.util.Optional;

import com.tp.ong.moduloRecetas.entidades.Receta;
import com.tp.ong.moduloRecetas.presentacion.Receta.RecetaFormDto;

public interface IRecetaServicio {
	
	//Aca nombro todos los metodos que va a tener Receta
	
	//----------------------------------------------Metodos para listar y filtrar
	public List<Receta> listarTodasLasRecetas();
	
	public Optional <Receta> buscarPorNombre(String nombre);
		
	public Optional<Receta> buscarPorNombreSinCaseSensitive(String nombre);
	
	public List<Receta> buscarPorNombreContienePalabra(String palabra);

	public List<Receta> buscarPorRangoDeCalorías(Integer minCalorias, Integer maxCalorias);
	
	
	//---------------------------------------------Metodos para Alta Baja Modificacion 
	
	
	           //Metodos para la validacion de que no puede haber dos recetas con el mismo nombre:
		
               //para validar si el nombre de la receta ya existe (para creación)
	public boolean existeRecetaConNombre(String nombre);

               //para validar si el nombre de la receta ya existe (para edición, excluyendo la propia receta)
    public boolean existeRecetaConNombreExcluyendoId(String nombre, Long idExcluido);

	           //Metodos Principales
    public Receta guardarReceta(Receta receta);
    
    public Optional<Receta> buscarRecetaPorId(Long id);
	
	public void borrarRecetaLogico(Long id);
	
	
	          // Metodos para Convertir DTO a Entidad y Viceversa
	
// ----------para flujo de datos hacia la base de datos (WRITE):DTO de entrada (frontend) hacia la Entidad de la base de datos.

	
	
	//Para crear receta desde el frontend y poder mandatla al backend (db) Recibe DTO devuelve Entidad Receta.-
	public Receta convertirDtoAEntidad(RecetaFormDto dto); 
	
	//Para Modificar Receta Existente En db - toma una entidad Receta que ya existe (la q traje de la base por su ID) 
	//y le aplica los cambios que vienen en el RecetaFormDto, en lugar de crear una entidad completamente nueva.
	//Función: Toma datos de un formulario y aplica esos cambios a una Receta que ya fue cargada de la base de datos.
    //La Uso: En guardarReceta cuando recetaFormDto.getId() != null.
	public void actualizarEntidadDesdeDto(Receta recetaExistente, RecetaFormDto dto);
	
	
//------------para flujo de datos desde la base de datos (READ): Entidad de la base de datos hacia el DTO de salida (frontend)	
	
	//para enviar datos desde el backend (base de datos) hacia el frontend, cdo no necesio mandar la entidad completa
	//para adaptar los datos a los campos específicos del formulario). En su lugar, usas convertirEntidadADto para 
	//transformar esa entidad Receta en un RecetaFormDto. y ese DTO es el que se envía a la plantilla Thymeleaf para
	//que los campos del formulario se pre-rellenen con los datos de la receta que se va a editar.
	public RecetaFormDto convertirEntidadADto(Receta receta);  
	
	
	

	
	
	//Metodo para Calcular Calorías: lo movi a la Entidad
	//public void calcularCaloriasTotales(Receta receta);
	
	 	  
}

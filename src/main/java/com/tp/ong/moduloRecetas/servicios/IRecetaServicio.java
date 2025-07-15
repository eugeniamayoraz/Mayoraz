package com.tp.ong.moduloRecetas.servicios;

import java.util.List;
import java.util.Optional;

import com.tp.ong.moduloRecetas.entidades.ItemReceta;
import com.tp.ong.moduloRecetas.entidades.Receta;
import com.tp.ong.moduloRecetas.presentacion.Receta.RecetaFormDto;

public interface IRecetaServicio {
	
	//Aca nombro todos los metodos que va a tener Receta
	
	//----------------------------------------------Metodos para Bucar listar y filtrar
		
	public Optional <Receta> buscarPorNombre(String nombre);
		
	public Optional<Receta> buscarPorNombreSinCaseSensitive(String nombre);
	
	public List<Receta> listarTodasLasRecetas();
	
	public List<Receta> buscarPorNombreContienePalabra(String palabra);

	public List<Receta> buscarPorRangoDeCalorías(Integer minCalorias, Integer maxCalorias);
	
	
	//---------------------------------------------Metodos para Alta Baja Modificacion 
	
	
	           //Metodos para la validacion de que no puede haber dos recetas con el mismo nombre:
		
               //para validar si el nombre de la receta ya existe (para creación)
	public boolean existeRecetaConNombre(String nombre);

               //para validar si el nombre de la receta ya existe (para edición, excluyendo la propia receta)
    public boolean existeRecetaConNombreExcluyendoId(String nombre, Long idExcluido);

	           //Metodos Principales
    
    public Optional<Receta> buscarRecetaPorId(Long id);// ---y no lo uso, uso el que sique.-
    
	public RecetaFormDto obtenerRecetaParaEdicion(Long id);// tiene codigo para la recuperacion logica de item 
	
    public List<ItemReceta> listarTodosLosItemsDeReceta(Long recetaId);
    
    public void borrarRecetaLogico(Long id);
	
    public Receta guardarReceta(Receta receta);//Basico ya no lo uso lo refactorice.-
    
    public Receta guardarReceta(RecetaFormDto formDto);//Agregado tiene codigo para eliminacion logica de items
    
    	
 	
	          // Metodos para Convertir DTO a Entidad y Viceversa
		
	
//------------para flujo de datos DESDE la base de datos (READ): Entidad de la base de datos hacia el DTO de salida (frontend)	
	
	//para enviar datos desde el backend (base de datos) hacia el frontend, cdo no necesio mandar la entidad completa
	//para adaptar los datos a los campos específicos del formulario uso convertirEntidadADto para 
	//transformar esa entidad Receta en un RecetaFormDto. y ese DTO es el que se envía a la plantilla Thymeleaf para
	//que los campos del formulario se pre-rellenen con los datos de la receta que se va a editar.
	public RecetaFormDto convertirEntidadADto(Receta receta); 	
	
	
// ----------para flujo de datos HACIA la base de datos (WRITE):DTO de entrada (frontend) hacia la Entidad de la base de datos.

		
	//Para crear receta desde el frontend y poder mandatla al backend (db) Recibe DTO devuelve Entidad Receta.-
	public Receta convertirDtoAEntidad(RecetaFormDto dto); 
	
	//Para Modificar Receta Existente En db - toma una entidad Receta que ya existe (la q traje de la base por su ID) 
	//y le aplica los cambios que vienen en el RecetaFormDto, o sea Toma los datos de un formulario y aplica esos cambios a una Receta que ya fue cargada de la base de datos.
    //La Uso: En guardarReceta cuando recetaFormDto.getId() != null.
	public void actualizarEntidadDesdeDto(Receta recetaExistente, RecetaFormDto dto);
	
			
	//Metodo para Calcular Calorías: lo movi a la Entidad
	//public void calcularCaloriasTotales(Receta receta);
	
	 	  
}

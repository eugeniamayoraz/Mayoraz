package com.tp.ong.moduloRecetas.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importo esto para @Transactional

//import com.tp.ong.moduloRecetas.Exceptions.ValidationException;
import com.tp.ong.moduloRecetas.accesoDatos.*;
import com.tp.ong.moduloRecetas.entidades.*;

// DTOs
import com.tp.ong.moduloRecetas.presentacion.Receta.RecetaFormDto;
import com.tp.ong.moduloRecetas.presentacion.Receta.ItemRecetaDto;

import java.util.stream.Collectors; // Para usar streams ???? (Sí, para DTOs y procesamiento de listas)

@Service
public class RecetaServicioImpl implements IRecetaServicio {

    // Inyecto el Repositorio de Recetas
    @Autowired
    private IRecetaRepo recetaRepo; // Cambié el nombre de 'repo' a 'recetaRepo' para distinguirlo de otros repos

    // Inyecto el Repositorio de Ingredientes para poder buscar los ingredientes por ID
    @Autowired
    private IIngredienteRepo ingredienteRepo; // 


    /*
        No necesito inyectar explícitamente IItemRecetaRepo en RecetaServicioImpl para la mayoría de las operaciones
        de alta, baja y modificación de Receta en relacción a sus ItemReceta ya que estan asociados a la entidad Receta,
        con la configuración:
		@OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
		private List<ItemReceta> items;

		-CascadeType.ALL: Indica que todas las operaciones de persistencia (Guardar, Actualizar, Borrar, etc.) 
		en Receta se "cascadearán" (aplicarán) automáticamente a sus entidades ItemReceta asociadas.
		-Cuando guardo una Receta nueva o editada -repo.save(receta), si esa Receta tiene una lista de ItemReceta, y JPA 
		(Hibernate en el fondo) automáticamente persiste tb esos ItemReceta. No necesito llamar a itemRecetaRepo.save(). 

		-orphanRemoval = true: hace q si un ItemReceta se desvincula de su Receta contenedora (x ej., lo eliminas de la lista receta.getItems()),
		 JPA lo tratará como una entidad "huérfana" y lo eliminará automáticamente de la base de datos.

     */


    // Constructor vacío no hace falta ya que spring Spring crea la instancia sin un constructor explícito. 
    //public RecetaServicioImpl() {
    //}

    //--------------------------- Metodos Relacionados con DB para BÚSQUEDA - 
    //@Transactional(readOnly = true) le da mejor rendimiento) Mas que nada los renombro para invocarlos mas intuitivamente.-
    
    
    @Override
    @Transactional(readOnly = true)
    public List<Receta> listarTodasLasRecetas() {
        // llama al método del repositorio que lista solo las recetas NO eliminadas lógicamente
        return recetaRepo.findByDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Receta> buscarPorNombre(String nombre) {
        // busca por nombre exacto (case-sensitive) y NO eliminadas
        return recetaRepo.findByNombreAndDeletedFalse(nombre);
    }//No Lo uso pero lo dejo porque arrranque por aca y luego lo mejoré---------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<Receta> buscarPorNombreSinCaseSensitive(String nombre) {
        // busca ignorando May o Min (case Sensitive) y NO eliminadas logicamente
        return recetaRepo.findByNombreIgnoreCaseAndDeletedFalse(nombre);
    }//------------------------------------Al final no lo usé xq desde las validaciones se llama directamente al metodo del repo.

    @Override
    @Transactional(readOnly = true)
    public List<Receta> buscarPorNombreContienePalabra(String palabra) {
        //busca por contenido de palabra sin CaseSensitive y NO eliminadas logicamente
        return recetaRepo.findByNombreContainingIgnoreCaseAndDeletedFalse(palabra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receta> buscarPorRangoDeCalorías(Integer minCalorias, Integer maxCalorias) {
        // Ahora usamos el método del repositorio que busca por rango de calorías y NO eliminadas logicamente
        return recetaRepo.findByCaloriasTotalesBetweenAndDeletedFalse(minCalorias, maxCalorias);
    }
    
    
    // //---------------------------------------------Metodos para Alta Baja Modificacion 

    //---------------  MÉTODOS DE VALIDACIÓN DE NOMBRE

    //para receta nueva
    @Override
    @Transactional(readOnly = true)
    public boolean existeRecetaConNombre(String nombre) {
        // Usar el repositorio para buscar por nombre (ignorando mayúsculas/minúsculas)
        // Y verificar que NO esté eliminada lógicamente.
    	
        return recetaRepo.findByNombreIgnoreCaseAndDeletedFalse(nombre).isPresent();
    	//return buscarPorNombreSinCaseSensitive(nombre).isPresent(); ----------------------tb lo podia llamar asi
    }

    
    // sirve para el caso en que quieres editar una receta sin cambiarle el nombre ej editarle los ingredientes.-
    // si este metodo no estuviera, no pordira guardar los cambios en la receta porque ya existe una con ese nombre" por eso se la excluye de la busqueda.
    @Override
    @Transactional(readOnly = true)
    public boolean existeRecetaConNombreExcluyendoId(String nombre, Long idExcluido) {
        // Buscar una receta con el mismo nombre y que no esté eliminada lógicamente y que no sea la que se esta editando.
        // Luego verificar si su ID es diferente al ID excluido(el de la receta q se esta editando).   	
        Optional<Receta> RecetaConMismoNombreexisting = recetaRepo.findByNombreIgnoreCaseAndDeletedFalse(nombre);
        return RecetaConMismoNombreexisting.isPresent() && !RecetaConMismoNombreexisting.get().getId().equals(idExcluido); //Es esa receta diferente a la que yo estoy editando ahora?
        // Si es Diferente a la que estoy editando ahora es True (hay duplicado)
        // Si es igual a la que estoy edittando ahora (mismo ID) es false, no se considera duplicado porque apunta a la misma.-
    }

    //------------------  METODOS BASICOS:


    // 1. Método para guardar o actualizar una Receta (usado por el controlador)
    @Override
    @Transactional
    public Receta guardarReceta(Receta receta) {
        // La validación de nombre único se hace ANTES de llamar a este método desde el controlador.
        // hay q asociar cada item receta a la referencia de la Receta Contenedora, importante para el cascade y orphanRemoval      
        if (receta.getItems() != null) {
            for (ItemReceta item : receta.getItems()) {
                item.setReceta(receta);
            }
        }
        receta.calcularCaloriasTotales(); // Llama al método de la entidad para calcular calorías antes de guardar
        return recetaRepo.save(receta);
    }


    // 2. Método para buscar una Receta por ID (necesario para la edición)
    @Override
    @Transactional(readOnly = true)
    public Optional<Receta> buscarRecetaPorId(Long id) {
        // Ahora usamos el método del repositorio que busca por ID solo las recetas NO eliminadas lógicamente.
        return recetaRepo.findByIdAndDeletedFalse(id);
    }

    // 3. Método para borrado lógico de una Receta
    @Override
    @Transactional
    public void borrarRecetaLogico(Long id) {
        // Buscamos la receta por ID, asegurándonos de que existe y NO está ya eliminada lógicamente.
        Optional<Receta> optionalReceta = recetaRepo.findByIdAndDeletedFalse(id); // Usa el método que filtra por deleted = false
        if (optionalReceta.isPresent()) {
            Receta receta = optionalReceta.get();
            receta.setDeleted(true); // Marca la receta como eliminada lógicamente
            recetaRepo.save(receta); // Guarda el cambio
            System.out.println("Receta con ID " + id + " marcada como eliminada lógicamente.");
        } else {
            // Manejo de error si la receta no se encuentra o si ya estaba eliminada lógicamente
            throw new RuntimeException("Receta no encontrada o ya eliminada lógicamente con ID: " + id);
        }
    }

    // -------------------------------------- MÉTODOS DE CONVERSIÓN DTO <-> ENTIDAD -----------------------------
    // Estos métodos serán `public` para que el controlador pueda llamarlos.

    /**
     * Convierte un RecetaFormDto a una entidad Receta.
     * se usa para CREAR nuevas recetas y que luego puedan ser guardadas.
     * @param dto El DTO de entrada con los datos del formulario.
     * @return La entidad Receta creada.
     */
 
    @Override
  //PARA TOMAR LOS DATOS DEL HTML Y CONVERTIRLOS EN UNA ENTIDAD PERSISTENTE.-(Receta)
  public Receta convertirDtoAEntidad(RecetaFormDto dto) {
      Receta receta = new Receta();
      // El ID no se establece aquí para una nueva receta, lo genera la DB
      receta.setNombre(dto.getNombre());
      receta.setDescripcion(dto.getDescripcion());
      receta.setDeleted(false); // Por defecto, una nueva receta NO está eliminada lógicamente

      // Convertir la lista de ItemRecetaDto a List<ItemReceta>
      if (dto.getItems() != null && !dto.getItems().isEmpty()) {
          List<ItemReceta> items = new ArrayList<>();
          for (ItemRecetaDto itemDto : dto.getItems()) {
              ItemReceta item = new ItemReceta();
              // El ID del ItemReceta no se establece aquí para un nuevo item, lo genera la DB
              item.setCantidad(itemDto.getCantidad());
              item.setCalorias(itemDto.getCalorias()); // Las calorías ingresadas manualmente

              // Buscar el Ingrediente por ID usando el ingredienteRepo inyectado
              Optional<Ingrediente> optionalIngrediente = ingredienteRepo.findById(itemDto.getIngredienteId());
              if (optionalIngrediente.isPresent()) {
                  item.setIngrediente(optionalIngrediente.get());
              } else {
                  throw new IllegalArgumentException("Ingrediente con ID " + itemDto.getIngredienteId() + " no encontrado.");
              }

              item.setReceta(receta); // ¡CRUCIAL para la relación bidireccional y el cascade!
              items.add(item);
          }
          receta.setItems(items);
      }

      return receta;
  }

    /**
     * Convierte una entidad Receta a un RecetaFormDto.
     * Utilizado para CARGAR los datos de una receta existente en el formulario de edición.
     * @param receta La entidad Receta a convertir.
     * @return El RecetaFormDto.
     */
    @Override
    public RecetaFormDto convertirEntidadADto(Receta receta) {
        RecetaFormDto dto = new RecetaFormDto();
        dto.setId(receta.getId());
        dto.setNombre(receta.getNombre());
        dto.setDescripcion(receta.getDescripcion());

        // Convertir la lista de ItemReceta a List<ItemRecetaDto>
        if (receta.getItems() != null && !receta.getItems().isEmpty()) {
            List<ItemRecetaDto> itemDtos = new ArrayList<>();
            for (ItemReceta item : receta.getItems()) {
                ItemRecetaDto itemDto = new ItemRecetaDto();
                itemDto.setId(item.getId()); // Si el item ya tiene ID
                itemDto.setCantidad(item.getCantidad());
                itemDto.setCalorias(item.getCalorias());
                if (item.getIngrediente() != null) {
                    itemDto.setIngredienteId(item.getIngrediente().getId());
                }
                itemDtos.add(itemDto);
            }
            dto.setItems(itemDtos);
        }

        return dto;
    }


    /**
     * Actualiza una entidad Receta existente con los datos de un RecetaFormDto.
     * Utilizado para PARA GUARDAR los cambios en la modificacion de Receta Existente
     * @param recetaExistente La entidad Receta que se va a actualizar.
     * @param dto El DTO con los nuevos datos.
     */
    @Override
    public void actualizarEntidadDesdeDto(Receta recetaExistente, RecetaFormDto dto) {
        recetaExistente.setNombre(dto.getNombre());
        recetaExistente.setDescripcion(dto.getDescripcion());

        // ** Lógica para actualizar la lista de ItemReceta **
        // Esta es la parte más delicada debido a la relación @OneToMany con orphanRemoval=true.
        // La estrategia que propongo aquí es robusta para agregar, actualizar y eliminar ítems.

        // Mapeo de items existentes por su ID para búsqueda rápida
        List<ItemReceta> existentes = recetaExistente.getItems();
        List<ItemReceta> updatedItems = new ArrayList<>();
        List<ItemReceta> itemsAEliminar = new ArrayList<>();

        if (dto.getItems() != null) {
            for (ItemRecetaDto itemDto : dto.getItems()) {
                ItemReceta itemExistente = null;

                if (itemDto.getId() != null) {
                    for (ItemReceta existente : existentes) {
                        if (itemDto.getId().equals(existente.getId())) {
                            itemExistente = existente;
                            break;
                        }
                    }
                }

                ItemReceta item;
                if (itemExistente != null) {
                    item = itemExistente;
                    item.setCantidad(itemDto.getCantidad());
                    item.setCalorias(itemDto.getCalorias());
                    // Si permitís cambiar el ingrediente, hacelo acá.
                    // Ejemplo:
                    // Ingrediente nuevo = ingredienteRepo.findById(...).orElseThrow(...)
                    // item.setIngrediente(nuevo);
                    existentes.remove(itemExistente); // Ya no se elimina
                } else {
                    item = new ItemReceta();
                    item.setCantidad(itemDto.getCantidad());
                    item.setCalorias(itemDto.getCalorias());

                    // Buscar el Ingrediente para el nuevo ítem
                    Optional<Ingrediente> optionalIngrediente = ingredienteRepo.findById(itemDto.getIngredienteId());
                    if (optionalIngrediente.isPresent()) {
                        item.setIngrediente(optionalIngrediente.get());
                    } else {
                        throw new IllegalArgumentException("Ingrediente con ID " + itemDto.getIngredienteId() + " no encontrado para item de receta.");
                    }
                }

                item.setReceta(recetaExistente); // Asegurar la relación bidireccional
                updatedItems.add(item);
            }
        }

        // Los que quedaron en `existentes` no fueron usados → se eliminan
        for (ItemReceta obsoleto : existentes) {
            itemsAEliminar.add(obsoleto);
        }
        recetaExistente.getItems().removeAll(itemsAEliminar);

        // Reemplazar la lista completa por la nueva
        recetaExistente.getItems().clear();
        recetaExistente.getItems().addAll(updatedItems);
    }

}
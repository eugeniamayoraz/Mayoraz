package com.tp.ong.moduloRecetas.servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tp.ong.moduloRecetas.accesoDatos.*;
import com.tp.ong.moduloRecetas.entidades.*;
import com.tp.ong.moduloRecetas.presentacion.Receta.RecetaFormDto;
import com.tp.ong.moduloRecetas.presentacion.Receta.ItemRecetaDto;
import jakarta.persistence.EntityNotFoundException;// para manejar entidades no encontradas

@Service
public class RecetaServicioImpl implements IRecetaServicio {

    // Inyecto el Repositorio de Recetas
    @Autowired
    private IRecetaRepo recetaRepo; 

    // Inyecto el Repositorio de Ingredientes para poder buscar los ingredientes por ID
    @Autowired
    private IIngredienteRepo ingredienteRepo; 

    // comentario agregado: Inyecto el Repositorio de ItemReceta para manejar explícitamente el borrado lógico
    @Autowired
    private IItemRecetaRepo itemRecetaRepo; 

    //Con la implementación de borrado lógico para ItemReceta,necesito IItemRecetaRepo y manejar la persistencia y el borrado lógico de ItemReceta explícitamente en este servicio.
    
    
  //----------------------------------------------Metodos para Bucar listar y filtrar 
    
 
    //@Transactional(readOnly = true) le da mejor rendimiento) Mas que nada los renombro para invocarlos mas intuitivamente.-
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Receta> buscarPorNombre(String nombre) {
        //busca por nombre exacto (case-sensitive) y NO eliminadas lógicamente
        return recetaRepo.findByNombreAndDeletedFalse(nombre);
    }//-----------------No Lo uso pero lo dejo porque arrranque por aca y luego lo mejoré
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Receta> buscarPorNombreSinCaseSensitive(String nombre) {
        // busca ignorando May o Min (case Sensitive) y NO eliminadas logicamente
        return recetaRepo.findByNombreIgnoreCaseAndDeletedFalse(nombre);
    }//---------------Al final no lo usé xq desde las validaciones se llama directamente al metodo del repo.

       
    @Override
    @Transactional(readOnly = true)
    public List<Receta> listarTodasLasRecetas() {
        // llama al método del repositorio que lista solo las recetas NO eliminadas lógicamente
        return recetaRepo.findByDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receta> buscarPorNombreContienePalabra(String palabra) {
        // busca por contenido de palabra sin CaseSensitive y NO eliminadas logicamente
        return recetaRepo.findByNombreContainingIgnoreCaseAndDeletedFalse(palabra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receta> buscarPorRangoDeCalorías(Integer minCalorias, Integer maxCalorias) {
        // busca por rango de calorías y NO eliminadas logicamente
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
        //Retorna TRUE si Existe con mismo nombre y No es la del mismo ID.
        // Si es Diferente a la que estoy editando ahora es True (hay duplicado)
        // Si es igual a la que estoy edittando ahora (mismo ID) es false, no se considera duplicado porque apunta a la misma.-
    }

    //------------------  METODOS BASICOS:

    // Método para buscar una Receta por ID para su edicion -- ya no lo Uso uso el que sigue
    @Override
    @Transactional(readOnly = true)
    public Optional<Receta> buscarRecetaPorId(Long id) {
        // busco por ID, no impota si esta eliminada logicamente xq la Lista de la Pantalla ya me muestra las activas y solo se pueden editar desde ahi
        // El filtro por 'deleted=false' se hará en los métodos que listan/buscan activas o en la conversión a DTO para edición.
        return recetaRepo.findById(id); 
    }
    
    // Metodo para obtener receta desde la Base No eliminada logicamente para su posterior edicion en el Frontend.-
    // se usa en el Controller para pasar los datos de la Receta existente No eliminada logicamente
    // al final en el return llama a otro metodo para convertir la Entidad a DTO para su edición en pantalla.
    // El filtro de los ítems eliminados logicamente se hace en convertirEntidadADto
    @Override
    @Transactional(readOnly = true)
    public RecetaFormDto obtenerRecetaParaEdicion(Long id) {
        Receta receta = recetaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Receta no encontrada con ID: " + id));
        
        // Si la receta principal está eliminada lógicamente, aviso, pero no va a suceder xq la lista ya las filtra
        if (receta.getDeleted()) {
            throw new EntityNotFoundException("Receta con ID: " + id + " ha sido eliminada lógicamente.");
        }

        // Usa el método de conversión existente, que ya filtra por ítems no eliminados
        return convertirEntidadADto(receta);
    }//este metodo que se usa al final esta mas abajo y es el que se ocupa exclusivamente de la conversion del objeto
    //desde el Controller se invoca directamente a obtenerRecetaParaEdicion (y este internamiente llama al otro para la conversion.-
    
    
    // Metodo para listarTodosLos Items de receta - No lo uso más.-
    @Override
    @Transactional(readOnly = true)
    public List<ItemReceta> listarTodosLosItemsDeReceta(Long recetaId) {
        return itemRecetaRepo.findByRecetaId(recetaId);
    }
    
    // Método para borrado lógico de una Receta
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

            //Marco todos los ItemReceta asociados como eliminados lógicamente
            if (receta.getItems() != null) {
                for (ItemReceta item : receta.getItems()) {
                    item.setDeleted(true); // Marco el ítem como eliminado lógicamente
                    itemRecetaRepo.save(item); // Guardo el cambio para cada ítem
                }
                System.out.println("Todos los ítems de la receta " + id + " marcados como eliminados lógicamente.");
            }
        } else {
            // Manejo de error si la receta no se encuentra o si ya estaba eliminada lógicamente
            throw new RuntimeException("Receta no encontrada o ya eliminada lógicamente con ID: " + id);
        }
    }
   
    
    // Método para guardar o actualizar una Receta
    // Basico - lo refactoricé ya no lo uso.
    @Override
    @Transactional
    public Receta guardarReceta(Receta receta) {
        // La validación de nombre único se hace ANTES de llamar a este método desde el controlador.
        // hay q asociar cada item receta a la referencia de la Receta Contenedora      
        if (receta.getItems() != null) {
            for (ItemReceta item : receta.getItems()) {
                item.setReceta(receta);
            }
        }
        receta.calcularCaloriasTotales(); // Llama al método de la entidad para calcular calorías antes de guardar
        return recetaRepo.save(receta);
    }

 // Método para guardar/actualizar Receta desde un DTO, 
    //el manejo de borrado lógico de ItemReceta esta dentro del metodo aqui invicado para edicion: actualizarEntidadDesdeDto
    @Override
    @Transactional
    public Receta guardarReceta(RecetaFormDto formDto) {
        Receta receta;
        if (formDto.getId() == null) {
            // Es una nueva receta
            receta = convertirDtoAEntidad(formDto); // Usa el método de conversión para crear la entidad base
            
            //Guarda la Receta principal primero para obtener su ID
            receta = recetaRepo.save(receta); 

            // guarda los ItemReceta asociados explícitamente
            if (receta.getItems() != null && !receta.getItems().isEmpty()) {
                // da a cada ItemReceta la referencia a la Receta recién guardada
                for (ItemReceta item : receta.getItems()) {
                    item.setReceta(receta);
                }
                itemRecetaRepo.saveAll(receta.getItems());
            }

        } else {
            // Es una edición de receta existente
            receta = recetaRepo.findById(formDto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Receta no encontrada para actualizar con ID: " + formDto.getId()));
            //Actualiza la entidad existente con los datos del DTO y maneja los ítems
            actualizarEntidadDesdeDto(receta, formDto); 
        }        
        // Recalcula calorías totales después de que los ítems han sido procesados
        receta.calcularCaloriasTotales(); 
      
        // Si es una edición, la receta principal ya fue guardada implícitamente por actualizarEntidadDesdeDto
        // o necesita ser guardada aquí si no hubo ítems modificados.
        // Para seguridad, guardo la receta principal 
        return recetaRepo.save(receta);
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
    //PARA TOMAR LOS DATOS DEL HTML Y CONVERTIRLOS EN UNA ENTIDAD PERSISTENTE.-(Receta creada por ABMC)
    //nueva Receta - los items recetas entran como deleted false.-
    public Receta convertirDtoAEntidad(RecetaFormDto dto) {
        Receta receta = new Receta();
        // El ID para una nueva receta lo genera la DB
        receta.setNombre(dto.getNombre());
        receta.setDescripcion(dto.getDescripcion());
        receta.setDeleted(false); // Por defecto, una nueva receta NO está eliminada lógicamente

        // Convertir la lista de ItemRecetaDto a List<ItemReceta>
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<ItemReceta> listaDeItems = new ArrayList<>();
            for (ItemRecetaDto itemDto : dto.getItems()) {
                ItemReceta item = new ItemReceta();
                // El ID del ItemReceta lo genera la DB
                item.setCantidad(itemDto.getCantidad());
                item.setCalorias(itemDto.getCalorias()); // Las calorías ingresadas manualmente
                item.setDeleted(false); // Un nuevo ítem siempre se crea como NO eliminado lógicamente

                // Buscar el Ingrediente por ID usando el ingredienteRepo inyectado
                Optional<Ingrediente> optionalIngrediente = ingredienteRepo.findById(itemDto.getIngredienteId());
                if (optionalIngrediente.isPresent()) {
                    item.setIngrediente(optionalIngrediente.get());
                } else {
                    throw new IllegalArgumentException("Ingrediente con ID " + itemDto.getIngredienteId() + " no encontrado.");
                }

                item.setReceta(receta); // para la relación bidireccional
                listaDeItems.add(item); // agrego en cada iteracion el item a la lista
            }//End For
            receta.setItems(listaDeItems);
        }
        return receta;
    }

    /**
     * Convierte una entidad Receta a un RecetaFormDto.
     * Utilizado para CARGAR los datos de una receta existente en el formulario de edición.
     * @param receta La entidad Receta a convertir.
     * @return El RecetaFormDto.
     */
    //valida que los items no esten eliminados logicamente antes de pasarlos al DTO.
    @Override
    public RecetaFormDto convertirEntidadADto(Receta receta) {
        RecetaFormDto dto = new RecetaFormDto();
        dto.setId(receta.getId());
        dto.setNombre(receta.getNombre());
        dto.setDescripcion(receta.getDescripcion());

        // Convertir la lista de ItemReceta a List<ItemRecetaDto>
        if (receta.getItems() != null && !receta.getItems().isEmpty()) {
            // Filtra solo los ItemReceta que NO estén eliminados lógicamente!!!!
            List<ItemRecetaDto> listaDeItemDtos = new ArrayList<>(); // Inicializo la listaDto 
            for (ItemReceta item : receta.getItems()) { //Itero sobre los ítems de la entidad
                if (!item.isDeleted()) { // -------------------------------------Solo si no está eliminado
                    ItemRecetaDto itemDto = new ItemRecetaDto();
                    itemDto.setId(item.getId()); 
                    itemDto.setCantidad(item.getCantidad());
                    itemDto.setCalorias(item.getCalorias());
                    if (item.getIngrediente() != null) {
                        itemDto.setIngredienteId(item.getIngrediente().getId());
                    }
                    listaDeItemDtos.add(itemDto); // agrego en cada iteracion el item a la lista de DTOs
                }
            }//End For
            dto.setItems(listaDeItemDtos);
        } else {
            // Si no hay ítems o la lista es nula, inicializo una lista vacía para el DTO, pero no va a suceder xq exigue al menos un item.
            dto.setItems(new ArrayList<>());
        }

        return dto;
    }


    /**
     * Actualizo una entidad Receta existente con los datos de un RecetaFormDto.
     * Lo utilizo para GUARDAR los cambios en la modificación de una Receta existente., de hecho se invoca desde el metodo para guardar.-
     * @param recetaExistente La entidad Receta que voy a actualizar.
     * @param dto El DTO con los nuevos datos.
     */
    //aca el borrado logico de items tiene que ser manejado cuidadosamente.-
    @Override
    @Transactional 
    public void actualizarEntidadDesdeDto(Receta recetaExistente, RecetaFormDto dto) {
        recetaExistente.setNombre(dto.getNombre());
        recetaExistente.setDescripcion(dto.getDescripcion());

        // 1. Obtengo todos los ítems existentes de la base de datos para esta receta (incluyendo los eliminados lógicamente)
        // y los mapeo a un HashMap para una búsqueda practica por ID la key es el ID, y el valor asociado el objeto Item Mismo.
        List<ItemReceta> itemsExistentesDb = itemRecetaRepo.findByRecetaId(recetaExistente.getId());
        Map<Long, ItemReceta> itemsExistentesMap = new HashMap<>();
        //Recorro la lista de items existentes (deleted and not deleted) para meter en el HashMap
        if (itemsExistentesDb != null) {
            for (ItemReceta item : itemsExistentesDb) {
                itemsExistentesMap.put(item.getId(), item);
            }
        }

        List<ItemReceta> itemsParaPersistir = new ArrayList<>(); // Esta es la lista para los ítems que voy a guardar/actualizar.

        // 2. Proceso los ítems que vienen del DTO (nuevos o actualizados)
        //- quiza se agregaron unos, quiza se eliminaron logicamente otros, quiza se modificaron las calorias o cantidad de los existentes)
        //. quiza se agregó alguno que no se mostraba pero estaba borrado logicamente, por ende tiene ese id en la tabla.-
        if (dto.getItems() != null) {
            for (ItemRecetaDto itemDto : dto.getItems()) {
                ItemReceta item;//declaro una variable que luego inicializo en cada bifurcación
            //por cada item del DTO
                if (itemDto.getId() != null && itemsExistentesMap.containsKey(itemDto.getId())) {
                	
                    // Es un ítem que existia previamente: el usuario lo mantuvo o lo edito, pero ya existia.- lo tomo del mapa
                	//remove() quita el elemento de la Hash, pero a la vez lo devuelve por eso se lo adjudica a la variable item.
                    item = itemsExistentesMap.remove(itemDto.getId()); // Lo remuevo para saber cuáles quedaron "sin tocar".
                    item.setDeleted(false); // Si estaba eliminado y vuelve, lo re-activo.
                } else {
                    // Es un nuevo ítem.
                    item = new ItemReceta();
                    item.setDeleted(false); // Un nuevo ítem siempre lo marco como activo, ya ingresa como activo igual
                } 
                
                // Actualizo las propiedades del ítem.
                item.setCantidad(itemDto.getCantidad());
                item.setCalorias(itemDto.getCalorias());
                
                // Si el ingrediente puede cambiar, lo actualizo.
                if (item.getIngrediente() == null || !item.getIngrediente().getId().equals(itemDto.getIngredienteId())) {
                    Optional<Ingrediente> ingredienteActualizado = ingredienteRepo.findById(itemDto.getIngredienteId());
                    if (ingredienteActualizado.isPresent()) {
                        item.setIngrediente(ingredienteActualizado.get());
                    } else {
                        throw new IllegalArgumentException("Ingrediente con ID " + itemDto.getIngredienteId() + " no encontrado para ítem de receta.");
                    }
                }
                item.setReceta(recetaExistente); // Aseguro la relación bidireccional.
                itemsParaPersistir.add(item); 
            }
        }

        // 3. Proceso ítems que estaban en la DB pero no en el DTO (fueron eliminados del formulario).
        // Los ítems que quedan en el HashMap son los que ya no están en el DTO, o sea los que 
        // el usuario eliminó del formulario o ya estaban en la base marcados como deleted true. 
        //Todos estos tienen que quedar como deleted = true en la base de datos.
        for (ItemReceta dbItem : itemsExistentesMap.values()) {
            if (!dbItem.isDeleted()) { // Solo si no estaban ya eliminados.
                dbItem.setDeleted(true); // Los marco como eliminados lógicamente.
                itemsParaPersistir.add(dbItem); // Los añado a la lista para persistir el cambio de estado.
            }
        }

        // 4. Guardo todos los ítems (nuevos, actualizados, o marcados como eliminados).
        itemRecetaRepo.saveAll(itemsParaPersistir);

        // 5. Actualizo la colección de ítems de la receta con todos los ítems procesados (activos y lógicamente eliminados).
        // El método calcularCaloriasTotales() de la entidad se encargará de filtrar solo los activos.
        // y en esta caso se invoca antes de guardar la entidad editada por el usuario, para actualizar justamente las calorias totales
        recetaExistente.setItems(itemsParaPersistir);
    }
    
}
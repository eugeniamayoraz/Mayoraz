package com.tp.ong.moduloRecetas.presentacion.Receta;

import com.tp.ong.moduloRecetas.servicios.IRecetaServicio;
import com.tp.ong.moduloRecetas.servicios.IIngredienteServicio;
//import com.tp.ong.moduloRecetas.entidades.Receta; // comentario agregado: Se mantiene para tipos de retorno/variables, aunque se usará más el DTO en el flujo del formulario
import jakarta.validation.Valid; // Para habilitar las validaciones en el DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Para pasar datos a la vista
import org.springframework.validation.BindingResult; // Para manejar errores de validación
import org.springframework.validation.FieldError; // Para errores de campo específicos
import org.springframework.web.bind.annotation.*; // Para anotaciones de mapeo HTTP
import java.util.ArrayList; // Para crear nuevas instancias de ArrayList
import jakarta.persistence.EntityNotFoundException; // para capturar excepciones de entidad no encontrada

@Controller
@RequestMapping("/recetas") // Prefijo para todas las URLs de este controlador
public class RecetasRegistrarEditarController {

    // Inyección de dependencias
    @Autowired
    private IRecetaServicio recetaServicio; // Servicio para manejar la lógica de negocio de las recetas

    @Autowired
    private IIngredienteServicio ingredienteServicio; // Servicio para obtener la lista de ingredientes disponibles

    /** NOTA: Hay una opcion de crear un método auxiliar cargarFormularioConErrores para renderizar el form si hay errores
     * y para no repetir tanta logica dentro de cada metodo, ya que incluye todos los datos que usa el HTML, DTOs, Listas, errores
     * de validación (BindingResult -Para mostrar mensajes de error específicos por campo) y mensajeError (Para mostrar mensajes de
     * errores generales (por ejemplo, errores de lógica de negocio o excepciones).
     */

    /**
     * Muestra el formulario para agregar una nueva receta. GET /recetas/nueva
     * * le crea y pasa el DTO y la lista de ingredientes al modelo
     * @param model Objeto Model (mochila) para pasar datos a la vista.
     * @return El nombre de la vista (template Thymeleaf) para el formulario de receta.
     */
    @GetMapping("/nueva") 
    public String mostrarFormularioNuevaReceta(Model model) {
        // Creo un nuevo DTO vacío para el formulario. Thymeleaf usa este objeto para enlazar los campos del formulario.
        RecetaFormDto recetaFormDto = new RecetaFormDto();
        // inicializo la lista de ítems para evitar NullPointerExceptions
        if (recetaFormDto.getItems() == null) {
            recetaFormDto.setItems(new ArrayList<>());
        }
        model.addAttribute("recetaFormDto", recetaFormDto);
        model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
        return "RecetasTemplates/receta-form";
    }

    /**
     * Muestra el formulario para editar una receta existente. GET /recetas/editar/{id}
     *
     * @param id              El ID de la receta a editar.
     * @param model           Objeto Model para pasar datos a la vista.
     * @return El nombre de la vista (template Thymeleaf) para el formulario de receta.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarReceta(@PathVariable Long id, Model model) {
        try {
            RecetaFormDto recetaFormDto = recetaServicio.obtenerRecetaParaEdicion(id);
            
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            return "RecetasTemplates/receta-form";
            
        } catch (EntityNotFoundException e) {
            // Si la receta no se encuentra o está eliminada lógicamente
            model.addAttribute("mensajeError", e.getMessage()); // Mostrar el mensaje de error del servicio
            return "redirect:/recetas/buscar"; // Redirijo a la página de búsqueda
        } catch (Exception e) {
            // Capturao otra excepción inesperada
            model.addAttribute("mensajeError", "Ocurrió un error inesperado al cargar la receta para editar: " + e.getMessage());
            return "redirect:/recetas/buscar";
        }
    }


    /**
     * Procesa el envío del formulario para guardar una nueva receta o actualizar una existente. POST /recetas/guardar
     *
     * @param recetaFormDto   El DTO que contiene los datos del formulario, con validaciones activas.
     * @param result          Objeto BindingResult para capturar errores de validación.
     * @param model           Objeto Model, usado en caso de que haya errores y se deba volver al formulario.
     * @return Redirección a la lista de recetas o al mismo formulario en caso de errores.
     */
    @PostMapping("/guardar")
    public String guardarReceta(@Valid @ModelAttribute("recetaFormDto") RecetaFormDto recetaFormDto,
                                 BindingResult result, Model model) {
        // Cuando Spring MVC recibe una petición y mapea los datos a un DTO que está marcado con @Valid o @Validated en el controlador, 
        // automáticamente activa las validaciones de campos del dto. 
        // 1. Verifico si hay errores de validación en el DTO (ej. @NotNull, @Min en nombre, descripcion o ítems).
        if (result.hasErrors()) {
            // Si hay errores, volver al formulario.
            // ! volver a cargar los ingredientes disponibles sino el select estará vacío.
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            // No hay mensajeError general, los errores de validación se manejan con BindingResult
            return "RecetasTemplates/receta-form"; // Vuelve a la misma vista del formulario
        }

        // 2. Validación de LÓGICA DE NEGOCIO: Nombre de receta único
        boolean nombreExiste;
        if (recetaFormDto.getId() == null) {
            // Es una nueva receta: verifico si el nombre ya existe en cualquier otra receta activa
            nombreExiste = recetaServicio.existeRecetaConNombre(recetaFormDto.getNombre());
        } else {
            // Es una edición: verifico si el nombre ya existe en otra receta ACTIVA que NO sea la propia
            nombreExiste = recetaServicio.existeRecetaConNombreExcluyendoId(recetaFormDto.getNombre(),
                    recetaFormDto.getId());
        }

        if (nombreExiste) {
            // Si el nombre ya existe, añadir un error de campo específico a 'nombre'
            result.addError(new FieldError("recetaFormDto", "nombre", "Ya existe una receta con este nombre."));
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            // No hay mensajeError general aquí
            return "RecetasTemplates/receta-form"; // Vuelvo al formulario con el error de nombre
        }

        try {
            // el servicio en su metodo maneja si es creación o edición
            recetaServicio.guardarReceta(recetaFormDto); 
            
            // Redirigir a la página de búsqueda de recetas después de la operacion terminada exitosamente.
            // Este es el patrón "POST-Redirect-GET" para evitar doble submission.
            return "redirect:/recetas/buscar"; 
        } catch (IllegalArgumentException e) {
            // Capturo excepciones personalizadas como "Ingrediente no encontrado" o lógicas de negocio
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            model.addAttribute("mensajeError", "Error de datos: " + e.getMessage()); // Añado el mensaje de error
            return "RecetasTemplates/receta-form";
        } catch (Exception e) {
            // Capturar cualquier otra excepción inesperada
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            model.addAttribute("mensajeError", "Ocurrió un error inesperado al guardar la receta: " + e.getMessage()); // Añadir el mensaje de error
            return "RecetasTemplates/receta-form";
        }
    }

    /**
     * Endpoint para AÑADIR un nuevo campo de ItemReceta al formulario.
     * Este método se invoca cuando el usuario hace clic en "Agregar Ingrediente".
     * Se procesa el formulario actual, se añade un nuevo DTO de ítem vacío a la lista,
     * y se vuelve a cargar la misma vista del formulario con el nuevo campo.
     *
     * @param recetaFormDto El DTO actual del formulario, con los datos ya ingresados por el usuario.
     * @param model         El objeto Model para pasar datos a la vista.
     * @return El nombre de la vista del formulario, para recargar la página con el nuevo campo.
     */
    @PostMapping(value = "/agregarItem")
    public String agregarItemReceta(@ModelAttribute("recetaFormDto") RecetaFormDto recetaFormDto, Model model) {
        if (recetaFormDto.getItems() == null) {
            recetaFormDto.setItems(new ArrayList<>());
        }
        // Añade un ItemRecetaDto vacío a la lista. Thymeleaf lo renderizará como un nuevo conjunto de campos.
        recetaFormDto.getItems().add(new ItemRecetaDto()); // Creo una nueva instancia de ItemRecetaDto

        model.addAttribute("recetaFormDto", recetaFormDto);
        model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
        // No hay mensajeError aquí

        // Recarga el formulario con el nuevo ítem, manteniendo todos los datos ya introducidos por el usuario.
        return "RecetasTemplates/receta-form";
    }

    /**
     * Endpoint para QUITAR un campo de ItemReceta del formulario: clic en "Quitar Ingrediente" para un ítem específico.
     * Se procesa el formulario actual, se elimina el ítem de la lista x su índice, y se vuelve a cargar la vista con el ítem removido.
     *
     * @param recetaFormDto El DTO actual del formulario, con los datos ya ingresados por el usuario(el que va como @ModelAtribute)
     * que es "el DTO que se enlaza automáticamente desde el formulario enviado.
     * @param itemIndex     El índice del ítem en la lista 'items' que se desea eliminar.
     * @param model         El objeto Model para pasar datos a la vista.
     * @return El nombre de la vista del formulario, para recargar la página sin el ítem.
     */
    @PostMapping(value = "/quitarItem")
    public String quitarItemReceta(@ModelAttribute("recetaFormDto") RecetaFormDto recetaFormDto,
                                   @RequestParam("itemIndex") int itemIndex, Model model) {
        // Verifico que la lista de ítems exista y que el índice sea válido.
        if (recetaFormDto.getItems() != null && itemIndex >= 0 && itemIndex < recetaFormDto.getItems().size()) {
            recetaFormDto.getItems().remove(itemIndex);
        }
        model.addAttribute("recetaFormDto", recetaFormDto);//paso los nuevos datos al model p que se muestren en la vista
        //este objeto DTO ahora no va a contener ese Item,.
        model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
        return "RecetasTemplates/receta-form"; // Renderizo la misma pantalla ahora sin el ítem quitado, manteniendo los demás datos.
    }


    /**
     * Proceso la solicitud para eliminar lógicamente una receta. GET /recetas/eliminar/{id} (o POST para más seguridad)
     *
     * @param id El ID de la receta a eliminar.
     * @return Redirección a la página de búsqueda de recetas.
     */
    @GetMapping("/eliminar/{id}") // Aunque se usa GET para operaciones de cambio de estado se recomienda POST.
    public String eliminarReceta(@PathVariable Long id) {
        try {
            // Llamo al servicio para hacer el borrado lógico.
            recetaServicio.borrarRecetaLogico(id);
        } catch (Exception e) {
            // Capturo cualquier error durante el borrado.
            // Si hay un error, simplemente redirijo
            System.err.println("Error al eliminar receta: " + e.getMessage()); // logeo el error para depuración
        }
        // Redirijo a la página de búsqueda.
        return "redirect:/recetas/buscar";
    }

}

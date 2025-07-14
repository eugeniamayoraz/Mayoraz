package com.tp.ong.moduloRecetas.presentacion.Receta;

import com.tp.ong.moduloRecetas.servicios.IRecetaServicio;
import com.tp.ong.moduloRecetas.servicios.IIngredienteServicio;
import com.tp.ong.moduloRecetas.entidades.Receta;
import jakarta.validation.Valid; // Para habilitar las validaciones en el DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Para pasar datos a la vista
import org.springframework.validation.BindingResult; // Para manejar errores de validación
import org.springframework.validation.FieldError; // Para errores de campo específicos
import org.springframework.web.bind.annotation.*; // Para anotaciones de mapeo HTTP
// import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ¡Esta línea ha sido eliminada ya que no se usará RedirectAttributes!
import java.util.ArrayList; // Para crear nuevas instancias de ArrayList
//import java.util.List; // Importante: para declarar variables de tipo List
import java.util.Optional;

@Controller
@RequestMapping("/recetas") // Prefijo para todas las URLs de este controlador
public class RecetasRegistrarEditarController {

    // Inyección de dependencias
    @Autowired
    private IRecetaServicio recetaServicio; // Servicio para manejar la lógica de negocio de las recetas

    @Autowired
    private IIngredienteServicio ingredienteServicio; // Servicio para obtener la lista de ingredientes disponibles

    /** NOTA: Hay una opcion de crear un método auxiliar cargarFormularioConErrores para renderizar el form si hay errores
    y para no repetir tanta logica dentro de cada metodo, ya que incluye todos los datos que usa el HTML, DTOs, Listas, errores
    de validación (BindingResult -Para mostrar mensajes de error específicos por campo) y mensajeError (Para mostrar mensajes de
    errores generales (por ejemplo, errores de lógica de negocio o excepciones).
    */

    /**
     * Muestra el formulario para agregar una nueva receta. GET /recetas/nueva 
     * 
     * le crea y pasa el DTO y la lista de ingredientes al modelo
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
     * @param id                 El ID de la receta a editar.
     * @param model              Objeto Model para pasar datos a la vista.
     * @return El nombre de la vista (template Thymeleaf) para el formulario de receta.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarReceta(@PathVariable Long id, Model model) {
        // 1. Buscar la receta por su ID.Recuper la Receta de la DB
        Optional<Receta> optionalReceta = recetaServicio.buscarRecetaPorId(id);

        if (optionalReceta.isPresent()) {
            Receta recetaExistente = optionalReceta.get();//si la encuentra la asigna a una variable de tipo Receta "recetaexistente"
            // 2. Converto la entidad Receta a un DTO para llenar el formulario.
            RecetaFormDto recetaFormDto = recetaServicio.convertirEntidadADto(recetaExistente);
            //aca declaro una variable de tipo RecetaFormDto y le asigno el RecetaFormDto que me devulve el metodo.-
            //La instanciación (new RecetaFormDto()) no la veo directamente xq ocurre dentro del método convertirEntidadADto
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            return "RecetasTemplates/receta-form";//Devuelve la vista html de receta-form con los datos del modelo.-
            
        } else {
            // Si la receta no se encuentra, redirigir a la página de búsqueda.
            return "redirect:/recetas/buscar"; // Redirigir a la página de búsqueda (ruta completa con /)
        }
    }


    /**
     * Procesa el envío del formulario para guardar una nueva receta o actualizar una existente. POST /recetas/guardar
     *
     * @param recetaFormDto      El DTO que contiene los datos del formulario, con validaciones activas.
     * @param result             Objeto BindingResult para capturar errores de validación.
     * @param model              Objeto Model, usado en caso de que haya errores y se deba volver al formulario.
     * @return Redirección a la lista de recetas o al mismo formulario en caso de errores.
     */
    @PostMapping("/guardar")
    public String guardarReceta(@Valid @ModelAttribute("recetaFormDto") RecetaFormDto recetaFormDto,
                                BindingResult result, Model model /* RedirectAttributes redirectAttributes - PARAMETRO ELIMINADO */) {
//Cuando Spring MVC recibe una petición y mapea los datos a un DTO que está marcado con @Valid o @Validated en el controlador, 
//automáticamente activa las validaciones de campos del dto. 
        // 1. Verificar si hay errores de validación en el DTO (ej. @NotNull, @Min en nombre, descripcion o ítems).
        if (result.hasErrors()) {
            // Si hay errores, volver al formulario.
            // Es crucial volver a cargar los ingredientes disponibles, de lo contrario el select estará vacío.
            // Lógica que estaba en cargarFormularioConErrores, ahora replicada aquí
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            // No hay mensajeError general aquí, los errores de validación se manejan con BindingResult
            return "RecetasTemplates/receta-form"; // Vuelve a la misma vista del formulario
        }

        // 2. Validación de LÓGICA DE NEGOCIO: Nombre de receta único
        boolean nombreExiste;
        if (recetaFormDto.getId() == null) {
            // Es una nueva receta: verificar si el nombre ya existe en cualquier otra receta activa
            nombreExiste = recetaServicio.existeRecetaConNombre(recetaFormDto.getNombre());
        } else {
            // Es una edición: verificar si el nombre ya existe en otra receta ACTIVA que NO sea la propia
            nombreExiste = recetaServicio.existeRecetaConNombreExcluyendoId(recetaFormDto.getNombre(),
                    recetaFormDto.getId());
        }

        if (nombreExiste) {
            // Si el nombre ya existe, añadir un error de campo específico a 'nombre'
            result.addError(new FieldError("recetaFormDto", "nombre", "Ya existe una receta con este nombre."));
            // Lógica que estaba en cargarFormularioConErrores, ahora replicada aquí
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            // No hay mensajeError general aquí
            return "RecetasTemplates/receta-form"; // Volver al formulario con el error de nombre
        }

        try {
            Receta receta;
            if (recetaFormDto.getId() == null) {
                // Si el ID es nulo, es una nueva receta.
                // 2. Convertir el DTO a una entidad Receta (crear la entidad en memoria).
                receta = recetaServicio.convertirDtoAEntidad(recetaFormDto);
                // 3. Guardar la nueva receta en la base de datos (con sus ítems casacadeados).
                recetaServicio.guardarReceta(receta);
                // Los mensajes flash NO se usan.
                // redirectAttributes.addFlashAttribute("mensajeExito", "Receta creada exitosamente!"); // Línea eliminada
            } else {
                // Si el ID no es nulo, es una receta existente que se está editando.
                // 2. Buscar la receta existente en la base de datos.
                Optional<Receta> optionalReceta = recetaServicio.buscarRecetaPorId(recetaFormDto.getId());
                if (optionalReceta.isPresent()) {
                    Receta recetaExistente = optionalReceta.get();
                    // 3. Actualizar la entidad existente con los datos del DTO.
                    recetaServicio.actualizarEntidadDesdeDto(recetaExistente, recetaFormDto);
                    // 4. Guardar los cambios en la base de datos.
                    recetaServicio.guardarReceta(recetaExistente); // Se reutiliza el método guardarReceta
                    // Los mensajes flash NO se usan.
                    // redirectAttributes.addFlashAttribute("mensajeExito", "Receta actualizada exitosamente!"); // Línea eliminada
                } else {
                    // Manejo si la receta no se encuentra para edición
                    // Los mensajes flash NO se usan, se usa el Model para un error en el formulario actual.
                    // redirectAttributes.addFlashAttribute("mensajeError", "Error: Receta a editar no encontrada."); // Línea eliminada
                    model.addAttribute("recetaFormDto", recetaFormDto); // Mantener el DTO en el modelo
                    model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
                    model.addAttribute("mensajeError", "Error: Receta a editar no encontrada. Por favor, intente de nuevo."); // Añadir el mensaje de error directamente al Model
                    return "RecetasTemplates/receta-form"; // Vuelve al formulario con el mensaje de error
                }
            }
            // Redirigir a la página de búsqueda de recetas después de un éxito.
            // Este es el patrón "POST-Redirect-GET" para evitar doble submission.
            return "redirect:/recetas/buscar"; // Asume que tienes un controlador o mapeo para /recetas/buscar
        } catch (IllegalArgumentException e) {
            // Capturar excepciones personalizadas como "Ingrediente no encontrado" o lógicas de negocio
            // Lógica que estaba en cargarFormularioConErrores, ahora replicada aquí
            model.addAttribute("recetaFormDto", recetaFormDto);
            model.addAttribute("ingredientesDisponibles", ingredienteServicio.listarTodosLosIngredientes());
            model.addAttribute("mensajeError", "Error de datos: " + e.getMessage()); // Añadir el mensaje de error
            return "RecetasTemplates/receta-form";
        } catch (Exception e) {
            // Capturar cualquier otra excepción inesperada
            // Lógica que estaba en cargarFormularioConErrores, ahora replicada aquí
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
        // Asegurarse de que la lista de ítems no sea nula antes de añadir.
        // Se declara la variable como List y se instancia como ArrayList
        if (recetaFormDto.getItems() == null) {
            recetaFormDto.setItems(new ArrayList<>());
        }
        // Añade un ItemRecetaDto vacío a la lista. Thymeleaf lo renderizará como un nuevo conjunto de campos.
        recetaFormDto.getItems().add(new ItemRecetaDto()); // Aquí creamos una nueva instancia de ItemRecetaDto

        // Lógica que estaba en cargarFormularioConErrores, ahora replicada aquí
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
     *  que es "el DTO que se enlaza automáticamente desde el formulario enviado.
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
        return "RecetasTemplates/receta-form"; // Renderiza la misma pantalla ahora sin el ítem quitado, manteniendo los demás datos.
    }


    /**
     * Proceso la solicitud para eliminar lógicamente una receta. GET /recetas/eliminar/{id} (o POST para más seguridad)
     *
     * @param id El ID de la receta a eliminar.
     * @return Redirección a la página de búsqueda de recetas.
     */
    @GetMapping("/eliminar/{id}") // Aunque se usa GET aquí, para operaciones de cambio de estado se recomienda POST.
    public String eliminarReceta(@PathVariable Long id) {
        try {
            // Llamo al servicio para hacer el borrado lógico.
            recetaServicio.borrarRecetaLogico(id);
        } catch (Exception e) {
            // Capturo cualquier error durante el borrado.
            // Si hay un error, simplemente redirijo
            System.err.println("Error al eliminar receta: " + e.getMessage()); // Loguea el error para depuración
        }
        // Redirijo a la página de búsqueda.
        return "redirect:/recetas/buscar";
    }

}
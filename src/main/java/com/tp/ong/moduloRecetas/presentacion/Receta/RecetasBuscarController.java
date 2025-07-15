package com.tp.ong.moduloRecetas.presentacion.Receta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList; // comentario agregado: Necesario para inicializar listas vacías
import java.util.List; 

import com.tp.ong.moduloRecetas.servicios.IRecetaServicio;
import com.tp.ong.moduloRecetas.entidades.Receta;

/*
 * @RequestMapping("/recetas") define el prefijo común de ruta (en este caso: /recetas).
 * @GetMapping (o @PostMapping, etc.) define el tipo de método HTTP (GET, POST, etc.) y el camino restante.
 *
 */

@Controller
@RequestMapping("/recetas") // Prefijo Común de la Ruta
public class RecetasBuscarController {
	
    @Autowired
    private IRecetaServicio recetaServicio;
    
    // Metodos del controller

    /**
     * Método principal para buscar y listar recetas.
     * Maneja la visualización inicial de todas las recetas,
     * y el filtrado por nombre o por rango de calorías.
     * También establece una bandera para controlar la visibilidad del botón "Limpiar Filtros".
     *
     * @param nombre        Parámetro opcional para buscar por nombre (contiene).
     * @param minCalorias   Parámetro opcional para el límite inferior de calorías.
     * @param maxCalorias   Parámetro opcional para el límite superior de calorías.
     * @param model         Objeto Model para pasar datos a la vista.
     * @return El nombre de la vista (template Thymeleaf) para el formulario de búsqueda de recetas.
     */
    @GetMapping("/buscar")
    public String buscarRecetas(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "minCalorias", required = false) Integer minCalorias,
            @RequestParam(value = "maxCalorias", required = false) Integer maxCalorias,
            Model model) {

        List<Receta> recetas;
        String mensaje = null;
        // comentario agregado: Bandera para indicar si se realizó una búsqueda con filtros
        boolean searchPerformed = false; 

        // Lógica para aplicar filtros
        if (nombre != null && !nombre.trim().isEmpty()) {
            recetas = recetaServicio.buscarPorNombreContienePalabra(nombre.trim());
            if (recetas.isEmpty()) {
                mensaje = "No se encontraron recetas con el nombre que contiene '" + nombre + "'.";
            } else {
                mensaje = "Se encontraron " + recetas.size() + " receta(s) que contienen '" + nombre + "'.";
            }
            searchPerformed = true; // comentario agregado: Se realizó una búsqueda por nombre
        } else if (minCalorias != null && maxCalorias != null) {
            // Validar que minCalorias no sea mayor que maxCalorias y que sean positivos
            if (minCalorias < 0 || maxCalorias < 0 || minCalorias > maxCalorias) {
                mensaje = "Por favor, ingrese un rango de calorías válido (valores no negativos, y mínimo no puede ser mayor que máximo).";
                recetas = new ArrayList<>(); // Lista vacía por error de validación
            } else {
                recetas = recetaServicio.buscarPorRangoDeCalorías(minCalorias, maxCalorias);
                if (recetas.isEmpty()) {
                    mensaje = "No se encontró ninguna receta entre " + minCalorias + " y " + maxCalorias + " calorías.";
                } else {
                    mensaje = "Se encontraron " + recetas.size() + " receta(s) entre " + minCalorias + " y " + maxCalorias + " calorías.";
                }
            }
            searchPerformed = true; // Se realizó una búsqueda por rango de calorías
        } else {
            // Si no hay parámetros de filtro, mostrar todas las recetas activas
            recetas = recetaServicio.listarTodasLasRecetas();
            if (recetas.isEmpty()) {
                mensaje = "No hay recetas disponibles.";
            }
            // searchPerformed se mantiene en false si no hay filtros aplicados - a esto lo uso para Activar el Btn de Limpiar Filtros
        }

        model.addAttribute("listaRecetas", recetas); // comentario agregado: Usar "listaRecetas" para la tabla
        model.addAttribute("mensaje", mensaje); // Pasa el mensaje al modelo
        
        // comentario agregado: Para mantener los valores del filtro en el formulario si se hizo una búsqueda
        model.addAttribute("nombreFiltro", nombre);
        model.addAttribute("minCaloriasFiltro", minCalorias);
        model.addAttribute("maxCaloriasFiltro", maxCalorias);
        
        // comentario agregado: Añadir la bandera al modelo para el HTML
        model.addAttribute("searchPerformed", searchPerformed);

        return "RecetasTemplates/recetas-Buscar-Form"; // comentario agregado: Retorna al template principal de búsqueda
    }
}

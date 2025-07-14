package com.tp.ong.moduloRecetas.presentacion.Receta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List; 

import com.tp.ong.moduloRecetas.servicios.IRecetaServicio;
import com.tp.ong.moduloRecetas.entidades.Receta;

/*
 * @RequestMapping("/recetas") define el prefijo común de ruta (en este caso: /recetas).
   @GetMapping (o @PostMapping, etc.) define el tipo de método HTTP (GET, POST, etc.) y el camino restante.
 *
 */

@Controller
@RequestMapping("/recetas") // Prefijo Común de la Ruta
public class RecetasBuscarController {
	

    @Autowired
    private IRecetaServicio recetaServicio;
    
    	//Metodos del controller

    //metodo que retorna el Template(la pagina) de Buscar Recetas y a nivel datos: "la lista cargada de las existentes" q se agregan al "modelo".
    @GetMapping("/buscar")
    public String mostrarBusqueda(Model model) {
        model.addAttribute("listaRecetas", recetaServicio.listarTodasLasRecetas());//Le pasamos un valor al modelo para poder invocarlo desde el html 
        return "RecetasTemplates/recetasBuscarForm";//nos retorna al html recetasBuscarForm pero como esta en subcarpeta tuve que agregarle el prefijo
    }
    
    
    //desde el navegador cdo ya estas en la pantalla (en el html "recetasBuscarForm") hay forms internos que invocan a los otros
    //metodos HTTP de este controller con la url que se le indica ej /buscarPorNombre ; /buscarCalorias 
    //con <form th:action="@{/recetas/buscarCalorias}" method="get"> cdo se le da Submit al form interno del html.-
    

    // Método para filtrar recetas que contengan el nombre tipeado...(input)
    @GetMapping("/buscarPorNombre")
    public String buscarRecetaPorNombre(@RequestParam("nombre") String nombreReceta, Model model) {
        List<Receta> recetasEncontradas = recetaServicio.buscarPorNombreContienePalabra(nombreReceta);

        if (!recetasEncontradas.isEmpty()) {
            model.addAttribute("listaRecetas", recetasEncontradas);
            model.addAttribute("mensaje", "Se encontraron " + recetasEncontradas.size() + " receta(s) que contienen '" + nombreReceta + "'.");
        } else {
            model.addAttribute("listaRecetas", recetasEncontradas); // Será una lista vacía
            model.addAttribute("mensaje", "No se encontró ninguna receta que contenga: '" + nombreReceta + "'.");
        }
        return "RecetasTemplates/recetasBuscarForm"; //Devuelve el template renderizado? 
        //Spring Boot/Thymeleaf se encargan de renderizar esta vista con los datos del modelo antes de enviarla al cliente(naveg)
    }
    
    // Método para filtrar recetas por rango de calorías tipeado...(input)
    @GetMapping("/buscarCalorias") // Coincide con th:action del formulario
    public String buscarRecetaPorRangoCalorias(
        @RequestParam("minCalorias") Integer minCalorias, // Parámetro de inicio del rango
        @RequestParam("maxCalorias") Integer maxCalorias, // Parámetro de fin del rango
        Model model) {

        // Valida que minCalorias no sea nulo y no sea mayor que maxCalorias
        if (minCalorias == null || maxCalorias == null || minCalorias < 0 || maxCalorias < 0 || minCalorias > maxCalorias) {
            model.addAttribute("listaRecetas", List.of()); // Devuelve una lista vacía
            model.addAttribute("mensaje", "Por favor, ingrese un rango de calorías válido (valores positivos, y mínimo no puede ser mayor que máximo).");
            return "RecetasTemplates/recetasBuscarForm";
        }
        // cierre if

        List<Receta> recetasEncontradas = recetaServicio.buscarPorRangoDeCalorías(minCalorias, maxCalorias);

        if (!recetasEncontradas.isEmpty()) {
            model.addAttribute("listaRecetas", recetasEncontradas);
            model.addAttribute("mensaje", "Se encontraron " + recetasEncontradas.size() + " receta(s) entre " + minCalorias + " y " + maxCalorias + " calorías.");
        } else {
            model.addAttribute("listaRecetas", recetasEncontradas);
            model.addAttribute("mensaje", "No se encontró ninguna receta entre " + minCalorias + " y " + maxCalorias + " calorías.");
        }
        return "RecetasTemplates/recetasBuscarForm";
    }
    
}
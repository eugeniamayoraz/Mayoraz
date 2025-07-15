package com.tp.ong.util;

import com.tp.ong.moduloRecetas.entidades.Ingrediente;
import com.tp.ong.moduloRecetas.entidades.ItemReceta;
import com.tp.ong.moduloRecetas.entidades.Receta;
import com.tp.ong.moduloRecetas.accesoDatos.IIngredienteRepo;
import com.tp.ong.moduloRecetas.accesoDatos.IRecetaRepo;
import com.tp.ong.moduloRecetas.accesoDatos.IItemRecetaRepo; // ¡NUEVA IMPORTACIÓN!
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration // Indica que esta clase contiene definiciones de beans de Spring
public class CargaDeDatos {

    // Inyectamos los repositorios que necesitamos
    private final IRecetaRepo recetaRepo;
    private final IIngredienteRepo ingredienteRepo;
    private final IItemRecetaRepo itemRecetaRepo; // ¡NUEVA INYECCIÓN!

    // Constructor para inyección de dependencias por Spring
    public CargaDeDatos(IRecetaRepo recetaRepo, IIngredienteRepo ingredienteRepo, IItemRecetaRepo itemRecetaRepo) { // ¡Constructor ACTUALIZADO!
        this.recetaRepo = recetaRepo;
        this.ingredienteRepo = ingredienteRepo;
        this.itemRecetaRepo = itemRecetaRepo; // Asignación del nuevo repositorio
    }

    // Este método será ejecutado por Spring Boot una vez que el contexto de la aplicación esté completamente cargado
    @Bean // Indica que el valor devuelto por este método (una instancia de CommandLineRunner) es un bean de Spring
    @Transactional // en una sola transacción
    public CommandLineRunner inicializarDatos() {
        return args -> {
            System.out.println("--- Iniciando carga de datos de ejemplo (CargaDeDatos CommandLineRunner) ---");

            // --- Instanciar y guardar Ingredientes y Recetas de forma idempotente ---
            // Es decir, que se puedan ejecutar múltiples veces sin causar errores de duplicidad.

            // RECETA 1: ARROZ CON POLLO
            // Definir los datos de los ingredientes de la receta
            List<ItemRecetaData> itemsArrozConPolloData = new ArrayList<>();
            itemsArrozConPolloData.add(new ItemRecetaData("Arroz", 200.0, 700));
            itemsArrozConPolloData.add(new ItemRecetaData("Cebolla", 100.0, 40));
            itemsArrozConPolloData.add(new ItemRecetaData("Morrón", 50.0, 20));
            itemsArrozConPolloData.add(new ItemRecetaData("Pollo", 300.0, 600));

            // Llamar al método auxiliar para obtener o crear la receta completa
            getOrCreateReceta(
                "Arroz con Pollo",
                "Una deliciosa receta de arroz con pollo, perfecta para cualquier ocasión.",
                itemsArrozConPolloData
            );
            System.out.println("--- Arroz con Pollo procesado ---");

            // --- RECETA 2: GUISO DE LENTEJAS ---
            // Definir los datos de los ingredientes de la receta
            List<ItemRecetaData> itemsGuisoLentejasData = new ArrayList<>();
            itemsGuisoLentejasData.add(new ItemRecetaData("Lentejas", 250.0, 850));
            itemsGuisoLentejasData.add(new ItemRecetaData("Papa", 200.0, 180));
            itemsGuisoLentejasData.add(new ItemRecetaData("Zanahoria", 100.0, 40));
            itemsGuisoLentejasData.add(new ItemRecetaData("Tomate", 150.0, 30));
            itemsGuisoLentejasData.add(new ItemRecetaData("Carne", 200.0, 450));
            itemsGuisoLentejasData.add(new ItemRecetaData("Cebolla", 50.0, 20)); // Reutilizamos el objeto cebolla
            itemsGuisoLentejasData.add(new ItemRecetaData("Morrón", 50.0, 30)); // Reutilizamos el objeto morron

            // Llamar al método auxiliar para obtener o crear la receta completa
            getOrCreateReceta(
                "Guiso de Lentejas",
                "Un guiso reconfortante y nutritivo, ideal para los días fríos.",
                itemsGuisoLentejasData
            );
            System.out.println("--- Guiso de Lentejas procesado ---");

            System.out.println("--- Carga de datos de ejemplo finalizada ---");

            // *** INGREDIENTES ADICIONALES (para que esten dados de alta y se puedan usar en itemReceta) ***
            // Estos no necesitan cambios ya que no tienen borrado lógico propio
            getOrCreateIngrediente("Carne Picada");
            getOrCreateIngrediente("Cebolla de Verdeo");
            getOrCreateIngrediente("Morrón Rojo");
            getOrCreateIngrediente("Leche");
            getOrCreateIngrediente("Manteca");
            getOrCreateIngrediente("Queso Rallado");
            getOrCreateIngrediente("Queso Cremoso");
            getOrCreateIngrediente("Queso Muzzarella");
            getOrCreateIngrediente("Crema de Leche");
            getOrCreateIngrediente("Huevo");
            getOrCreateIngrediente("Huevo Duro");
            getOrCreateIngrediente("Aceitunas Verdes");
            getOrCreateIngrediente("Pasas de Uva");
            getOrCreateIngrediente("Tapa de Empanadas");
            getOrCreateIngrediente("Tapa de Masa Pascualina");
            getOrCreateIngrediente("Aceite de Girasol");
            // Condimentos
            getOrCreateIngrediente("Sal");
            getOrCreateIngrediente("Pimienta");
            getOrCreateIngrediente("Comino");
            getOrCreateIngrediente("Pimentón Dulce");
            getOrCreateIngrediente("Ají Molido");
            getOrCreateIngrediente("Orégano");
            getOrCreateIngrediente("Nuez Moscada");
            // Harinas
            getOrCreateIngrediente("Harina de Trigo");
            getOrCreateIngrediente("Harina Integral");
            getOrCreateIngrediente("Harina de Maíz");
            getOrCreateIngrediente("Harina de Arroz");
            getOrCreateIngrediente("Almidón de Maíz"); // Maicena
            getOrCreateIngrediente("Harina de Garbanzo");
        };
    }

    // --- Método auxiliar getOrCreateIngrediente ---
    // Este método busca un ingrediente por nombre y lo crea si no existe.
    private Ingrediente getOrCreateIngrediente(String nombre) {
        Optional<Ingrediente> ingredienteOptional = ingredienteRepo.findByNombre(nombre);
        if (ingredienteOptional.isPresent()) {
            return ingredienteOptional.get();
        } else {
            // Asumo un constructor de Ingrediente (String nombre)
            Ingrediente nuevoIngrediente = new Ingrediente(nombre);
            ingredienteRepo.save(nuevoIngrediente);
            System.out.println("Guardado nuevo ingrediente: " + nombre);
            return nuevoIngrediente;
        }
    }

    // --- NUEVO MÉTODO AUXILIAR: getOrCreateReceta ---
    // Este método busca una receta por nombre (no eliminada lógicamente) y la crea si no existe,
    // incluyendo todos sus ItemReceta y el cálculo de calorías.
    private Receta getOrCreateReceta(String nombreReceta, String descripcionReceta, List<ItemRecetaData> itemDetails) {
        // Verifica si la receta ya existe y NO está marcada como deleted = true
        //findByNombreAndDeletedFalse(String nombre);
        Optional<Receta> existingReceta = recetaRepo.findByNombreAndDeletedFalse(nombreReceta); 
        
        if (existingReceta.isPresent()) {
            System.out.println("--- Receta '" + nombreReceta + "' ya existe y no está eliminada lógicamente. Saltando inserción completa. ---");
            return existingReceta.get(); // Retorna la receta existente
        } else {
            // Si la receta NO existe o está eliminada lógicamente, procedemos a crearla junto con sus ítems.

            // 1. Creo los ItemReceta a partir de los datos proporcionados
            List<ItemReceta> itemsReceta = new ArrayList<>();
            for (ItemRecetaData itemData : itemDetails) {
                // Me aseguro que el ingrediente exista o lo creo
                Ingrediente ingrediente = getOrCreateIngrediente(itemData.ingredienteNombre());
                // Crear el ItemReceta. El último 'null' para Receta se establecerá bidireccionalmente después
                // El constructor de ItemReceta inicializa 'deleted' a false por defecto
                ItemReceta item = new ItemReceta(ingrediente, itemData.cantidad(), itemData.calorias(), null);
                itemsReceta.add(item);
            }

            // 2. Instanciar la Receta
            Receta nuevaReceta = new Receta(nombreReceta, descripcionReceta, itemsReceta);

            // Asegúrate de que la Receta también se marque como NO eliminada lógicamente.
            // Aunque el constructor de Receta ya debería hacerlo, lo reforzamos aquí para claridad en la carga inicial.
            nuevaReceta.setDeleted(false); 

            // 3. Establezco la relación bidireccional en los ItemReceta ANTES de guardarse
            for (ItemReceta item : itemsReceta) {
                item.setReceta(nuevaReceta);
            }

            // 4. Calculo calorías totales ANTES de guardar la receta
            // Este método ahora filtra por 'item.isDeleted()' automáticamente
            nuevaReceta.calcularCaloriasTotales();

            // 5. Guardar la Receta primero para que tenga un ID asignado por la base de datos
            recetaRepo.save(nuevaReceta); 
            
            // 6. Ahora que la receta tiene un ID, guardo los ItemReceta asociados.
            itemRecetaRepo.saveAll(itemsReceta); // 

            System.out.println("--- Nueva Receta '" + nombreReceta + "' guardada con sus ítems y calorías calculadas ---");
            return nuevaReceta;
        }
    }

    // --- Clase (o record) auxiliar para pasar los datos de los ItemReceta ---
    // Usamos 'record' para Java 16+ por su concisión. Si usas una versión anterior, cámbialo a una clase normal.
    private record ItemRecetaData(String ingredienteNombre, Double cantidad, Integer calorias) {}
}
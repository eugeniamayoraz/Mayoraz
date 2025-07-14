package com.tp.ong.moduloRecetas.accesoDatos;

import com.tp.ong.moduloRecetas.entidades.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRecetaRepo extends JpaRepository<Receta, Long> {

    // No pones nada si vas a usar los metodos basicos, podes escribir metodos personalizados si lo necesitas.-

    // Método para buscar una receta por nombre exacto (case-sensitive) y que NO esté eliminada lógicamente.
    // Esto reemplaza al antiguo `findByNombre`. Ahora siempre filtra por 'deleted=false'.
    Optional<Receta> findByNombreAndDeletedFalse(String nombre);

    // Con esto a dif del anterior ignora may y minusculas, y que NO esté eliminada lógicamente.
    // Este método ahora es el estándar para búsquedas por nombre que ignoran mayúsculas/minúsculas y son activas.
    Optional<Receta> findByNombreIgnoreCaseAndDeletedFalse(String nombre);

    // Método para buscar recetas cuyo nombre contenga una palabra, ignorando mayúsculas/minúsculas,
    // y que NO estén eliminadas lógicamente.
    // Esto reemplaza al antiguo `findByNombreContainingIgnoreCase`. Ahora siempre filtra por 'deleted=false'.
    List<Receta> findByNombreContainingIgnoreCaseAndDeletedFalse(String nombre);

    // MÉTODO: para buscar recetas por rango de calorías, y que NO estén eliminadas lógicamente.
    // Spring Data JPA lo traduce a algo como WHERE caloriasTotales BETWEEN ?1 AND ?2 AND deleted = false.
    // Esto reemplaza al antiguo `findByCaloriasTotalesBetween`. Ahora siempre filtra por 'deleted=false'.
    List<Receta> findByCaloriasTotalesBetweenAndDeletedFalse(Integer minCalorias, Integer maxCalorias);


    // Metodo para buscar Receta por ID ej para la edicion y eliminacion.
    // Usaremos este método para obtener una receta por ID, solo si NO está eliminada lógicamente.
    // Es el método preferido para obtener recetas para edición o visualización después de la implementación de borrado lógico.
    // NOTA: JpaRepository ya provee findById(ID), pero este es un método personalizado crucial para el borrado lógico.
    Optional<Receta> findByIdAndDeletedFalse(Long id);


    // Ya no es un "Nuevo método", ahora es el método estándar para este tipo de búsqueda.
    // Este es crucial para las validaciones y búsquedas de recetas activas.
    // Optional<Receta> findByNombreIgnoreCaseAndDeletedFalse(String nombre); // Ya lo tenemos arriba como la nueva implementación estándar.

    // --- Métodos de Listado General (siempre filtran por deleted = false) ---

    // Método para buscar todas las recetas que NO estén eliminadas lógicamente o sea las Activas
    List<Receta> findByDeletedFalse();

    //lo usa la precarga de datos
	Optional<Receta> findByNombreIgnoreCase(String nombreReceta);

    // Elimino los métodos sin "AndDeletedFalse" para forzar que todas las búsquedas de cara al usuario final
    // solo muestren resultados activos. Si en algún momento necesitas acceder a una receta eliminada
    // (ej. para un panel de administración), se debería crear un método específico como `findByNombreEvenIfDeleted(String nombre)`.

}
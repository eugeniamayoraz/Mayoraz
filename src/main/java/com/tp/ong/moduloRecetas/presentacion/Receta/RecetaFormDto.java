

package com.tp.ong.moduloRecetas.presentacion.Receta; // Ubicación sugerida

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;



/*
 * es el objeto principal que encapsula todos los datos del formulario (nombre, descripción, y la lista de ItemRecetaDto). 
 * para que Thymeleaf pueda poblar los campos del formulario
 */

public class RecetaFormDto {

    private Long id; // Este campo será nulo para nuevas recetas y contendrá el ID para la edición.
    
    @NotBlank(message = "El nombre de la receta no puede estar vacío.")
    @Size(max = 100, message = "El nombre de la receta no puede exceder los 100 caracteres.")
    private String nombre;

    @Size(max = 250, message = "La descripción no puede exceder los 250 caracteres.")
    private String descripcion;

    @Valid
    @NotEmpty(message = "La receta debe contener al menos un ingrediente.")
    // Contendrá la lista de los ítems de la receta, cada uno como un ItemRecetaDto.
    private List<ItemRecetaDto> items;

    // --- Constructores ---
    public RecetaFormDto() {
        // Constructor vacío necesario para Spring (manejo de formularios)
    }

    // Constructor con campos principales (opcional, útil para tests o inicialización rápida)
    public RecetaFormDto(Long id, String nombre, String descripcion, List<ItemRecetaDto> items) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.items = items;
    }

    // --- Getters y Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<ItemRecetaDto> getItems() {
        return items;
    }

    public void setItems(List<ItemRecetaDto> items) {
        this.items = items;
    }
}
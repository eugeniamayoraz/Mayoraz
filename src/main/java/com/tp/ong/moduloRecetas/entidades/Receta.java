package com.tp.ong.moduloRecetas.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
// import jakarta.persistence.CascadeType; // Ya no necesito CascadeType.ALL si hago borrado lógico de ItemReceta
import jakarta.persistence.Table;

import java.util.List;
import java.util.ArrayList; // 

@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @OneToMany(mappedBy = "receta") // Removed CascadeType.ALL and orphanRemoval
    private List<ItemReceta> items = new ArrayList<>(); // Inicializar para evitar NPEs

    // Nuevo atributo: caloriasTotales.
    // Lo refactorice a NO NULO a nivel de base de datos para evitar problemas con las consultas. y Le doy un valor por defecto.
    @Column(nullable = false) // Esto le dice a JPA que la columna no debe aceptar NULLs en la DB
    private Integer caloriasTotales = 0; // Valor por defecto en Java para nuevas instancias

    // Nuevo atributo: deleted, valor por defecto false
    private Boolean deleted = false;

    // Constructores

    // Vacío (necesario para JPA)
    public Receta() {
        super();
    }

    // Constructor con todos los atributos menos el ID, caloriasTotales y deleted
    public Receta(String nombre, String descripcion, List<ItemReceta> items) {
        super();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.items = (items != null) ? items : new ArrayList<>(); // Inicializar para evitar NPEs
        // Al crear, las calorías se calcularán después de añadir los ítems y guardarlos
    }

    // Constructor con todos los atributos (menos caloriasTotales y deleted)
    public Receta(Long id, String nombre, String descripcion, List<ItemReceta> items) {
        super();
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.items = (items != null) ? items : new ArrayList<>(); // Inicializar para evitar NPEs
    }

    // Getters y Setters
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

    public List<ItemReceta> getItems() {
        return items;
    }

    public void setItems(List<ItemReceta> items) {
        // Asegurarse de que la lista nunca sea null al setearla
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public Integer getCaloriasTotales() {
        return caloriasTotales;
    }

    public void setCaloriasTotales(Integer caloriasTotales) {
        //si se intenta setear a null, se setee a 0 en su lugar
        this.caloriasTotales = (caloriasTotales != null) ? caloriasTotales : 0;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    // IMPORTANTE: Este método DEBE filtrar por ItemReceta.isEliminadoLogicamente() para sumar las calorías de los ítems activos.
    public void calcularCaloriasTotales() {
        Integer totalCalorias = 0; // iniciliao el contador

        for (ItemReceta item : this.items) {
            // Solo suma si el ítem NO está marcado como eliminado
            if (!item.isEliminadoLogicamente()) { 
                // si calorias es NULL se usa 0 para la suma, p evitar NullPointerException.
                Integer caloriasDelItem = (item.getCalorias() != null) ? item.getCalorias() : 0;
                totalCalorias += caloriasDelItem;
            }
        }       
        // Asigno el total de calorías calculado al atributo 'caloriasTotales' de la receta.
        this.caloriasTotales = totalCalorias;
    }
}
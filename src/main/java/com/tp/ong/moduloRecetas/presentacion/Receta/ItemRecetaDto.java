package com.tp.ong.moduloRecetas.presentacion.Receta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin; // Para Double con valor mínimo

public class ItemRecetaDto {
	
 private Long id;

 @NotNull(message = "Debe Seleccionar un ingrediente")
 private Long ingredienteId;
 

 @NotNull(message = "La cantidad no puede ser nula.")
 @DecimalMin(value = "0.01", message = "La cantidad debe ser un valor positivo mayor que cero.") // 0.01 para permitir comas
 private Double cantidad; // Cambiado a Double

 @NotNull(message = "Las calorías no pueden ser nulas.")
 @Min(value = 0, message = "Las calorías deben ser un valor no negativo.")
 private Integer calorias;

 // Constructores, Getters y Setters (actualizar para Double cantidad)
 public ItemRecetaDto() {}

 public ItemRecetaDto(Long id, Long ingredienteId, Double cantidad, Integer calorias) {
     this.id = id;
     this.ingredienteId = ingredienteId;
     this.cantidad = cantidad;
     this.calorias = calorias;
 }

 public Long getId() { return id; }
 public void setId(Long id) { this.id = id; }
 public Long getIngredienteId() { return ingredienteId; }
 public void setIngredienteId(Long ingredienteId) { this.ingredienteId = ingredienteId; }
 public Double getCantidad() { return cantidad; }
 public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
 public Integer getCalorias() { return calorias; }
 public void setCalorias(Integer calorias) { this.calorias = calorias; }
}
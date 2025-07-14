package com.tp.ong.moduloRecetas.entidades;

import jakarta.persistence.*;

@Entity
@Table(name="items_recetas")
public class ItemReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;
    private Double cantidad;
    private Integer calorias;

    @ManyToOne
    @JoinColumn(name = "receta_id")
    private Receta receta;

    //Constructores
    
    //vacio
	public ItemReceta() {
		super();
	}
    	
	//con todos los atributos
	public ItemReceta(Long id, Ingrediente ingrediente, Double cantidad, Integer calorias, Receta receta) {
		super();
		this.id = id;
		this.ingrediente = ingrediente;
		this.cantidad = cantidad;
		this.calorias = calorias;
		this.receta = receta;
	}
	
	//con todos los atributos menos ID del item 
	public ItemReceta(Ingrediente ingrediente, Double cantidad, Integer calorias, Receta receta) {
			super();
			this.ingrediente = ingrediente;
			this.cantidad = cantidad;
			this.calorias = calorias;
			this.receta = receta;
		}

	// Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public Integer getCalorias() { return calorias; }
    public void setCalorias(Integer calorias) { this.calorias = calorias; }

    public Receta getReceta() { return receta; }
    public void setReceta(Receta receta) { this.receta = receta; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }
}

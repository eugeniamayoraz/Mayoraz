package com.tp.ong.moduloRecetas.entidades;

import jakarta.persistence.*;

@Entity
@Table(name="ingredientes")
public class Ingrediente {

    //  Atributos
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true)
    private String nombre;
    
    //private Integer calorias; no lo voy a poner aca porque aca no hay cantidad y cantidad y calorias estan en itemReceta
    
    
    //  Constructores  (no se si hacen falta los 3, revisar) 
    //vacio
    public Ingrediente() {
		super();
	}
    
	//con todos los atributos
    public Ingrediente(Long id, String nombre) {
		super();
		this.id = id;
		this.nombre = nombre;
	}
    
    //con atributos menos id
    public Ingrediente(String nombre) {
		super();
		this.nombre = nombre;
	}


	// Getters y Setters
    public Long getId() { return id; }
    
	public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    //public Integer getCalorias() { return calorias; }
    //public void setCalorias(Integer calorias) { this.calorias = calorias; }
}

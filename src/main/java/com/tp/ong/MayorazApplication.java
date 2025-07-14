package com.tp.ong;

import org.springframework.boot.CommandLineRunner;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.tp.ong.moduloRecetas.accesoDatos.*;

import com.tp.ong.moduloRecetas.entidades.*;
//import com.tp.ong.moduloRecetas.presentacion.Receta.RecetasRegistrarEditarController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; //para que acepte autowired en esta clase y yo pueda instanciar
//objetos para ya insertarlos en la base a atraves de los metodos del repo.

@SpringBootApplication


public class MayorazApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(MayorazApplication.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {
        // Aquí puedes dejarlo vacío por ahora, o poner un System.out.println
        // La lógica de carga de datos debería estar manejada por el bean CargaDeDatos
        // si ya lo tienes anotado con @Component y su constructor hace el trabajo,
        // o si tiene un método @PostConstruct.
        System.out.println("Application started and CommandLineRunner executed.");
    }
	
}

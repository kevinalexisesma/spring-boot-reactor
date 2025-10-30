package com.bolsadeideas.springboot.reactor.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.app.models.Usuario;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploFlatMap();
	}

	public void ejemploFlatMap() throws Exception {
		List<String> usuariosList = new ArrayList<>() {
			{
				add("Andres Guzman");
				add("Pedro Fulano");
				add("Diego Melano");
				add("Juan Perez");
				add("Kevin Eslava");
				add("Bruce Wayne");
				add("Bruce Lee");
			}
		};

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					}
					return Mono.empty();
				})
				.map(usuario -> {
					usuario.setNombre(usuario.getNombre().toLowerCase());
					return usuario;
				})
				.subscribe(usuario -> log.info(usuario.toString()));
	}

	public void ejemploIterable() throws Exception {
		List<String> usuariosList = new ArrayList<>() {
			{
				add("Andres Guzman");
				add("Pedro Fulano");
				add("Diego Melano");
				add("Juan Perez");
				add("Kevin Eslava");
				add("Bruce Wayne");
				add("Bruce Lee");
			}
		};

		Flux<String> nombres = Flux.fromIterable(usuariosList);

		// Flux<String> nombres = Flux.just("Andres Guzman", "Pedro Fulano", "Diego
		// Melano", "Juan Perez", "Kevin Eslava",
		// "Bruce Wayne", "Bruce Lee");

		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equalsIgnoreCase("bruce"))
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacíos.");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				})
				.map(usuario -> {
					usuario.setNombre(usuario.getNombre().toLowerCase());
					return usuario;
				});

		usuarios.subscribe(usuario -> log.info(usuario.toString()),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecución del flux con éxito.");
					}
				});
	}
}

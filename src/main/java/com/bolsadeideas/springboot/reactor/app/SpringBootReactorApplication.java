package com.bolsadeideas.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.app.models.Comentarios;
import com.bolsadeideas.springboot.reactor.app.models.Usuario;
import com.bolsadeideas.springboot.reactor.app.models.UsuarioConComentarios;

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
		ejemploContraPresion();
	}

	public void ejemploContraPresion() throws Exception {
		Flux.range(0, 10)
				.log()
				.limitRate(2)
				.subscribe(
				// new Subscriber<Integer>() {

				// private Subscription s;
				// private Integer limite = 2;
				// private Integer consumido = 0;

				// @Override
				// public void onComplete() {
				// // TODO Auto-generated method stub
				// }

				// @Override
				// public void onError(Throwable arg0) {
				// log.error(arg0.getMessage());
				// }

				// @Override
				// public void onNext(Integer arg0) {
				// log.info(arg0.toString());
				// consumido++;
				// if (consumido == limite) {
				// consumido = 0;
				// s.request(limite);
				// }
				// }

				// @Override
				// public void onSubscribe(Subscription arg0) {
				// this.s = arg0;
				// this.s.request(limite);
				// }

				// }
				);
	}

	public void ejemploIntervalDesdeCreate() throws Exception {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}
					if (contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5"));
					}
				}

			}, 1000, 1000);
		})
				.subscribe(next -> log.info(next.toString()),
						error -> log.error(error.getMessage()),
						() -> log.info("Hemos terminado!"));
	}

	public void ejemploIntervalInfinito() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(() -> latch.countDown())
				.flatMap(i -> {
					if (i >= 5)
						return Flux.error(new InterruptedException("Solo hasta 5."));
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2)
				.subscribe(tick -> log.info(tick.toString()), e -> log.error(e.getMessage()));

		latch.await();
	}

	public void ejemploDelayElements() throws Exception {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(valor -> log.info(valor.toString()));

		// rango.subscribe();
		rango.blockLast(); // Se bloquea solo para mostrar en consola.

	}

	public void ejemploInterval() throws Exception {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(retraso, (rang, retra) -> rang)
				.doOnNext(valores -> log.info(valores.toString()))
				.blockLast(); // Se bloquea solo para mostrar en consola.
	}

	public void ejemploZiWithRangos() throws Exception {
		Flux.just(1, 2, 3, 4)
				.map(numero -> numero * 2)
				.zipWith(Flux.range(0, 4),
						(original, rango) -> String.format("Primer flux: %d, segundo flux: %d", original, rango))
				.subscribe(texto -> log.info(texto));
	}

	public void ejemploUsuarioConComentariosZiWithForma2() throws Exception {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Marston"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.agregarComentario("Este es el primer comentario");
			comentarios.agregarComentario("Este es el segundo comentario");
			comentarios.agregarComentario("Este es el tercer comentario");
			return comentarios;
		});

		Mono<UsuarioConComentarios> usuarioConComentariosMono = usuarioMono
				.zipWith(comentariosMono)
				.map(tuple -> new UsuarioConComentarios(tuple.getT1(), tuple.getT2()));

		usuarioConComentariosMono.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioConComentariosZiWith() throws Exception {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Marston"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.agregarComentario("Este es el primer comentario");
			comentarios.agregarComentario("Este es el segundo comentario");
			comentarios.agregarComentario("Este es el tercer comentario");
			return comentarios;
		});

		Mono<UsuarioConComentarios> usuarioConComentariosMono = usuarioMono
				.zipWith(comentariosMono, (usuario, comentarios) -> new UsuarioConComentarios(usuario, comentarios));

		usuarioConComentariosMono.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioConComentariosFlatMap() throws Exception {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Marston"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.agregarComentario("Este es el primer comentario");
			comentarios.agregarComentario("Este es el segundo comentario");
			comentarios.agregarComentario("Este es el tercer comentario");
			return comentarios;
		});

		usuarioMono
				.flatMap(
						usuario -> comentariosMono.map(comentarios -> new UsuarioConComentarios(usuario, comentarios)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploToCollectList() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>() {
			{
				add(new Usuario("Andres", "Guzman"));
				add(new Usuario("Pedro", "Fulano"));
				add(new Usuario("Diego", "Melano"));
				add(new Usuario("Juan", "Perez"));
				add(new Usuario("Kevin", "Eslava"));
				add(new Usuario("Bruce", "Wayne"));
				add(new Usuario("Bruce", "Lee"));
			}
		};

		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(usuario -> log.info(usuario.toString()));
				});
	}

	public void ejemploToString() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>() {
			{
				add(new Usuario("Andres", "Guzman"));
				add(new Usuario("Pedro", "Fulano"));
				add(new Usuario("Diego", "Melano"));
				add(new Usuario("Juan", "Perez"));
				add(new Usuario("Kevin", "Eslava"));
				add(new Usuario("Bruce", "Wayne"));
				add(new Usuario("Bruce", "Lee"));
			}
		};

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ")
						.concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombres -> {
					if (nombres.contains("BRUCE")) {
						return Mono.just(nombres);
					}
					return Mono.empty();
				})
				.map(nombres -> nombres.toLowerCase())
				.subscribe(nombres -> log.info(nombres));
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

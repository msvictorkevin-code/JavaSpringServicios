package com.bolsadeideas.springboot.reactor;

import com.bolsadeideas.springboot.reactor.models.Comentarios;
import com.bolsadeideas.springboot.reactor.models.Usuario;
import com.bolsadeideas.springboot.reactor.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        //ejemploFlatMap();
        // ejemploToString();
        //ejemploCollectList();
        //ejemploUsuarioComentariosFlatMap();
        //ejemploUsuarioComentariosZipWith();
        // ejemploZipWithRangos();
        //ejemploInterval();
        //ejemploDelayElements();
        //ejemploIntervalInfinito();
        //ejemploIntervalDesdeCreate();
        ejemploContraPresion();
    }

    public void ejemploContraPresion() {
        Flux.range(1, 10)
                .log()
                .limitRate(5)
                .subscribe(new Subscriber<Integer>() {
                    private Subscription subscription;
                    private Integer limite = 5;
                    private Integer consumido = 0;


                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        s.request(limite);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        logger.info(integer.toString());
                        consumido++;
                        if (consumido == limite) {
                            consumido = 0;
                            subscription.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void ejemploIntervalDesdeCreate() {
        Flux.create(emmiter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                private Integer contador = 0;

                @Override
                public void run() {
                    emmiter.next(++contador);
                    if (contador == 10) {
                        timer.cancel();
                        emmiter.complete();
                    }

                    if (contador == 5) {
                        timer.cancel();
                        emmiter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
                    }
                }
            }, 100, 100);
        }).subscribe(next -> logger.info(next.toString()), error -> logger.error(error.getMessage()), () -> logger.info("Hemos terminado."));
    }

    public void ejemploIntervalInfinito() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(countDownLatch::countDown)
                .flatMap(i -> {
                    if (i >= 5) {
                        return Flux.error(new InterruptedException("Solo hasta 5!"));
                    }
                    return Flux.just(i);
                }).map(i -> "Hola" + i)
                .retry(2)
                .subscribe(s -> logger.info(s), e -> logger.error(e.getMessage()));

        countDownLatch.await();
    }

    public void ejemploDelayElements() {
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> logger.info(i.toString()));

        rango.blockLast();

    }

    public void ejemploInterval() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
        rango.zipWith(retraso, (ra, re) -> ra)
                .doOnNext(i -> logger.info(i.toString()))
                .blockLast();
    }


    public void ejemploZipWithRangos() {
        Flux.just(1, 2, 3, 4)
                .map(i -> (i * 2))
                .zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
                .subscribe(s -> logger.info(s));
    }

    public void ejemploUsuarioComentariosZipWithForma2() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
                    Comentarios comentarios = new Comentarios();
                    comentarios.addComentario("Hola pepe, que tal!");
                    comentarios.addComentario("Mañana voya a la playa");
                    comentarios.addComentario("Estoy tomando el curso de spring reactor");
                    return comentarios;
                }
        );


        Mono<UsuarioComentarios> usuarioComentarios = usuarioMono
                .zipWith(comentariosUsuarioMono)
                .map(tuple -> {
                    Usuario u = tuple.getT1();
                    Comentarios c = tuple.getT2();

                    return new UsuarioComentarios(u, c);
                });


        usuarioComentarios.subscribe(uc -> System.out.println(uc));
    }

    public void ejemploUsuarioComentariosZipWith() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
                    Comentarios comentarios = new Comentarios();
                    comentarios.addComentario("Hola pepe, que tal!");
                    comentarios.addComentario("Mañana voya a la playa");
                    comentarios.addComentario("Estoy tomando el curso de spring reactor");
                    return comentarios;
                }
        );


        Mono<UsuarioComentarios> usuarioComentarios = usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> new UsuarioComentarios(usuarioMono.block(), comentariosUsuarioMono.block()));


        usuarioComentarios.subscribe(uc -> System.out.println(uc));
    }

    public void ejemploUsuarioComentariosFlatMap() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
                    Comentarios comentarios = new Comentarios();
                    comentarios.addComentario("Hola pepe, que tal!");
                    comentarios.addComentario("Mañana voya a la playa");
                    comentarios.addComentario("Estoy tomando el curso de spring reactor");
                    return comentarios;
                }
        );

        /*usuarioMono.flatMap(usuario -> comentariosUsuarioMono.map(comentarios -> new UsuarioComentarios(usuario, comentarios)))
                .subscribe(usuarioComentarios -> logger.info(usuarioComentarios.toString()));*/

        Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono.flatMap(usuario -> comentariosUsuarioMono.map(comentarios -> new UsuarioComentarios(usuario, comentarios)));
        usuarioComentariosMono.subscribe(usuarioComentarios -> System.out.println(usuarioComentarios));
    }


    public void ejemploCollectList() throws

            Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Andres", "Guzman"));
        usuariosList.add(new Usuario("Pedro", "Fulano"));
        usuariosList.add(new Usuario("Diego", "Fulano"));
        usuariosList.add(new Usuario("Juan", "Mengana"));
        usuariosList.add(new Usuario("Maria", "Fulana"));
        usuariosList.add(new Usuario("Bruce", "Lee"));
        usuariosList.add(new Usuario("Bruce", "Willis"));

        Flux.fromIterable(usuariosList).collectList().subscribe(usuarios -> System.out.println(usuarios));
    }


    public void ejemploToString() throws

            Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Andres", "Guzman"));
        usuariosList.add(new Usuario("Pedro", "Fulano"));
        usuariosList.add(new Usuario("Diego", "Fulano"));
        usuariosList.add(new Usuario("Juan", "Mengana"));
        usuariosList.add(new Usuario("Maria", "Fulana"));
        usuariosList.add(new Usuario("Bruce", "Lee"));
        usuariosList.add(new Usuario("Bruce", "Willis"));


        Flux.fromIterable(usuariosList) // Flux.just("Andres Guzman", "Pedro Fulano", "Diego Fulano", "Juan Mengana", "Maria Fulana", "Bruce Lee", "Bruce Willis")
                .map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
                .flatMap(usuario -> {
                    if (usuario.contains("bruce".toUpperCase())) {
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(usuario -> {
                    //usuario.toLowerCase();
                    return usuario.toLowerCase();
                }).subscribe(s -> logger.info(s));
    }

    public void ejemploFlatMap() throws

            Exception {

        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres Guzman");
        usuariosList.add("Pedro Fulano");
        usuariosList.add("Diego Fulano");
        usuariosList.add("Juan Mengana");
        usuariosList.add("Maria Fulana");
        usuariosList.add("Bruce Lee");
        usuariosList.add("Bruce Willis");


        Flux.fromIterable(usuariosList) // Flux.just("Andres Guzman", "Pedro Fulano", "Diego Fulano", "Juan Mengana", "Maria Fulana", "Bruce Lee", "Bruce Willis")
                .map(nombre1 -> {
                            return new Usuario(nombre1.split(" ")[0].toUpperCase(), nombre1.split(" ")[1].toUpperCase());
                        }
                )
                .flatMap(usuario -> {
                    if (usuario.getNombre().toLowerCase().equals("bruce")) {
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }

                })
                .filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
                .doOnNext(usuario -> // Notifica por elemento de flujo
                        {
                            if (usuario == null) {
                                throw new RuntimeException("Nombres no pueden ser vacios");
                            } else {
                                System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                            }

                        }
                ).map(usuario -> {
            usuario.setNombre(usuario.getNombre().toLowerCase());
            return usuario;
        }).subscribe(s -> logger.info(s.toString()));
    }

    public void ejemploIterable() throws Exception {

        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres Guzman");
        usuariosList.add("Pedro Fulano");
        usuariosList.add("Diego Fulano");
        usuariosList.add("Juan Mengana");
        usuariosList.add("Maria Fulana");
        usuariosList.add("Bruce Lee");
        usuariosList.add("Bruce Willis");

        Flux<String> nombres = Flux.fromIterable(usuariosList);


        Flux<Usuario> usuarios = nombres // Flux.just("Andres Guzman", "Pedro Fulano", "Diego Fulano", "Juan Mengana", "Maria Fulana", "Bruce Lee", "Bruce Willis")
                .map(nombre1 -> {
                            return new Usuario(nombre1.split(" ")[0].toUpperCase(), nombre1.split(" ")[1].toUpperCase());
                        }
                )
                .filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
                .doOnNext(usuario -> // Notifica por elemento de flujo
                        {
                            if (usuario == null) {
                                throw new RuntimeException("Nombres no pueden ser vacios");
                            } else {
                                System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                            }

                        }
                ).map(usuario -> {
                    usuario.setNombre(usuario.getNombre().toLowerCase());
                    return usuario;
                });
        usuarios.subscribe(s -> logger.info(s.toString()),
                error -> logger.error(error.getMessage()),
                new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Ha finalizado la ejecucion del observable con exito.");
                    }
                }
        );
    }
}

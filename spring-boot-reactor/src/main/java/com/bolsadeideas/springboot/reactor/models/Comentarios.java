package com.bolsadeideas.springboot.reactor.models;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {

    private List<String> comentarios;


    public Comentarios() {
        this.comentarios = new ArrayList<>();
    }

    public Comentarios(List<String> comentarios) {
        this.comentarios = comentarios;
    }

    public void addComentario(String comentarios) {
        this.comentarios.add(comentarios);
    }

    @Override
    public String toString() {
        return "comentarios=" + comentarios;
    }
}

package com.sga.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Agenda {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty fecha = new SimpleStringProperty();
    private final StringProperty hora = new SimpleStringProperty();
    private Taller taller;
    private Usuario responsable;

    public Agenda() {}

    public Agenda(long id, String fecha, String hora, Taller taller, Usuario responsable) {
        this.id.set(id);
        this.fecha.set(fecha);
        this.hora.set(hora);
        this.taller = taller;
        this.responsable = responsable;
    }

    // ID
    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }
    public void setId(long id) { this.id.set(id); }

    // Fecha
    public String getFecha() { return fecha.get(); }
    public StringProperty fechaProperty() { return fecha; }
    public void setFecha(String fecha) { this.fecha.set(fecha); }

    // Hora
    public String getHora() { return hora.get(); }
    public StringProperty horaProperty() { return hora; }
    public void setHora(String hora) { this.hora.set(hora); }

    // Taller
    public Taller getTaller() { return taller; }
    public void setTaller(Taller taller) { this.taller = taller; }

    // Responsable
    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }
}

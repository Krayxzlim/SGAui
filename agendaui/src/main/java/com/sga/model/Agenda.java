package com.sga.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Agenda {

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty fecha = new SimpleStringProperty();
    private final StringProperty hora = new SimpleStringProperty();
    private final StringProperty taller = new SimpleStringProperty();
    private final StringProperty responsable = new SimpleStringProperty();

    public Agenda() {}

    public Agenda(String id, String fecha, String hora, String taller, String responsable) {
        this.id.set(id);
        this.fecha.set(fecha);
        this.hora.set(hora);
        this.taller.set(taller);
        this.responsable.set(responsable);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }
    public void setId(String id) { this.id.set(id); }

    public String getFecha() { return fecha.get(); }
    public StringProperty fechaProperty() { return fecha; }
    public void setFecha(String fecha) { this.fecha.set(fecha); }

    public String getHora() { return hora.get(); }
    public StringProperty horaProperty() { return hora; }
    public void setHora(String hora) { this.hora.set(hora); }

    public String getTaller() { return taller.get(); }
    public StringProperty tallerProperty() { return taller; }
    public void setTaller(String taller) { this.taller.set(taller); }

    public String getResponsable() { return responsable.get(); }
    public StringProperty responsableProperty() { return responsable; }
    public void setResponsable(String responsable) { this.responsable.set(responsable); }
}

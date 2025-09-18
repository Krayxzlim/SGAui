package com.sga.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Colegio {

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty direccion = new SimpleStringProperty();
    private final StringProperty contacto = new SimpleStringProperty();

    public Colegio() {}

    public Colegio(String id, String nombre, String direccion, String contacto) {
        this.id.set(id);
        this.nombre.set(nombre);
        this.direccion.set(direccion);
        this.contacto.set(contacto);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }
    public void setId(String id) { this.id.set(id); }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }
    public void setNombre(String nombre) { this.nombre.set(nombre); }

    public String getDireccion() { return direccion.get(); }
    public StringProperty direccionProperty() { return direccion; }
    public void setDireccion(String direccion) { this.direccion.set(direccion); }

    public String getContacto() { return contacto.get(); }
    public StringProperty contactoProperty() { return contacto; }
    public void setContacto(String contacto) { this.contacto.set(contacto); }
}

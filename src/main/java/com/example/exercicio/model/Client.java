package com.example.exercicio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String taxIdentifier;

    private String email;

    private String phoneNumber;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    public Client(String firstName, String lastName, String taxIdentifier, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.taxIdentifier = taxIdentifier;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public void addDocument(Document document) {
        documents.add(document);
        document.setClient(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setClient(null);
    }
}

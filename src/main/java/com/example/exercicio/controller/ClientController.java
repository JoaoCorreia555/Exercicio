package com.example.exercicio.controller;

import com.example.exercicio.dto.ClientDTO;
import com.example.exercicio.dto.DocumentDTO;
import com.example.exercicio.model.Client;
import com.example.exercicio.model.Document;
import com.example.exercicio.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clients")
@Tag(name = "Clients", description = "Operations related to clients and their documents")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    private Client dtoToEntity(ClientDTO dto) {
        Client c = new Client(dto.getFirstName(), dto.getLastName(), dto.getTaxIdentifier(), dto.getEmail(), dto.getPhoneNumber());
        if (dto.getDocuments() != null) {
            dto.getDocuments().forEach(d -> c.addDocument(new Document(d.getType(), d.getNumber(), d.getDescription(), d.getExpirationDate(), c)));
        }
        return c;
    }

    private ClientDTO entityToDto(Client entity) {
        List<DocumentDTO> docs = entity.getDocuments().stream()
                .map(d -> new DocumentDTO(d.getId(), d.getType(), d.getNumber(), d.getDescription(), d.getExpirationDate()))
                .collect(Collectors.toList());
        return new ClientDTO(entity.getId(), entity.getFirstName(), entity.getLastName(), entity.getTaxIdentifier(), entity.getEmail(), entity.getPhoneNumber(), docs);
    }

    @Operation(summary = "Create a new client")
    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO) {
        Client toSave = dtoToEntity(clientDTO);
        Client saved = clientService.saveClientWithDocuments(toSave);
        ClientDTO response = entityToDto(saved);
        return ResponseEntity.created(URI.create("/clients/" + saved.getId())).body(response);
    }

    @Operation(summary = "Get all clients")
    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> list = clientService.findAll().stream().map(this::entityToDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get client by id")
    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        Optional<Client> found = clientService.findById(id);
        return found.map(c -> ResponseEntity.ok(entityToDto(c))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update an existing client")
    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @RequestBody ClientDTO clientDTO) {
        Client updatedEntity = dtoToEntity(clientDTO);
        Optional<Client> updated = clientService.updateClient(id, updatedEntity);
        return updated.map(c -> ResponseEntity.ok(entityToDto(c))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a client by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

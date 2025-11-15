package com.example.exercicio.service;

import com.example.exercicio.dto.ClientDTO;
import com.example.exercicio.dto.DocumentDTO;
import com.example.exercicio.exception.ClientAlreadyExistsException;
import com.example.exercicio.exception.ClientNotFoundException;
import com.example.exercicio.model.Client;
import com.example.exercicio.model.Document;
import com.example.exercicio.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

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

    @Transactional
    public ClientDTO saveClient(ClientDTO clientDTO) {
        if (clientRepository.findByTaxIdentifier(clientDTO.getTaxIdentifier()).isPresent()) {
            throw new ClientAlreadyExistsException("Client with tax identifier " + clientDTO.getTaxIdentifier() + " already exists");
        }

        Client toSave = dtoToEntity(clientDTO);
        Client saved = clientRepository.save(toSave);
        return entityToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientDTO findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        return entityToDto(client);
    }

    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        if (!existing.getTaxIdentifier().equals(clientDTO.getTaxIdentifier())) {
            Optional<Client> existingWithSameTax = clientRepository.findByTaxIdentifier(clientDTO.getTaxIdentifier());
            if (existingWithSameTax.isPresent() && !existingWithSameTax.get().getId().equals(id)) {
                throw new ClientAlreadyExistsException("Client with tax identifier " + clientDTO.getTaxIdentifier() + " already exists");
            }
        }

        existing.setFirstName(clientDTO.getFirstName());
        existing.setLastName(clientDTO.getLastName());
        existing.setTaxIdentifier(clientDTO.getTaxIdentifier());
        existing.setEmail(clientDTO.getEmail());
        existing.setPhoneNumber(clientDTO.getPhoneNumber());

        existing.getDocuments().clear();
        if (clientDTO.getDocuments() != null) {
            clientDTO.getDocuments().forEach(d -> existing.addDocument(
                    new Document(d.getType(), d.getNumber(), d.getDescription(), d.getExpirationDate(), existing)));
        }

        Client updated = clientRepository.save(existing);
        return entityToDto(updated);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }
        clientRepository.deleteById(id);
    }
}

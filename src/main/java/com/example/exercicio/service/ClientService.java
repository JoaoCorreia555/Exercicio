package com.example.exercicio.service;

import com.example.exercicio.model.Client;
import com.example.exercicio.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;


    @Transactional
    public Client saveClientWithDocuments(Client client) {
        return clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    @Transactional
    public Optional<Client> updateClient(Long id, Client updated) {
        return clientRepository.findById(id).map(existing -> {
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setTaxIdentifier(updated.getTaxIdentifier());
            existing.setEmail(updated.getEmail());
            existing.setPhoneNumber(updated.getPhoneNumber());

            existing.getDocuments().clear();
            if (updated.getDocuments() != null) {
                updated.getDocuments().forEach(existing::addDocument);
            }

            return clientRepository.save(existing);
        });
    }

    @Transactional
    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }
}

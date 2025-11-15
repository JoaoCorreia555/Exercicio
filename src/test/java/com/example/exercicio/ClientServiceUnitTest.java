package com.example.exercicio;

import com.example.exercicio.dto.ClientDTO;
import com.example.exercicio.dto.DocumentDTO;
import com.example.exercicio.exception.ClientAlreadyExistsException;
import com.example.exercicio.exception.ClientNotFoundException;
import com.example.exercicio.model.Client;
import com.example.exercicio.model.Document;
import com.example.exercicio.repository.ClientRepository;
import com.example.exercicio.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceUnitTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientDTO inputClientDTO;
    private Client savedClient;
    private DocumentDTO documentDTO;
    private Document document;

    @BeforeEach
    void setup() {
        // Create test data
        documentDTO = new DocumentDTO(null, "passport", "P123456", "Valid passport", LocalDate.now().plusYears(5));
        inputClientDTO = new ClientDTO(null, "John", "Doe", "123456789", "john.doe@email.com", "+1234567890", List.of(documentDTO));

        document = new Document("passport", "P123456", "Valid passport", LocalDate.now().plusYears(5), null);
        document.setId(1L);

        savedClient = new Client("John", "Doe", "123456789", "john.doe@email.com", "+1234567890");
        savedClient.setId(1L);
        savedClient.setDocuments(new ArrayList<>());
        savedClient.getDocuments().add(document);
        document.setClient(savedClient);
    }

    // SAVE CLIENT TESTS

    @Test
    void saveClient_Success() {
        // Given
        when(clientRepository.findByTaxIdentifier("123456789")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // When
        ClientDTO result = clientService.saveClient(inputClientDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getTaxIdentifier()).isEqualTo("123456789");
        assertThat(result.getEmail()).isEqualTo("john.doe@email.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(result.getDocuments()).hasSize(1);
        assertThat(result.getDocuments().get(0).getType()).isEqualTo("passport");

        verify(clientRepository).findByTaxIdentifier("123456789");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void saveClient_ThrowsClientAlreadyExistsException_WhenTaxIdentifierExists() {
        // Given
        Client existingClient = new Client("Jane", "Smith", "123456789", "jane@email.com", "+0987654321");
        when(clientRepository.findByTaxIdentifier("123456789")).thenReturn(Optional.of(existingClient));

        // When & Then
        assertThatThrownBy(() -> clientService.saveClient(inputClientDTO))
                .isInstanceOf(ClientAlreadyExistsException.class)
                .hasMessage("Client with tax identifier 123456789 already exists");

        verify(clientRepository).findByTaxIdentifier("123456789");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void saveClient_Success_WithoutDocuments() {
        // Given
        ClientDTO clientWithoutDocs = new ClientDTO(null, "John", "Doe", "123456789", "john.doe@email.com", "+1234567890", new ArrayList<>());
        Client savedClientWithoutDocs = new Client("John", "Doe", "123456789", "john.doe@email.com", "+1234567890");
        savedClientWithoutDocs.setId(1L);
        savedClientWithoutDocs.setDocuments(new ArrayList<>());

        when(clientRepository.findByTaxIdentifier("123456789")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(savedClientWithoutDocs);

        // When
        ClientDTO result = clientService.saveClient(clientWithoutDocs);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocuments()).isEmpty();
        verify(clientRepository).save(any(Client.class));
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void findAll_Success() {
        // Given
        Client client2 = new Client("Jane", "Smith", "987654321", "jane@email.com", "+0987654321");
        client2.setId(2L);
        client2.setDocuments(new ArrayList<>());

        List<Client> clients = Arrays.asList(savedClient, client2);
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        List<ClientDTO> result = clientService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getFirstName()).isEqualTo("Jane");

        verify(clientRepository).findAll();
    }

    @Test
    void findAll_EmptyList() {
        // Given
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ClientDTO> result = clientService.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(clientRepository).findAll();
    }

    //FIND BY ID TESTS

    @Test
    void findById_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));

        // When
        ClientDTO result = clientService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getDocuments()).hasSize(1);

        verify(clientRepository).findById(1L);
    }

    @Test
    void findById_ThrowsClientNotFoundException() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> clientService.findById(999L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Client with id 999 not found");

        verify(clientRepository).findById(999L);
    }

    //UPDATE CLIENT TESTS

    @Test
    void updateClient_Success() {
        // Given
        ClientDTO updateDTO = new ClientDTO(1L, "John Updated", "Doe Updated", "123456789", "john.updated@email.com", "+1111111111", List.of(documentDTO));

        Client updatedClient = new Client("John Updated", "Doe Updated", "123456789", "john.updated@email.com", "+1111111111");
        updatedClient.setId(1L);
        updatedClient.setDocuments(new ArrayList<>());
        Document updatedDoc = new Document("passport", "P123456", "Valid passport", LocalDate.now().plusYears(5), updatedClient);
        updatedDoc.setId(1L);
        updatedClient.getDocuments().add(updatedDoc);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);

        // When
        ClientDTO result = clientService.updateClient(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John Updated");
        assertThat(result.getLastName()).isEqualTo("Doe Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@email.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+1111111111");

        verify(clientRepository).findById(1L);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateClient_ThrowsClientNotFoundException() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> clientService.updateClient(999L, inputClientDTO))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Client with id 999 not found");

        verify(clientRepository).findById(999L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClient_ThrowsClientAlreadyExistsException_WhenChangingTaxIdentifierToExistingOne() {
        // Given
        ClientDTO updateDTO = new ClientDTO(1L, "John", "Doe", "987654321", "john@email.com", "+1234567890", List.of(documentDTO));

        Client otherClient = new Client("Other", "Client", "987654321", "other@email.com", "+0000000000");
        otherClient.setId(2L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(clientRepository.findByTaxIdentifier("987654321")).thenReturn(Optional.of(otherClient));

        // When & Then
        assertThatThrownBy(() -> clientService.updateClient(1L, updateDTO))
                .isInstanceOf(ClientAlreadyExistsException.class)
                .hasMessage("Client with tax identifier 987654321 already exists");

        verify(clientRepository).findById(1L);
        verify(clientRepository).findByTaxIdentifier("987654321");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClient_Success_WhenKeepingSameTaxIdentifier() {
        // Given
        ClientDTO updateDTO = new ClientDTO(1L, "John Updated", "Doe Updated", "123456789", "john.updated@email.com", "+1111111111", List.of(documentDTO));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // When
        ClientDTO result = clientService.updateClient(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).findByTaxIdentifier(anyString());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateClient_Success_WhenChangingTaxIdentifierToNonExistingOne() {
        // Given
        ClientDTO updateDTO = new ClientDTO(1L, "John", "Doe", "NEW_TAX_ID", "john@email.com", "+1234567890", List.of(documentDTO));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(clientRepository.findByTaxIdentifier("NEW_TAX_ID")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // When
        ClientDTO result = clientService.updateClient(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(clientRepository).findById(1L);
        verify(clientRepository).findByTaxIdentifier("NEW_TAX_ID");
        verify(clientRepository).save(any(Client.class));
    }

    //DELETE CLIENT TESTS

    @Test
    void deleteById_Success() {
        // Given
        when(clientRepository.existsById(1L)).thenReturn(true);

        // When
        clientService.deleteById(1L);

        // Then
        verify(clientRepository).existsById(1L);
        verify(clientRepository).deleteById(1L);
    }

    @Test
    void deleteById_ThrowsClientNotFoundException() {
        // Given
        when(clientRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> clientService.deleteById(999L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Client with id 999 not found");

        verify(clientRepository).existsById(999L);
        verify(clientRepository, never()).deleteById(999L);
    }
}

package com.example.exercicio;

import com.example.exercicio.controller.ClientController;
import com.example.exercicio.dto.ClientDTO;
import com.example.exercicio.dto.DocumentDTO;
import com.example.exercicio.exception.ClientAlreadyExistsException;
import com.example.exercicio.exception.ClientNotFoundException;
import com.example.exercicio.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ClientControllerUnitTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ClientDTO inputClientDTO;
    private ClientDTO savedClientDTO;
    private DocumentDTO documentDTO;
    private DocumentDTO savedDocumentDTO;

    @BeforeEach
    void setup() {
        // Configure ObjectMapper
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();

        // Create DTOs
        documentDTO = new DocumentDTO(null, "passport", "P123456", "Valid passport", LocalDate.now().plusYears(5));
        savedDocumentDTO = new DocumentDTO(1L, "passport", "P123456", "Valid passport", LocalDate.now().plusYears(5));

        inputClientDTO = new ClientDTO(null, "John", "Doe", "123456789", "john.doe@email.com", "+1234567890", List.of(documentDTO));
        savedClientDTO = new ClientDTO(1L, "John", "Doe", "123456789", "john.doe@email.com", "+1234567890", List.of(savedDocumentDTO));
    }

    // CREATE CLIENT TESTS

    @Test
    void createClient_Success() throws Exception {
        when(clientService.saveClient(any(ClientDTO.class))).thenReturn(savedClientDTO);

        mockMvc.perform(post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputClientDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/clients/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.taxIdentifier").value("123456789"))
                .andExpect(jsonPath("$.email").value("john.doe@email.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.documents[0].id").value(1))
                .andExpect(jsonPath("$.documents[0].type").value("passport"));

        verify(clientService).saveClient(any(ClientDTO.class));
    }

    @Test
    void createClient_ClientAlreadyExists() throws Exception {
        when(clientService.saveClient(any(ClientDTO.class)))
                .thenThrow(new ClientAlreadyExistsException("Client with tax identifier 123456789 already exists"));

        mockMvc.perform(post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputClientDTO)))
                .andExpect(status().isConflict());

        verify(clientService).saveClient(any(ClientDTO.class));
    }

    //GET ALL CLIENTS TESTS

    @Test
    void getAllClients_Success() throws Exception {
        ClientDTO client2 = new ClientDTO(2L, "Jane", "Smith", "987654321", "jane.smith@email.com", "+0987654321", Collections.emptyList());
        List<ClientDTO> clients = Arrays.asList(savedClientDTO, client2);

        when(clientService.findAll()).thenReturn(clients);

        mockMvc.perform(get("/clients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        verify(clientService).findAll();
    }

    @Test
    void getAllClients_EmptyList() throws Exception {
        when(clientService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/clients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(clientService).findAll();
    }

    //GET CLIENT BY ID TESTS

    @Test
    void getClientById_Success() throws Exception {
        when(clientService.findById(1L)).thenReturn(savedClientDTO);

        mockMvc.perform(get("/clients/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.documents[0].type").value("passport"));

        verify(clientService).findById(1L);
    }

    @Test
    void getClientById_NotFound() throws Exception {
        when(clientService.findById(999L)).thenThrow(new ClientNotFoundException(999L));

        mockMvc.perform(get("/clients/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(clientService).findById(999L);
    }

    //UPDATE CLIENT TESTS

    @Test
    void updateClient_Success() throws Exception {
        ClientDTO updatedClient = new ClientDTO(1L, "John Updated", "Doe Updated", "123456789", "john.updated@email.com", "+1111111111", List.of(savedDocumentDTO));

        when(clientService.updateClient(eq(1L), any(ClientDTO.class))).thenReturn(updatedClient);

        mockMvc.perform(put("/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputClientDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John Updated"))
                .andExpect(jsonPath("$.lastName").value("Doe Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@email.com"));

        verify(clientService).updateClient(eq(1L), any(ClientDTO.class));
    }

    @Test
    void updateClient_NotFound() throws Exception {
        when(clientService.updateClient(eq(999L), any(ClientDTO.class)))
                .thenThrow(new ClientNotFoundException(999L));

        mockMvc.perform(put("/clients/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputClientDTO)))
                .andExpect(status().isNotFound());

        verify(clientService).updateClient(eq(999L), any(ClientDTO.class));
    }

    @Test
    void updateClient_TaxIdentifierAlreadyExists() throws Exception {
        when(clientService.updateClient(eq(1L), any(ClientDTO.class)))
                .thenThrow(new ClientAlreadyExistsException("Client with tax identifier 123456789 already exists"));

        mockMvc.perform(put("/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputClientDTO)))
                .andExpect(status().isConflict());

        verify(clientService).updateClient(eq(1L), any(ClientDTO.class));
    }

    //DELETE CLIENT TESTS

    @Test
    void deleteClient_Success() throws Exception {
        doNothing().when(clientService).deleteById(1L);

        mockMvc.perform(delete("/clients/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(clientService).deleteById(1L);
    }

    @Test
    void deleteClient_NotFound() throws Exception {
        doThrow(new ClientNotFoundException(999L)).when(clientService).deleteById(999L);

        mockMvc.perform(delete("/clients/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }
}

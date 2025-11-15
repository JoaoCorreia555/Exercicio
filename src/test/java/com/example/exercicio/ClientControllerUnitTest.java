package com.example.exercicio;

import com.example.exercicio.controller.ClientController;
import com.example.exercicio.dto.ClientDTO;
import com.example.exercicio.dto.DocumentDTO;
import com.example.exercicio.model.Client;
import com.example.exercicio.model.Document;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ClientControllerUnitTest {

    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ClientDTO sampleDto;
    private Client sampleEntity;

    @BeforeEach
    void setup() {
        // configure object mapper to support Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();

        DocumentDTO docDto = new DocumentDTO(null, "passport", "P123", "desc", LocalDate.now().plusYears(1));
        sampleDto = new ClientDTO(null, "First", "Last", "TAX1", "a@b.com", "123", List.of(docDto));

        Document doc = new Document("passport", "P123", "desc", LocalDate.now().plusYears(1), null);
        sampleEntity = new Client("First", "Last", "TAX1", "a@b.com", "123");
        sampleEntity.addDocument(doc);
        sampleEntity.setId(1L);
    }

    @Test
    void createClientHappyPath() throws Exception {
        when(clientService.saveClientWithDocuments(any(Client.class))).thenReturn(sampleEntity);

        mockMvc.perform(post("/clients").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(sampleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllClientsHappyPath() throws Exception {
        when(clientService.findAll()).thenReturn(List.of(sampleEntity));

        mockMvc.perform(get("/clients").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getClientByIdHappyPath() throws Exception {
        when(clientService.findById(1L)).thenReturn(Optional.of(sampleEntity));

        mockMvc.perform(get("/clients/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateClientHappyPath() throws Exception {
        when(clientService.updateClient(eq(1L), any(Client.class))).thenReturn(Optional.of(sampleEntity));

        mockMvc.perform(put("/clients/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(sampleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteClientHappyPath() throws Exception {
        doNothing().when(clientService).deleteById(1L);

        mockMvc.perform(delete("/clients/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}

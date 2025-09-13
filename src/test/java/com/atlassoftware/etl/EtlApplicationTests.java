package com.atlassoftware.etl;

import com.atlassoftware.etl.model.Order;
import com.atlassoftware.etl.repository.OrderRepository;
import com.atlassoftware.etl.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EtlApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService OrderService;

    @Autowired
    private OrderRepository OrderRepository;

    private final String source = "src/main/java/com/atlassoftware/etl/data/transactions.csv";

    @Test
    void contextLoads() {
        Assertions.assertNotNull(OrderService);
        Assertions.assertNotNull(OrderRepository);
    }

    @Test
    void mustUploadFileAndReturn201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",                       // nome do campo esperado no controller
                "transactions.csv",           // nome do arquivo
                MediaType.TEXT_PLAIN_VALUE,   // tipo do arquivo
                this.getClass().getResourceAsStream("/transactions.csv") // precisa estar em src/test/resources
        );

        mockMvc.perform(multipart("/savefile")
                        .file(file)
                        .with(csrf())
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isCreated());
    }

    @Test
    void mustReturn400WhenSendEmptyBody() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",                // precisa bater com @RequestParam("file") do controller
                "testfile.csv",        // nome do arquivo
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]            // conteúdo vazio
        );

        mockMvc.perform(multipart("/savefile")
                        .file(emptyFile)
                        .with(csrf())
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isBadRequest()); // ou isForbidden() se você decidiu 403
    }

    @Test
    void mustPersistOnlyActiveUsersFromUploadedFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.csv",
                MediaType.TEXT_PLAIN_VALUE,
                this.getClass().getResourceAsStream("/transactions.csv")
        );

        mockMvc.perform(multipart("/savefile")
                        .file(file)
                        .with(csrf())
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isCreated());

        List<Order> orders = OrderRepository.findAll();

        Assertions.assertFalse(orders.isEmpty(), "A lista não deve estar vazia");
        Assertions.assertTrue(orders.stream().allMatch(Order::getActive), "Somente usuários ativos devem ser salvos");
    }
}

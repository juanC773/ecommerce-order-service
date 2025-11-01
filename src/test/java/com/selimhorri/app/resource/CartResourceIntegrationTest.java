package com.selimhorri.app.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.CartRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CartResource Integration Tests")
class CartResourceIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CartRepository cartRepository;

	@MockBean
	private RestTemplate restTemplate;

	private Cart testCart;
	private UserDto testUserDto;

	@BeforeEach
	void setUp() {
		// Clean database
		cartRepository.deleteAll();

		// Create test cart
		testCart = Cart.builder()
				.userId(1)
				.build();
		testCart = cartRepository.save(testCart);

		// Mock user service response
		testUserDto = UserDto.builder()
				.userId(1)
				.build();
	}

	@Test
	@DisplayName("GET /api/carts - Should return all carts")
	void testFindAll_ShouldReturnAllCarts() throws Exception {
		// Arrange - Mock external user service call
		// Note: En pruebas reales esto requeriría WireMock o similar para mockear el servicio externo

		// Act & Assert
		mockMvc.perform(get("/api/carts")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection").isArray())
				.andExpect(jsonPath("$.collection[0].cartId").exists());
	}

	@Test
	@DisplayName("GET /api/carts/{cartId} - Should return cart by id")
	void testFindById_ShouldReturnCart() throws Exception {
		// Act & Assert
		mockMvc.perform(get("/api/carts/" + testCart.getCartId())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cartId").value(testCart.getCartId()))
				.andExpect(jsonPath("$.userId").value(1));
	}

	@Test
	@DisplayName("GET /api/carts/{cartId} - Should return 404 when cart not found")
	void testFindById_WhenCartNotFound_ShouldReturn404() throws Exception {
		// Act & Assert
		mockMvc.perform(get("/api/carts/999")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/carts - Should create new cart")
	void testSave_ShouldCreateCart() throws Exception {
		// Arrange
		CartDto newCartDto = CartDto.builder()
				.userId(2)
				.build();

		// Act & Assert
		mockMvc.perform(post("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newCartDto)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cartId").exists())
				.andExpect(jsonPath("$.userId").value(2));
	}

	@Test
	@DisplayName("POST /api/carts - Should return 400 when request body is null")
	void testSave_WhenRequestBodyIsNull_ShouldReturn400() throws Exception {
		// Act & Assert
		mockMvc.perform(post("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(""))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /api/carts - Should update cart")
	void testUpdate_WithCartDto_ShouldUpdateCart() throws Exception {
		// Arrange
		CartDto updatedCartDto = CartDto.builder()
				.cartId(testCart.getCartId())
				.userId(3)
				.build();

		// Act & Assert
		mockMvc.perform(put("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedCartDto)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cartId").value(testCart.getCartId()))
				.andExpect(jsonPath("$.userId").value(3));
	}

	@Test
	@DisplayName("PUT /api/carts/{cartId} - Should update cart by id")
	void testUpdate_WithCartIdAndCartDto_ShouldUpdateCart() throws Exception {
		// Arrange
		CartDto updatedCartDto = CartDto.builder()
				.userId(4)
				.build();

		// Act & Assert
		mockMvc.perform(put("/api/carts/" + testCart.getCartId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedCartDto)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cartId").value(testCart.getCartId()));
	}

	@Test
	@DisplayName("DELETE /api/carts/{cartId} - Should delete cart")
	void testDeleteById_ShouldDeleteCart() throws Exception {
		// Act & Assert
		mockMvc.perform(delete("/api/carts/" + testCart.getCartId())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));

		// Verify cart is deleted
		assertFalse(cartRepository.findById(testCart.getCartId()).isPresent());
	}

	@Test
	@DisplayName("DELETE /api/carts/{cartId} - Should return 404 when cart not found")
	void testDeleteById_WhenCartNotFound_ShouldReturn404() throws Exception {
		// Act & Assert
		// Spring Data JPA deleteById throws EmptyResultDataAccessException when entity doesn't exist
		// This should be handled by ApiExceptionHandler which returns NOT_FOUND
		mockMvc.perform(delete("/api/carts/999")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
}


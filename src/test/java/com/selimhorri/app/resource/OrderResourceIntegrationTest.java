package com.selimhorri.app.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.enums.OrderStatus;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.repository.CartRepository;
import com.selimhorri.app.repository.OrderRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderResource Integration Tests")
class OrderResourceIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	private Cart testCart;
	private Order testOrder;

	@BeforeEach
	void setUp() {
		// Clean database
		orderRepository.deleteAll();
		cartRepository.deleteAll();

		// Create test cart
		testCart = Cart.builder()
				.userId(1)
				.build();
		testCart = cartRepository.save(testCart);

		// Create test order
		testOrder = Order.builder()
				.orderDesc("Test order")
				.orderFee(5000.0)
				.isActive(true)
				.status(OrderStatus.CREATED)
				.cart(testCart)
				.build();
		testOrder = orderRepository.save(testOrder);
	}

	@Test
	@DisplayName("GET /api/orders - Should return all active orders")
	void testFindAll_ShouldReturnAllActiveOrders() throws Exception {
		// Act & Assert
		mockMvc.perform(get("/api/orders")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection").isArray())
				.andExpect(jsonPath("$.collection[0].orderId").exists())
				.andExpect(jsonPath("$.collection[0].orderDesc").value("Test order"));
	}

	@Test
	@DisplayName("GET /api/orders/{orderId} - Should return order by id")
	void testFindById_ShouldReturnOrder() throws Exception {
		// Act & Assert
		mockMvc.perform(get("/api/orders/" + testOrder.getOrderId())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(testOrder.getOrderId()))
				.andExpect(jsonPath("$.orderDesc").value("Test order"))
				.andExpect(jsonPath("$.orderFee").value(5000.0));
	}

	@Test
	@DisplayName("GET /api/orders/{orderId} - Should return 404 when order not found")
	void testFindById_WhenOrderNotFound_ShouldReturn404() throws Exception {
		// Act & Assert - Note: ApiExceptionHandler returns NOT_FOUND for OrderNotFoundException
		mockMvc.perform(get("/api/orders/999")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/orders - Should create new order")
	void testSave_ShouldCreateOrder() throws Exception {
		// Arrange
		OrderDto newOrderDto = OrderDto.builder()
				.orderDesc("New order")
				.orderFee(3000.0)
				.cartDto(CartDto.builder().cartId(testCart.getCartId()).build())
				.build();

		// Act & Assert
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newOrderDto)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").exists())
				.andExpect(jsonPath("$.orderDesc").value("New order"))
				.andExpect(jsonPath("$.orderFee").value(3000.0))
				.andExpect(jsonPath("$.orderStatus").value("CREATED"));
	}

	@Test
	@DisplayName("POST /api/orders - Should return 400 when cart is null")
	void testSave_WhenCartIsNull_ShouldReturn400() throws Exception {
		// Arrange
		OrderDto orderDtoWithoutCart = OrderDto.builder()
				.orderDesc("Order without cart")
				.orderFee(3000.0)
				.cartDto(null)
				.build();

		// Act & Assert - IllegalArgumentException should return 400
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderDtoWithoutCart)))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/orders - Should return 404 when cart does not exist")
	void testSave_WhenCartNotFound_ShouldReturnError() throws Exception {
		// Arrange
		OrderDto orderDto = OrderDto.builder()
				.orderDesc("Order")
				.orderFee(3000.0)
				.cartDto(CartDto.builder().cartId(999).build())
				.build();

		// Act & Assert - CartNotFoundException should return 404
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderDto)))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("PATCH /api/orders/{orderId}/status - Should update order status from CREATED to ORDERED")
	void testUpdateStatus_FromCreatedToOrdered_ShouldUpdate() throws Exception {
		// Act & Assert
		mockMvc.perform(patch("/api/orders/" + testOrder.getOrderId() + "/status")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderStatus").value("ORDERED"));
	}

	@Test
	@DisplayName("PATCH /api/orders/{orderId}/status - Should update order status from ORDERED to IN_PAYMENT")
	void testUpdateStatus_FromOrderedToInPayment_ShouldUpdate() throws Exception {
		// Arrange - Update order to ORDERED first
		testOrder.setStatus(OrderStatus.ORDERED);
		testOrder = orderRepository.save(testOrder);

		// Act & Assert
		mockMvc.perform(patch("/api/orders/" + testOrder.getOrderId() + "/status")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderStatus").value("IN_PAYMENT"));
	}

	@Test
	@DisplayName("PATCH /api/orders/{orderId}/status - Should return 400 when order is already IN_PAYMENT")
	void testUpdateStatus_WhenInPayment_ShouldReturnError() throws Exception {
		// Arrange - Update order to IN_PAYMENT
		testOrder.setStatus(OrderStatus.IN_PAYMENT);
		testOrder = orderRepository.save(testOrder);

		// Act & Assert - IllegalStateException should return 400
		mockMvc.perform(patch("/api/orders/" + testOrder.getOrderId() + "/status")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /api/orders/{orderId} - Should update order")
	void testUpdate_ShouldUpdateOrder() throws Exception {
		// Arrange
		OrderDto updatedOrderDto = OrderDto.builder()
				.orderDesc("Updated order description")
				.orderFee(6000.0)
				.build();

		// Act & Assert
		mockMvc.perform(put("/api/orders/" + testOrder.getOrderId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedOrderDto)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(testOrder.getOrderId()))
				.andExpect(jsonPath("$.orderDesc").value("Updated order description"))
				.andExpect(jsonPath("$.orderFee").value(6000.0));
	}

	@Test
	@DisplayName("DELETE /api/orders/{orderId} - Should delete order (soft delete)")
	void testDeleteById_ShouldDeactivateOrder() throws Exception {
		// Act & Assert
		mockMvc.perform(delete("/api/orders/" + testOrder.getOrderId())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));

		// Verify soft delete - order should not be found by active query
		List<Order> activeOrders = orderRepository.findAllByIsActiveTrue();
		assertTrue(activeOrders.stream()
				.noneMatch(o -> o.getOrderId().equals(testOrder.getOrderId())));
	}

	@Test
	@DisplayName("DELETE /api/orders/{orderId} - Should return 400 when trying to delete order with IN_PAYMENT status")
	void testDeleteById_WhenStatusInPayment_ShouldReturnError() throws Exception {
		// Arrange - Update order to IN_PAYMENT
		testOrder.setStatus(OrderStatus.IN_PAYMENT);
		testOrder = orderRepository.save(testOrder);

		// Act & Assert - IllegalStateException should return 400
		mockMvc.perform(delete("/api/orders/" + testOrder.getOrderId())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}
}


package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.enums.OrderStatus;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;

@DisplayName("OrderMappingHelper Unit Tests")
class OrderMappingHelperTest {

	private Cart testCart;
	private Order testOrder;
	private OrderDto testOrderDto;

	@BeforeEach
	void setUp() {
		testCart = Cart.builder()
				.cartId(1)
				.userId(1)
				.build();

		testOrder = Order.builder()
				.orderId(1)
				.orderDate(LocalDateTime.now())
				.orderDesc("Test order")
				.orderFee(5000.0)
				.isActive(true)
				.status(OrderStatus.CREATED)
				.cart(testCart)
				.build();

		testOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDate(LocalDateTime.now())
				.orderDesc("Test order")
				.orderFee(5000.0)
				.orderStatus(OrderStatus.CREATED)
				.cartDto(CartDto.builder().cartId(1).build())
				.build();
	}

	@Test
	@DisplayName("Should map Order to OrderDto correctly")
	void testMap_OrderToDto_ShouldMapCorrectly() {
		// Act
		OrderDto result = OrderMappingHelper.map(testOrder);

		// Assert
		assertNotNull(result);
		assertEquals(testOrder.getOrderId(), result.getOrderId());
		assertEquals(testOrder.getOrderDate(), result.getOrderDate());
		assertEquals(testOrder.getOrderDesc(), result.getOrderDesc());
		assertEquals(testOrder.getOrderFee(), result.getOrderFee());
		assertEquals(testOrder.getStatus(), result.getOrderStatus());
		assertNotNull(result.getCartDto());
		assertEquals(testOrder.getCart().getCartId(), result.getCartDto().getCartId());
	}

	@Test
	@DisplayName("Should map OrderDto to Order correctly")
	void testMap_DtoToOrder_ShouldMapCorrectly() {
		// Act
		Order result = OrderMappingHelper.map(testOrderDto);

		// Assert
		assertNotNull(result);
		assertEquals(testOrderDto.getOrderId(), result.getOrderId());
		assertEquals(testOrderDto.getOrderDesc(), result.getOrderDesc());
		assertEquals(testOrderDto.getOrderFee(), result.getOrderFee());
		assertNotNull(result.getOrderDate());
		assertNotNull(result.getCart());
		assertEquals(testOrderDto.getCartDto().getCartId(), result.getCart().getCartId());
	}

	@Test
	@DisplayName("Should use CREATED as default status when orderStatus is null")
	void testMap_DtoToOrder_WhenStatusIsNull_ShouldUseDefault() {
		// Arrange
		OrderDto orderDtoWithoutStatus = OrderDto.builder()
				.orderDesc("Order without status")
				.orderFee(3000.0)
				.orderStatus(null)
				.cartDto(CartDto.builder().cartId(1).build())
				.build();

		// Act
		Order result = OrderMappingHelper.map(orderDtoWithoutStatus);

		// Assert
		assertNotNull(result);
		assertEquals(OrderStatus.CREATED, result.getStatus());
	}

	@Test
	@DisplayName("Should map OrderDto to Order for creation with isActive=true")
	void testMapForCreationOrder_ShouldSetActiveToTrue() {
		// Arrange
		OrderDto newOrderDto = OrderDto.builder()
				.orderDesc("New order")
				.orderFee(3000.0)
				.cartDto(CartDto.builder().cartId(1).build())
				.build();

		// Act
		Order result = OrderMappingHelper.mapForCreationOrder(newOrderDto);

		// Assert
		assertNotNull(result);
		assertTrue(result.isActive());
		assertNotNull(result.getOrderDate());
		assertEquals(OrderStatus.CREATED, result.getStatus());
		assertNotNull(result.getCart());
		assertEquals(newOrderDto.getCartDto().getCartId(), result.getCart().getCartId());
	}

	@Test
	@DisplayName("Should map OrderDto to Order for update preserving cart")
	void testMapForUpdate_ShouldPreserveCart() {
		// Arrange
		OrderDto updatedOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDate(LocalDateTime.now())
				.orderDesc("Updated order")
				.orderFee(6000.0)
				.build();

		Cart existingCart = Cart.builder()
				.cartId(2)
				.userId(2)
				.build();

		// Act
		Order result = OrderMappingHelper.mapForUpdate(updatedOrderDto, existingCart);

		// Assert
		assertNotNull(result);
		assertEquals(updatedOrderDto.getOrderId(), result.getOrderId());
		assertEquals(updatedOrderDto.getOrderDesc(), result.getOrderDesc());
		assertEquals(updatedOrderDto.getOrderFee(), result.getOrderFee());
		assertEquals(updatedOrderDto.getOrderDate(), result.getOrderDate());
		assertNotNull(result.getCart());
		assertEquals(existingCart.getCartId(), result.getCart().getCartId());
		assertEquals(existingCart.getUserId(), result.getCart().getUserId());
	}

	@Test
	@DisplayName("Should handle null values gracefully in map")
	void testMap_WithNullValues_ShouldHandleGracefully() {
		// Arrange
		Order orderWithNulls = Order.builder()
				.orderId(1)
				.orderDate(LocalDateTime.now())
				.orderDesc(null)
				.orderFee(null)
				.status(OrderStatus.CREATED)
				.cart(testCart)
				.build();

		// Act
		OrderDto result = OrderMappingHelper.map(orderWithNulls);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.getOrderId());
		assertNull(result.getOrderDesc());
		assertNull(result.getOrderFee());
	}
}


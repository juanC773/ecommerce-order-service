package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.enums.OrderStatus;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.repository.CartRepository;
import com.selimhorri.app.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private CartRepository cartRepository;

	@InjectMocks
	private OrderServiceImpl orderService;

	private Order testOrder;
	private OrderDto testOrderDto;
	private Cart testCart;

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
	@DisplayName("Should find all active orders successfully")
	void testFindAll_ShouldReturnActiveOrders() {
		// Arrange
		List<Order> orders = Arrays.asList(testOrder);
		when(orderRepository.findAllByIsActiveTrue()).thenReturn(orders);

		// Act
		List<OrderDto> result = orderService.findAll();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		verify(orderRepository, times(1)).findAllByIsActiveTrue();
	}

	@Test
	@DisplayName("Should find order by id when order exists and is active")
	void testFindById_WhenOrderExists_ShouldReturnOrder() {
		// Arrange
		when(orderRepository.findByOrderIdAndIsActiveTrue(1)).thenReturn(Optional.of(testOrder));

		// Act
		OrderDto result = orderService.findById(1);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.getOrderId());
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
	}

	@Test
	@DisplayName("Should throw OrderNotFoundException when order not found")
	void testFindById_WhenOrderNotFound_ShouldThrowException() {
		// Arrange
		when(orderRepository.findByOrderIdAndIsActiveTrue(999)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(OrderNotFoundException.class, () -> orderService.findById(999));
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(999);
	}

	@Test
	@DisplayName("Should save order successfully when cart exists")
	void testSave_WhenCartExists_ShouldSaveOrder() {
		// Arrange
		OrderDto newOrderDto = OrderDto.builder()
				.orderDesc("New order")
				.orderFee(3000.0)
				.cartDto(CartDto.builder().cartId(1).build())
				.build();

		when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
		when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

		// Act
		OrderDto result = orderService.save(newOrderDto);

		// Assert
		assertNotNull(result);
		assertNull(newOrderDto.getOrderId()); // Should be null after save method
		assertNull(newOrderDto.getOrderStatus()); // Should be null after save method
		verify(cartRepository, times(1)).findById(1);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when cart is null")
	void testSave_WhenCartIsNull_ShouldThrowException() {
		// Arrange
		OrderDto orderDtoWithoutCart = OrderDto.builder()
				.orderDesc("Order without cart")
				.orderFee(3000.0)
				.cartDto(null)
				.build();

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> orderService.save(orderDtoWithoutCart));
		verify(cartRepository, never()).findById(anyInt());
		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	@DisplayName("Should throw CartNotFoundException when cart does not exist")
	void testSave_WhenCartNotFound_ShouldThrowException() {
		// Arrange
		OrderDto orderDto = OrderDto.builder()
				.orderDesc("Order")
				.orderFee(3000.0)
				.cartDto(CartDto.builder().cartId(999).build())
				.build();

		when(cartRepository.findById(999)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(CartNotFoundException.class, () -> orderService.save(orderDto));
		verify(cartRepository, times(1)).findById(999);
		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	@DisplayName("Should update order status from CREATED to ORDERED")
	void testUpdateStatus_FromCreatedToOrdered_ShouldUpdate() {
		// Arrange
		Order orderWithCreatedStatus = Order.builder()
				.orderId(1)
				.status(OrderStatus.CREATED)
				.isActive(true)
				.cart(testCart)
				.build();

		when(orderRepository.findByOrderIdAndIsActiveTrue(1))
				.thenReturn(Optional.of(orderWithCreatedStatus));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order saved = invocation.getArgument(0);
			assertEquals(OrderStatus.ORDERED, saved.getStatus());
			return saved;
		});

		// Act
		OrderDto result = orderService.updateStatus(1);

		// Assert
		assertNotNull(result);
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	@DisplayName("Should update order status from ORDERED to IN_PAYMENT")
	void testUpdateStatus_FromOrderedToInPayment_ShouldUpdate() {
		// Arrange
		Order orderWithOrderedStatus = Order.builder()
				.orderId(1)
				.status(OrderStatus.ORDERED)
				.isActive(true)
				.cart(testCart)
				.build();

		when(orderRepository.findByOrderIdAndIsActiveTrue(1))
				.thenReturn(Optional.of(orderWithOrderedStatus));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order saved = invocation.getArgument(0);
			assertEquals(OrderStatus.IN_PAYMENT, saved.getStatus());
			return saved;
		});

		// Act
		OrderDto result = orderService.updateStatus(1);

		// Assert
		assertNotNull(result);
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	@DisplayName("Should throw IllegalStateException when trying to update IN_PAYMENT status")
	void testUpdateStatus_WhenStatusIsInPayment_ShouldThrowException() {
		// Arrange
		Order orderWithInPaymentStatus = Order.builder()
				.orderId(1)
				.status(OrderStatus.IN_PAYMENT)
				.isActive(true)
				.cart(testCart)
				.build();

		when(orderRepository.findByOrderIdAndIsActiveTrue(1))
				.thenReturn(Optional.of(orderWithInPaymentStatus));

		// Act & Assert
		assertThrows(IllegalStateException.class, () -> orderService.updateStatus(1));
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	@DisplayName("Should update order successfully")
	void testUpdate_ShouldUpdateOrder() {
		// Arrange
		OrderDto updatedOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDesc("Updated order")
				.orderFee(6000.0)
				.build();

		when(orderRepository.findByOrderIdAndIsActiveTrue(1))
				.thenReturn(Optional.of(testOrder));
		when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

		// Act
		OrderDto result = orderService.update(1, updatedOrderDto);

		// Assert
		assertNotNull(result);
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	@DisplayName("Should delete order (soft delete) when status is not IN_PAYMENT")
	void testDeleteById_WhenStatusNotInPayment_ShouldDeactivate() {
		// Arrange
		when(orderRepository.findByOrderIdAndIsActiveTrue(1))
				.thenReturn(Optional.of(testOrder));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order saved = invocation.getArgument(0);
			assertFalse(saved.isActive());
			return saved;
		});

		// Act
		orderService.deleteById(1);

		// Assert
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	@DisplayName("Should throw IllegalStateException when trying to delete order with IN_PAYMENT status")
	void testDeleteById_WhenStatusIsInPayment_ShouldThrowException() {
		// Arrange
		Order orderInPayment = Order.builder()
				.orderId(1)
				.status(OrderStatus.IN_PAYMENT)
				.isActive(true)
				.cart(testCart)
				.build();

		when(orderRepository.findByOrderIdAndIsActiveTrue(1))
				.thenReturn(Optional.of(orderInPayment));

		// Act & Assert
		assertThrows(IllegalStateException.class, () -> orderService.deleteById(1));
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	@DisplayName("Should throw OrderNotFoundException when order not found for delete")
	void testDeleteById_WhenOrderNotFound_ShouldThrowException() {
		// Arrange
		when(orderRepository.findByOrderIdAndIsActiveTrue(999)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(OrderNotFoundException.class, () -> orderService.deleteById(999));
		verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(999);
		verify(orderRepository, never()).save(any(Order.class));
	}
}


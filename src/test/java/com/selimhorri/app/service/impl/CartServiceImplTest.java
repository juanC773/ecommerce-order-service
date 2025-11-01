package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Unit Tests")
class CartServiceImplTest {

	@Mock
	private CartRepository cartRepository;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private CartServiceImpl cartService;

	private Cart testCart;
	private CartDto testCartDto;
	private UserDto testUserDto;

	@BeforeEach
	void setUp() {
		testCart = Cart.builder()
				.cartId(1)
				.userId(1)
				.build();

		testCartDto = CartDto.builder()
				.cartId(1)
				.userId(1)
				.build();

		testUserDto = UserDto.builder()
				.userId(1)
				.build();
	}

	@Test
	@DisplayName("Should find all carts successfully")
	void testFindAll_ShouldReturnAllCarts() {
		// Arrange
		List<Cart> carts = Arrays.asList(testCart);
		when(cartRepository.findAll()).thenReturn(carts);
		when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
				.thenReturn(testUserDto);

		// Act
		List<CartDto> result = cartService.findAll();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertNotNull(result.get(0).getUserDto());
		verify(cartRepository, times(1)).findAll();
		verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
	}

	@Test
	@DisplayName("Should find cart by id when cart exists")
	void testFindById_WhenCartExists_ShouldReturnCart() {
		// Arrange
		when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
		when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
				.thenReturn(testUserDto);

		// Act
		CartDto result = cartService.findById(1);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.getCartId());
		assertEquals(1, result.getUserId());
		assertNotNull(result.getUserDto());
		verify(cartRepository, times(1)).findById(1);
		verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
	}

	@Test
	@DisplayName("Should throw CartNotFoundException when cart not found")
	void testFindById_WhenCartNotFound_ShouldThrowException() {
		// Arrange
		when(cartRepository.findById(999)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(CartNotFoundException.class, () -> cartService.findById(999));
		verify(cartRepository, times(1)).findById(999);
		verify(restTemplate, never()).getForObject(anyString(), eq(UserDto.class));
	}

	@Test
	@DisplayName("Should save cart successfully")
	void testSave_ShouldSaveCart() {
		// Arrange
		CartDto newCartDto = CartDto.builder()
				.userId(2)
				.build();

		when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

		// Act
		CartDto result = cartService.save(newCartDto);

		// Assert
		assertNotNull(result);
		verify(cartRepository, times(1)).save(any(Cart.class));
	}

	@Test
	@DisplayName("Should update cart successfully with CartDto")
	void testUpdate_WithCartDto_ShouldUpdateCart() {
		// Arrange
		CartDto updatedCartDto = CartDto.builder()
				.cartId(1)
				.userId(2)
				.build();

		when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

		// Act
		CartDto result = cartService.update(updatedCartDto);

		// Assert
		assertNotNull(result);
		verify(cartRepository, times(1)).save(any(Cart.class));
	}

	@Test
	@DisplayName("Should update cart successfully with cartId and CartDto")
	void testUpdate_WithCartIdAndCartDto_ShouldUpdateCart() {
		// Arrange
		CartDto updatedCartDto = CartDto.builder()
				.userId(2)
				.build();

		when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
		when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
				.thenReturn(testUserDto);
		when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

		// Act
		CartDto result = cartService.update(1, updatedCartDto);

		// Assert
		assertNotNull(result);
		verify(cartRepository, times(1)).findById(1);
		verify(cartRepository, times(1)).save(any(Cart.class));
	}

	@Test
	@DisplayName("Should delete cart by id successfully")
	void testDeleteById_ShouldDeleteCart() {
		// Arrange
		doNothing().when(cartRepository).deleteById(1);

		// Act
		cartService.deleteById(1);

		// Assert
		verify(cartRepository, times(1)).deleteById(1);
	}

	@Test
	@DisplayName("Should handle null user from external service gracefully")
	void testFindAll_WhenUserServiceReturnsNull_ShouldHandleGracefully() {
		// Arrange
		List<Cart> carts = Arrays.asList(testCart);
		when(cartRepository.findAll()).thenReturn(carts);
		when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
				.thenReturn(null);

		// Act
		List<CartDto> result = cartService.findAll();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		verify(cartRepository, times(1)).findAll();
		verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
	}
}


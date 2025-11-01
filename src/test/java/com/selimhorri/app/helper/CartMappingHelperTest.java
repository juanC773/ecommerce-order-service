package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;

@DisplayName("CartMappingHelper Unit Tests")
class CartMappingHelperTest {

	private Cart testCart;
	private CartDto testCartDto;

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
	}

	@Test
	@DisplayName("Should map Cart to CartDto correctly")
	void testMap_CartToDto_ShouldMapCorrectly() {
		// Act
		CartDto result = CartMappingHelper.map(testCart);

		// Assert
		assertNotNull(result);
		assertEquals(testCart.getCartId(), result.getCartId());
		assertEquals(testCart.getUserId(), result.getUserId());
		assertNotNull(result.getUserDto());
		assertEquals(testCart.getUserId(), result.getUserDto().getUserId());
	}

	@Test
	@DisplayName("Should map CartDto to Cart correctly")
	void testMap_DtoToCart_ShouldMapCorrectly() {
		// Act
		Cart result = CartMappingHelper.map(testCartDto);

		// Assert
		assertNotNull(result);
		assertEquals(testCartDto.getCartId(), result.getCartId());
		assertEquals(testCartDto.getUserId(), result.getUserId());
	}

	@Test
	@DisplayName("Should handle null values gracefully in Cart to DTO mapping")
	void testMap_CartToDto_WithNullValues_ShouldHandleGracefully() {
		// Arrange
		Cart cartWithNulls = Cart.builder()
				.cartId(1)
				.userId(null)
				.build();

		// Act
		CartDto result = CartMappingHelper.map(cartWithNulls);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.getCartId());
		assertNull(result.getUserId());
	}

	@Test
	@DisplayName("Should handle null values gracefully in DTO to Cart mapping")
	void testMap_DtoToCart_WithNullValues_ShouldHandleGracefully() {
		// Arrange
		CartDto cartDtoWithNulls = CartDto.builder()
				.cartId(null)
				.userId(1)
				.build();

		// Act
		Cart result = CartMappingHelper.map(cartDtoWithNulls);

		// Assert
		assertNotNull(result);
		assertNull(result.getCartId());
		assertEquals(1, result.getUserId());
	}

	@Test
	@DisplayName("Should preserve UserDto structure in Cart to DTO mapping")
	void testMap_CartToDto_ShouldPreserveUserDtoStructure() {
		// Act
		CartDto result = CartMappingHelper.map(testCart);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getUserDto());
		assertEquals(testCart.getUserId(), result.getUserDto().getUserId());
		// UserDto should be created with userId but other fields may be null
		assertTrue(result.getUserDto() instanceof UserDto);
	}
}


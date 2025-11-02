package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.helper.CartMappingHelper;
import com.selimhorri.app.repository.CartRepository;
import com.selimhorri.app.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
	
	private final CartRepository cartRepository;
	private final RestTemplate restTemplate;
	
	@Override
	public List<CartDto> findAll() {
		log.info("*** CartDto List, service; fetch all carts *");
		return this.cartRepository.findAll()
				.stream()
					.map(CartMappingHelper::map)
					.map(c -> {
						c.setUserDto(this.restTemplate.getForObject(AppConstant.DiscoveredDomainsApi
								.USER_SERVICE_API_URL + "/" + c.getUserDto().getUserId(), UserDto.class));
						return c;
					})
					.distinct()
					.collect(Collectors.toUnmodifiableList());
	}
	
	@Override
	public CartDto findById(final Integer cartId) {
		log.info("*** CartDto, service; fetch cart by id *");
		return this.cartRepository.findById(cartId)
				.map(CartMappingHelper::map)
				.map(c -> {
					// Solo llamar a User Service si userId existe
					if (c.getUserId() != null && c.getUserDto() != null && c.getUserDto().getUserId() != null) {
						try {
							log.debug("Calling USER-SERVICE to get user with ID: {}", c.getUserDto().getUserId());
							UserDto userDto = this.restTemplate.getForObject(AppConstant.DiscoveredDomainsApi
									.USER_SERVICE_API_URL + "/" + c.getUserDto().getUserId(), UserDto.class);
							if (userDto != null) {
								c.setUserDto(userDto);
								log.debug("Successfully retrieved user data from USER-SERVICE");
							} else {
								log.warn("USER-SERVICE returned null for user ID: {}", c.getUserDto().getUserId());
							}
						} catch (Exception e) {
							log.error("Failed to call USER-SERVICE for user ID {}: {}", c.getUserDto().getUserId(), e.getMessage());
							// No lanzar excepción, solo loguear el error y continuar sin userDto completo
							// Esto permite que el carrito se retorne aunque la llamada a User Service falle
						}
					} else {
						log.warn("Cart {} has null userId or userDto, skipping USER-SERVICE call", cartId);
					}
					return c;
				})
				.orElseThrow(() -> new CartNotFoundException(String
						.format("Cart with id: %d not found", cartId)));
	}
	
	@Override
	public CartDto save(final CartDto cartDto) {
		log.info("*** CartDto, service; save cart *");
		return CartMappingHelper.map(this.cartRepository
				.save(CartMappingHelper.map(cartDto)));
	}
	
	@Override
	public CartDto update(final CartDto cartDto) {
		log.info("*** CartDto, service; update cart *");
		return CartMappingHelper.map(this.cartRepository
				.save(CartMappingHelper.map(cartDto)));
	}
	
	@Override
	public CartDto update(final Integer cartId, final CartDto cartDto) {
		log.info("*** CartDto, service; update cart with cartId *");
		return CartMappingHelper.map(this.cartRepository
				.save(CartMappingHelper.map(this.findById(cartId))));
	}
	
	@Override
	public void deleteById(final Integer cartId) {
		log.info("*** Void, service; delete cart by id *");
		this.cartRepository.deleteById(cartId);
	}
	
	
	
}











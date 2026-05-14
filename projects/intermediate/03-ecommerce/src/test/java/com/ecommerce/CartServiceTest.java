package com.ecommerce;

import com.ecommerce.dto.*;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Captor
    private ArgumentCaptor<Cart> cartCaptor;
    @Captor
    private ArgumentCaptor<CartItem> cartItemCaptor;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").name("Test User").build();
        testProduct = Product.builder().id(1L).name("Test Product").price(BigDecimal.valueOf(19.99)).stockQuantity(10).build();
        testCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();
    }

    @Nested
    class GetCart {

        @Test
        void testGetCart_Existing() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

            CartResponse response = cartService.getCart(1L);

            assertEquals(1L, response.getId());
            assertEquals(1L, response.getUserId());
        }

        @Test
        void testGetCart_CreatesNewWhenNotFound() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            CartResponse response = cartService.getCart(1L);

            assertNotNull(response);
        }

        @Test
        void testGetCart_UserNotFound() {
            when(cartRepository.findByUserId(999L)).thenReturn(Optional.empty());
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> cartService.getCart(999L));
        }
    }

    @Nested
    class AddToCart {

        @Test
        void testAddItem_NewItem() {
            CartItemRequest request = CartItemRequest.builder().productId(1L).quantity(2).build();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));

            CartResponse response = cartService.addItem(1L, request);

            assertNotNull(response);
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        void testAddItem_ExistingItemIncreasesQuantity() {
            CartItem existing = CartItem.builder().id(1L).cart(testCart).product(testProduct).quantity(1).build();
            testCart.getItems().add(existing);
            CartItemRequest request = CartItemRequest.builder().productId(1L).quantity(3).build();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));

            cartService.addItem(1L, request);

            assertEquals(4, existing.getQuantity());
            verify(cartItemRepository).save(existing);
        }

        @Test
        void testAddItem_ProductNotFound() {
            CartItemRequest request = CartItemRequest.builder().productId(999L).quantity(1).build();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> cartService.addItem(1L, request));
        }
    }

    @Nested
    class UpdateQuantity {

        @Test
        void testUpdateItemQuantity() {
            CartItem item = CartItem.builder().id(1L).cart(testCart).product(testProduct).quantity(2).build();
            testCart.getItems().add(item);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));

            cartService.updateItemQuantity(1L, 1L, 5);

            assertEquals(5, item.getQuantity());
            verify(cartItemRepository).save(item);
        }

        @Test
        void testUpdateItemQuantity_RemovesWhenZero() {
            CartItem item = CartItem.builder().id(1L).cart(testCart).product(testProduct).quantity(2).build();
            testCart.getItems().add(item);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));

            cartService.updateItemQuantity(1L, 1L, 0);

            assertTrue(testCart.getItems().isEmpty());
            verify(cartItemRepository).delete(item);
        }

        @Test
        void testUpdateItemQuantity_WrongUser() {
            Cart otherCart = Cart.builder().id(2L).user(User.builder().id(2L).build()).build();
            CartItem item = CartItem.builder().id(1L).cart(otherCart).build();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

            assertThrows(RuntimeException.class, () -> cartService.updateItemQuantity(1L, 1L, 5));
        }

        @Test
        void testUpdateItemQuantity_ItemNotFound() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> cartService.updateItemQuantity(1L, 999L, 5));
        }
    }

    @Nested
    class RemoveItem {

        @Test
        void testRemoveItem() {
            CartItem item = CartItem.builder().id(1L).cart(testCart).build();
            testCart.getItems().add(item);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));

            cartService.removeItem(1L, 1L);

            verify(cartItemRepository).delete(item);
        }

        @Test
        void testRemoveItem_WrongUser() {
            Cart otherCart = Cart.builder().id(2L).user(User.builder().id(2L).build()).build();
            CartItem item = CartItem.builder().id(1L).cart(otherCart).build();

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

            assertThrows(RuntimeException.class, () -> cartService.removeItem(1L, 1L));
        }
    }

    @Nested
    class ClearCart {

        @Test
        void testClearCart() {
            CartItem item = CartItem.builder().id(1L).cart(testCart).build();
            testCart.getItems().add(item);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

            cartService.clearCart(1L);

            verify(cartItemRepository).deleteAll(testCart.getItems());
            assertTrue(testCart.getItems().isEmpty());
            verify(cartRepository).save(testCart);
        }

        @Test
        void testClearCart_EmptyCart() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

            cartService.clearCart(1L);

            verify(cartItemRepository).deleteAll(testCart.getItems());
            verify(cartRepository).save(testCart);
        }
    }
}

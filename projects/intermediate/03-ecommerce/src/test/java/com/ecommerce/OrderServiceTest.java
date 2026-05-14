package com.ecommerce;

import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").name("Test").build();
        testProduct = Product.builder().id(1L).name("Product").price(BigDecimal.valueOf(29.99)).stockQuantity(10).build();
        testCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();
    }

    @Nested
    class CreateOrder {

        @Test
        void testCreateOrder_Success() {
            CartItem cartItem = CartItem.builder().id(1L).cart(testCart).product(testProduct).quantity(2).build();
            testCart.getItems().add(cartItem);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order o = invocation.getArgument(0);
                o.setId(1L);
                o.onCreate();
                return o;
            });
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            OrderResponse response = orderService.createOrder(1L, "123 Main St");

            assertNotNull(response);
            assertEquals("PENDING", response.getStatus());
            assertEquals("123 Main St", response.getShippingAddress());
            assertEquals(BigDecimal.valueOf(59.98).doubleValue(), response.getTotalAmount().doubleValue(), 0.01);
        }

        @Test
        void testCreateOrder_EmptyCart() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

            assertThrows(RuntimeException.class, () -> orderService.createOrder(1L, "Address"));
        }

        @Test
        void testCreateOrder_NoCart() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> orderService.createOrder(1L, "Address"));
        }

        @Test
        void testCreateOrder_InsufficientStock() {
            Product lowStockProduct = Product.builder().id(1L).name("Low").price(BigDecimal.TEN).stockQuantity(1).build();
            CartItem cartItem = CartItem.builder().id(1L).cart(testCart).product(lowStockProduct).quantity(5).build();
            testCart.getItems().add(cartItem);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

            assertThrows(RuntimeException.class, () -> orderService.createOrder(1L, "Address"));
        }

        @Test
        void testCreateOrder_UpdatesStock() {
            CartItem cartItem = CartItem.builder().id(1L).cart(testCart).product(testProduct).quantity(3).build();
            testCart.getItems().add(cartItem);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order o = invocation.getArgument(0);
                o.setId(1L);
                o.onCreate();
                return o;
            });
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            orderService.createOrder(1L, "Address");

            assertEquals(7, testProduct.getStockQuantity());
        }

        @Test
        void testCreateOrder_ClearsCartAfterOrder() {
            CartItem cartItem = CartItem.builder().id(1L).cart(testCart).product(testProduct).quantity(1).build();
            testCart.getItems().add(cartItem);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order o = invocation.getArgument(0);
                o.setId(1L);
                o.onCreate();
                return o;
            });
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            orderService.createOrder(1L, "Address");

            verify(cartItemRepository).deleteAll(testCart.getItems());
            verify(cartRepository).save(testCart);
        }
    }

    @Nested
    class GetOrder {

        @Test
        void testGetOrderById() {
            Order order = Order.builder().id(1L).user(testUser).status("PENDING").totalAmount(BigDecimal.valueOf(100)).shippingAddress("Addr").build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            OrderResponse response = orderService.getOrderById(1L);

            assertEquals(1L, response.getId());
            assertEquals("PENDING", response.getStatus());
        }

        @Test
        void testGetOrderById_NotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
        }
    }

    @Nested
    class UserOrders {

        @Test
        void testGetUserOrders() {
            Page<Order> page = new PageImpl<>(List.of(
                    Order.builder().id(1L).user(testUser).status("PENDING").build()));
            when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

            Page<OrderResponse> result = orderService.getUserOrders(1L, 0, 10);

            assertEquals(1, result.getContent().size());
        }

        @Test
        void testGetUserOrders_Empty() {
            when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(Page.empty());

            Page<OrderResponse> result = orderService.getUserOrders(1L, 0, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetAllOrders {

        @Test
        void testGetAllOrders() {
            when(orderRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(Order.builder().id(1L).build())));

            Page<OrderResponse> result = orderService.getAllOrders(0, 10);

            assertEquals(1, result.getContent().size());
        }
    }

    @Nested
    class UpdateStatus {

        @Test
        void testUpdateOrderStatus() {
            Order order = Order.builder().id(1L).user(testUser).status("PENDING").build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrderResponse response = orderService.updateOrderStatus(1L, "SHIPPED");

            assertEquals("SHIPPED", response.getStatus());
        }

        @Test
        void testUpdateOrderStatus_NotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(999L, "SHIPPED"));
        }

        @Test
        void testFullStatusWorkflow() {
            Order order = Order.builder().id(1L).user(testUser).status("PENDING").build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrderResponse pending = orderService.updateOrderStatus(1L, "PENDING");
            assertEquals("PENDING", pending.getStatus());

            OrderResponse shipped = orderService.updateOrderStatus(1L, "SHIPPED");
            assertEquals("SHIPPED", shipped.getStatus());

            OrderResponse delivered = orderService.updateOrderStatus(1L, "DELIVERED");
            assertEquals("DELIVERED", delivered.getStatus());
        }
    }
}

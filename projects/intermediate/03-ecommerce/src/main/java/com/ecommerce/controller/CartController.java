package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestAttribute Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestAttribute Long userId,
                                                 @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(@RequestAttribute Long userId,
                                                            @PathVariable Long itemId,
                                                            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@RequestAttribute Long userId,
                                                    @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestAttribute Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}

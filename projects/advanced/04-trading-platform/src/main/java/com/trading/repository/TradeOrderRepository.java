package com.trading.repository;

import com.trading.model.OrderStatus;
import com.trading.model.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
    List<TradeOrder> findByUserId(Long userId);
    List<TradeOrder> findByUserIdAndStatus(Long userId, OrderStatus status);
    List<TradeOrder> findBySymbolAndStatus(String symbol, OrderStatus status);
    List<TradeOrder> findByStatus(OrderStatus status);
}

package com.trading.service;

import com.trading.dto.PortfolioResponse;
import com.trading.model.OrderSide;
import com.trading.model.Portfolio;
import com.trading.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PortfolioService {
    private final PortfolioRepository repository;
    private final MarketDataService marketDataService;

    public PortfolioService(PortfolioRepository repository, MarketDataService marketDataService) {
        this.repository = repository;
        this.marketDataService = marketDataService;
    }

    public List<PortfolioResponse> getUserPortfolio(Long userId) {
        return repository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void updatePortfolio(Long userId, String symbol, OrderSide side,
                                 BigDecimal quantity, BigDecimal price) {
        var holding = repository.findByUserIdAndSymbol(userId, symbol);
        if (side == OrderSide.BUY) {
            var portfolio = holding.orElseGet(() -> {
                var p = new Portfolio();
                p.setUserId(userId);
                p.setSymbol(symbol);
                p.setQuantity(BigDecimal.ZERO);
                p.setAvgCost(BigDecimal.ZERO);
                return p;
            });
            var totalCost = portfolio.getAvgCost().multiply(portfolio.getQuantity())
                    .add(price.multiply(quantity));
            var newQty = portfolio.getQuantity().add(quantity);
            portfolio.setQuantity(newQty);
            portfolio.setAvgCost(totalCost.divide(newQty, 6, RoundingMode.HALF_UP));
            repository.save(portfolio);
        } else {
            holding.ifPresent(portfolio -> {
                portfolio.setQuantity(portfolio.getQuantity().subtract(quantity));
                if (portfolio.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    repository.delete(portfolio);
                } else {
                    repository.save(portfolio);
                }
            });
        }
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        var currentPrice = marketDataService.getLatestPrice(portfolio.getSymbol());
        var currentVal = currentPrice.multiply(portfolio.getQuantity());
        var costBasis = portfolio.getAvgCost().multiply(portfolio.getQuantity());
        return new PortfolioResponse(
            portfolio.getId(), portfolio.getUserId(), portfolio.getSymbol(),
            portfolio.getQuantity(), portfolio.getAvgCost(),
            currentPrice, currentVal, currentVal.subtract(costBasis));
    }
}

package nexters.payout.domain.portfolio.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioStock {

    private UUID portfolioId;
    private UUID stockId;
    private Integer shares;


    public PortfolioStock(final UUID id, final UUID portfolioId, final UUID stockId, final Integer shares) {
        this.portfolioId = portfolioId;
        this.stockId = stockId;
        this.shares = shares;
    }

    private PortfolioStock(final UUID portfolioId, final UUID stockId, final Integer shares) {
        this.portfolioId = portfolioId;
        this.stockId = stockId;
        this.shares = shares;
    }

    public PortfolioStock create(final UUID portfolioId, final UUID stockId, final Integer shares) {
        return new PortfolioStock(portfolioId, stockId, shares);
    }
}

package nexters.payout.apiserver.stock.application.dto.response;

import nexters.payout.domain.dividend.domain.Dividend;
import nexters.payout.domain.stock.domain.Stock;

import java.util.UUID;

public record StockResponse(
        UUID stockId,
        String ticker,
        String companyName,
        String sectorName,
        String exchange,
        String industry,
        Double price,
        Integer volume,
        Double dividendPerShare
) {
    public static StockResponse of(Stock stock, Dividend dividend) {
        return new StockResponse(
                stock.getId(),
                stock.getTicker(),
                stock.getName(),
                stock.getSector().getName(),
                stock.getExchange(),
                stock.getIndustry(),
                stock.getPrice(),
                stock.getVolume(),
                dividend == null ? null : dividend.getDividend()
        );
    }
}

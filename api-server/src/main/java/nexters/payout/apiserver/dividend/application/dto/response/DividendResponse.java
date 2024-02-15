package nexters.payout.apiserver.dividend.application.dto.response;

import nexters.payout.domain.dividend.domain.Dividend;
import nexters.payout.domain.stock.domain.Stock;

public record DividendResponse(
        String ticker,
        String logoUrl,
        Integer share,
        Double dividend,
        Double totalDividend
) {
    public static DividendResponse of(Stock stock, String logoUrl, int share, Dividend dividend) {
        return new DividendResponse(
                stock.getTicker(),
                logoUrl,
                share,
                dividend.getDividend(),
                dividend.getDividend() * share
        );
    }
}

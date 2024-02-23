package nexters.payout.apiserver.stock.application;

import lombok.RequiredArgsConstructor;
import nexters.payout.apiserver.stock.application.dto.request.SectorRatioRequest;
import nexters.payout.apiserver.stock.application.dto.request.TickerShare;
import nexters.payout.apiserver.stock.application.dto.response.*;
import nexters.payout.core.exception.error.NotFoundException;
import nexters.payout.core.time.InstantProvider;
import nexters.payout.domain.dividend.domain.Dividend;
import nexters.payout.domain.dividend.domain.repository.DividendRepository;
import nexters.payout.domain.stock.domain.Sector;
import nexters.payout.domain.stock.domain.Stock;
import nexters.payout.domain.stock.domain.repository.StockRepository;
import nexters.payout.domain.stock.domain.service.DividendAnalysisService;
import nexters.payout.domain.stock.domain.service.SectorAnalysisService;
import nexters.payout.domain.stock.domain.service.SectorAnalysisService.SectorInfo;
import nexters.payout.domain.stock.domain.service.SectorAnalysisService.StockShare;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockQueryService {

    private final StockRepository stockRepository;
    private final DividendRepository dividendRepository;
    private final SectorAnalysisService sectorAnalysisService;
    private final DividendAnalysisService dividendAnalysisService;

    public List<StockResponse> searchStock(final String keyword, final Integer pageNumber, final Integer pageSize) {
        return stockRepository.findStocksByTickerOrNameWithPriority(keyword, pageNumber, pageSize)
                .stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }

    public StockDetailResponse getStockByTicker(final String ticker) {
        Stock stock = getStock(ticker);

        List<Dividend> lastYearDividends = getLastYearDividends(stock);
        List<Dividend> thisYearDividends = getThisYearDividends(stock);

        List<Month> dividendMonths = dividendAnalysisService.calculateDividendMonths(stock, lastYearDividends);
        Double dividendYield = dividendAnalysisService.calculateDividendYield(stock, lastYearDividends);

        return dividendAnalysisService.findEarliestDividend(lastYearDividends, thisYearDividends)
                .map(dividend -> StockDetailResponse.of(stock, dividend, dividendMonths, dividendYield))
                .orElseGet(() -> StockDetailResponse.from(stock));
    }

    private Stock getStock(String ticker) {
        return stockRepository.findByTicker(ticker)
                .orElseThrow(() -> new NotFoundException(String.format("not found ticker [%s]", ticker)));
    }


    private List<Dividend> getLastYearDividends(Stock stock) {
        int lastYear = InstantProvider.getLastYear();

        return dividendRepository.findAllByStockId(stock.getId())
                .stream()
                .filter(dividend -> InstantProvider.toLocalDate(dividend.getPaymentDate()).getYear() == lastYear)
                .collect(Collectors.toList());
    }

    private List<Dividend> getThisYearDividends(Stock stock) {
        int thisYear = InstantProvider.getThisYear();

        return dividendRepository.findAllByStockId(stock.getId())
                .stream()
                .filter(dividend -> InstantProvider.toLocalDate(dividend.getPaymentDate()).getYear() == thisYear)
                .collect(Collectors.toList());
    }

    public List<SectorRatioResponse> analyzeSectorRatio(final SectorRatioRequest request) {
        List<StockShare> stockShares = getStockShares(request);

        Map<Sector, SectorInfo> sectorInfoMap = sectorAnalysisService.calculateSectorRatios(stockShares);

        return SectorRatioResponse.fromMap(sectorInfoMap);
    }

    public List<UpcomingDividendResponse> getUpcomingDividendStocks(int pageNumber, int pageSize) {

        return stockRepository.findUpcomingDividendStock(pageNumber, pageSize).stream()
                .map(stockDividend -> UpcomingDividendResponse.of(
                        stockDividend.stock(),
                        stockDividend.dividend())
                )
                .collect(Collectors.toList());
    }

    public List<StockDividendYieldResponse> getBiggestDividendStocks(int pageNumber, int pageSize) {

        return stockRepository.findBiggestDividendYieldStock(InstantProvider.getLastYear(), pageNumber, pageSize).stream()
                .map(stockDividendYield -> StockDividendYieldResponse.of(
                        stockDividendYield.stock(),
                        stockDividendYield.dividendYield())
                )
                .collect(Collectors.toList());
    }

    private List<StockShare> getStockShares(final SectorRatioRequest request) {
        List<Stock> stocks = stockRepository.findAllByTickerIn(getTickers(request));

        return stocks.stream()
                .map(stock -> new StockShare(
                        stock,
                        getTickerShareMap(request).get(stock.getTicker())))
                .collect(Collectors.toList());
    }

    private List<String> getTickers(final SectorRatioRequest request) {
        return request.tickerShares()
                .stream()
                .map(TickerShare::ticker)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> getTickerShareMap(final SectorRatioRequest request) {
        return request.tickerShares()
                .stream()
                .collect(Collectors.toMap(TickerShare::ticker, TickerShare::share));
    }
}

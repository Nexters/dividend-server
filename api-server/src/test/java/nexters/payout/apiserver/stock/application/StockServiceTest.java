package nexters.payout.apiserver.stock.application;

import nexters.payout.apiserver.stock.application.dto.request.SectorRatioRequest;
import nexters.payout.apiserver.stock.application.dto.request.TickerShare;
import nexters.payout.apiserver.stock.application.dto.response.SectorRatioResponse;
import nexters.payout.apiserver.stock.application.dto.response.StockResponse;
import nexters.payout.domain.StockFixture;
import nexters.payout.domain.stock.Sector;
import nexters.payout.domain.stock.Stock;
import nexters.payout.domain.stock.repository.StockRepository;
import nexters.payout.domain.stock.service.SectorAnalyzer;
import nexters.payout.domain.stock.service.SectorAnalyzer.SectorInfo;
import nexters.payout.domain.stock.service.SectorAnalyzer.StockShare;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static nexters.payout.domain.StockFixture.AAPL;
import static nexters.payout.domain.StockFixture.TSLA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SectorAnalyzer sectorAnalyzer;

    @Test
    void 포트폴리오에_존재하는_종목과_개수_현재가를_기준으로_섹터_정보를_정상적으로_반환한다() {
        // given
        SectorRatioRequest request = new SectorRatioRequest(List.of(new TickerShare(AAPL, 2), new TickerShare(TSLA, 3)));
        Stock appl = StockFixture.createStock(AAPL, Sector.TECHNOLOGY, 4.0);
        Stock tsla = StockFixture.createStock(StockFixture.TSLA, Sector.CONSUMER_CYCLICAL, 2.2);
        List<Stock> stocks = List.of(appl, tsla);

        given(stockRepository.findAllByTickerIn(any())).willReturn(stocks);
        given(sectorAnalyzer.calculateSectorRatios(any())).willReturn(
                Map.of(
                        Sector.TECHNOLOGY, new SectorInfo(0.5479, List.of(new StockShare(appl, 2))),
                        Sector.CONSUMER_CYCLICAL, new SectorInfo(0.4520, List.of(new StockShare(tsla, 3)))
                )
        );

        List<SectorRatioResponse> expected = List.of(
                new SectorRatioResponse(
                        Sector.TECHNOLOGY.getName(),
                        0.5479,
                        List.of(new StockResponse(
                                appl.getTicker(),
                                appl.getName(),
                                appl.getSector(),
                                appl.getExchange(),
                                appl.getIndustry(),
                                appl.getPrice(),
                                appl.getVolume()))
                        ),
                new SectorRatioResponse(
                        Sector.CONSUMER_CYCLICAL.getName(),
                        0.4520,
                        List.of(new StockResponse(
                                tsla.getTicker(),
                                tsla.getName(),
                                tsla.getSector(),
                                tsla.getExchange(),
                                tsla.getIndustry(),
                                tsla.getPrice(),
                                tsla.getVolume()))
                )
        );

        // when
        List<SectorRatioResponse> actual = stockService.findSectorRatios(request);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
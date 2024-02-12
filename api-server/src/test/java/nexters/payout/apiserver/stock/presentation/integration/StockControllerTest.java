package nexters.payout.apiserver.stock.presentation.integration;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import nexters.payout.apiserver.stock.application.dto.request.SectorRatioRequest;
import nexters.payout.apiserver.stock.application.dto.request.TickerShare;
import nexters.payout.apiserver.stock.application.dto.response.SectorRatioResponse;
import nexters.payout.apiserver.stock.common.IntegrationTest;
import nexters.payout.core.exception.ErrorResponse;
import nexters.payout.domain.DividendFixture;
import nexters.payout.domain.StockFixture;
import nexters.payout.domain.stock.Sector;
import nexters.payout.domain.stock.Stock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nexters.payout.domain.StockFixture.AAPL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class StockControllerTest extends IntegrationTest {

    @Test
    void 빈_리스트로_요청한_경우_예외가_발생한다() {
        // given
        SectorRatioRequest request = new SectorRatioRequest(List.of());

        // when, then
        RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("api/stock/sector-ratio")
                .then().log().all()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);
    }

    @Test
    void 티커가_빈문자열이면_예외가_발생한다() {
        // given
        SectorRatioRequest request = new SectorRatioRequest(List.of(new TickerShare("", 1)));

        // when, then
        RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("api/stock/sector-ratio")
                .then().log().all()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);
    }

    @Test
    void 종목_소유_개수가_0개인_경우_예외가_발생한다() {
        // given
        SectorRatioRequest request = new SectorRatioRequest(List.of(new TickerShare(AAPL, 0)));

        // when, then
        RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("api/stock/sector-ratio")
                .then().log().all()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);
    }


    @Test
    void 티커가_1개_이상일_경우_정상적으로_동작한다() {
        // given
        SectorRatioRequest request = new SectorRatioRequest(List.of(new TickerShare(AAPL, 2)));
        Stock stock = stockRepository.save(StockFixture.createStock(AAPL, Sector.TECHNOLOGY, 5.0));
        dividendRepository.save(DividendFixture.createDividend(stock.getId(), 12.0));

        // when
        List<SectorRatioResponse> actual = RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("api/stock/sector-ratio")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        // then
        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.get(0).sectorName()).isEqualTo("Technology"),
                () -> assertThat(actual.get(0).sectorRatio()).isEqualTo(1.0),
                () -> assertThat(actual.get(0).stocks().get(0).ticker()).isEqualTo(AAPL),
                () -> assertThat(actual.get(0).stocks().get(0).dividendPerShare()).isEqualTo(12.0)
        );
    }

    @Test
    void 선택한_종목의_배당금이_존재하지_않아도_정상적으로_동작한다() {
        // given
        SectorRatioRequest request = new SectorRatioRequest(List.of(new TickerShare(AAPL, 2)));
        Stock stock = stockRepository.save(StockFixture.createStock(AAPL, Sector.TECHNOLOGY, 5.0));

        // when
        List<SectorRatioResponse> actual = RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("api/stock/sector-ratio")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        // then
        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.get(0).sectorName()).isEqualTo("Technology"),
                () -> assertThat(actual.get(0).sectorRatio()).isEqualTo(1.0),
                () -> assertThat(actual.get(0).stocks().get(0).ticker()).isEqualTo(AAPL),
                () -> assertThat(actual.get(0).stocks().get(0).dividendPerShare()).isEqualTo(null)
        );
    }
}
package nexters.payout.domain.stock.service;

import nexters.payout.domain.common.config.DomainService;
import nexters.payout.domain.stock.Sector;
import nexters.payout.domain.stock.Stock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DomainService
public class SectorAnalyzer {

    public Map<Sector, Double> calculateSectorRatios(final List<Stock> stocks) {
        Map<Sector, Integer> sectorCountMap = new HashMap<>();
        for (Stock stock : stocks) {
            sectorCountMap.put(stock.getSector(), sectorCountMap.getOrDefault(stock.getSector(), 0) + 1);
        }

        Map<Sector, Double> sectorRatioMap = new HashMap<>();
        int stockSize = stocks.size();

        for (Sector sector : Sector.values()) {
            Integer stockCountBySector = sectorCountMap.getOrDefault(sector, 0);
            if (stockCountBySector > 0) {
                sectorRatioMap.put(sector, (double) stockCountBySector / stockSize);
            }
        }

        return sectorRatioMap;
    }
}
package pt.zerodseis.services.webscraper.templates;

import java.io.Serial;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class CarAdsTemplate implements AdsListTemplate<CarAd> {

    @Serial
    private static final long serialVersionUID = 5620142655897624649L;

    private int resultsPerPage;
    private long totalResults;
    private int page;
    private List<CarAd> ads;
    private String webSite;
}

package pt.zerodseis.services.webscraper.templates;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class CarAd implements Serializable {

    @Serial
    private static final long serialVersionUID = 6088238750726441210L;

    private URI url;
    private URI photoUrl;
    private String brand;
    private String model;
    private String fuel;
    private int mileage;
    private int power;
    private String powerUnits;
    private BigDecimal price;
    private String priceCurrency;

}

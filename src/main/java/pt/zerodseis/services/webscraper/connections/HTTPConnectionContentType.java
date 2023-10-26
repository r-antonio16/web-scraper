package pt.zerodseis.services.webscraper.connections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum HTTPConnectionContentType {

    JSON ("json"),
    STATIC_HTML_PAGE ("static"),
    DYNAMIC_HTML_PAGE ("dynamic");

    private final String typeAbbr;

    @JsonCreator
    public static HTTPConnectionContentType forValue(String value) {
        for (HTTPConnectionContentType connContentType : HTTPConnectionContentType.values()) {
            if (connContentType.typeAbbr.equals(StringUtils.lowerCase(value))) {
                return connContentType;
            }
        }

        return null;
    }

    @JsonValue
    public String toValue() {
        return this.getTypeAbbr();
    }
}

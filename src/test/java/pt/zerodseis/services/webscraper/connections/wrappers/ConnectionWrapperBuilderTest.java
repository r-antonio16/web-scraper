package pt.zerodseis.services.webscraper.connections.wrappers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.connections.HTTPConnectionContentType;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
public class ConnectionWrapperBuilderTest {

    @ParameterizedTest
    @MethodSource("getConnectionWrapperImplByContentTypeToTest")
    public void Should_ReturnConnectionWrapperImpl_When_ByContentType(HTTPConnectionContentType contentType,
                                                                      Class<?> expectedConnectionWrapper)
            throws IOException {
        URLConnectionWrapper urlConnectionWrapper = URLConnectionWrapper.builder()
                            .url(mock(URL.class))
                            .proxy(Proxy.NO_PROXY)
                            .contentType(contentType)
                            .requestProperties(Collections.emptyMap())
                            .build();

        assertEquals(urlConnectionWrapper.getClass(), expectedConnectionWrapper);
    }

    @ParameterizedTest
    @MethodSource("getConnectionPropsToTest")
    public void Should_ThrowException_When_Required_Attribute_IsNull(URL url,
                                                                     Proxy proxy,
                                                                     HTTPConnectionContentType contentType,
                                                                     Map<String, List<Object>> requestProperties) {

        assertThrows(NullPointerException.class, () -> URLConnectionWrapper.builder()
                                                                               .url(url)
                                                                               .proxy(proxy)
                                                                               .contentType(contentType)
                                                                               .requestProperties(requestProperties)
                                                                               .build());
    }

    static Stream<Arguments> getConnectionPropsToTest() {
        return Stream.of(
                Arguments.of(null,
                        Proxy.NO_PROXY, HTTPConnectionContentType.JSON, Collections.emptyMap()),
                Arguments.of(mock(URL.class),
                        null, HTTPConnectionContentType.JSON, Collections.emptyMap()),
                Arguments.of(mock(URL.class),
                        Proxy.NO_PROXY, HTTPConnectionContentType.JSON, null),
                Arguments.of(null,
                        null, null, null)
        );
    }

    static Stream<Arguments> getConnectionWrapperImplByContentTypeToTest() {
        return Stream.of(
                Arguments.of(HTTPConnectionContentType.JSON,
                        DefaultURLConnectionWrapper.class),
                Arguments.of(HTTPConnectionContentType.STATIC_HTML_PAGE,
                        DefaultURLConnectionWrapper.class),
                Arguments.of(HTTPConnectionContentType.DYNAMIC_HTML_PAGE,
                        SeleniumURLConnectionWrapper.class)
        );
    }
}

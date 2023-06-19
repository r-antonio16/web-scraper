package pt.zerodseis.services.webscraper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.exceptions.ReadUserAgentJsonException;
import pt.zerodseis.services.webscraper.utils.UserAgentUtil.UserAgent;

@ExtendWith(SpringExtension.class)
public class UserAgentUtilTest {

    @Test
    public void Should_ReturnDifferentUA_When_getRandomUserAgent_Is_Executed_Multiple_Times() {
        Set<String> randomUAs = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            randomUAs.add(UserAgentUtil.getRandomUserAgent());
        }

        assertFalse(randomUAs.isEmpty());
        assertTrue(randomUAs.size() > 2);
    }

    @Test
    public void Should_ReturnUAs_When_ResourceExists() {
        List<UserAgent> mobileUas = UserAgentUtil.readUAJson("mobile");
        List<UserAgent> desktopUas = UserAgentUtil.readUAJson("desktop");

        assertNotNull(mobileUas);
        assertNotNull(desktopUas);
        assertEquals(10, mobileUas.size());
        assertEquals(10, desktopUas.size());
    }

    @Test
    public void Should_ThrowReadUserAgentJsonException_When_ResourceIsNotAvailable() {
           assertThrows(ReadUserAgentJsonException.class, () -> UserAgentUtil.readUAJson("unknown"));
    }

}

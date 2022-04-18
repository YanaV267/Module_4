package test.epam.esm.validator;

import com.epam.esm.validator.GiftCertificateValidator;
import com.epam.esm.validator.impl.GiftCertificateValidatorImpl;
import com.epam.esm.validator.impl.TagValidatorImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static com.epam.esm.util.ParameterName.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {GiftCertificateValidatorImpl.class, TagValidatorImpl.class})
@EnableAutoConfiguration
class GiftCertificateValidatorTest {
    @Autowired
    private GiftCertificateValidator validator;

    @ParameterizedTest
    @ValueSource(strings = {"travelling", "tour17", "918&@*82"})
    void checkName(String name) {
        boolean actual = validator.checkName(name);
        Assertions.assertTrue(actual);
    }

    @ParameterizedTest
    @MethodSource("provideDescription")
    void checkDescription(String description) {
        boolean actual = validator.checkDescription(description);
        Assertions.assertFalse(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"18.21", "40", "72k2", "182727.19"})
    void checkPrice(String price) {
        boolean actual = validator.checkPrice(price);
        Assertions.assertTrue(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"20", "qwe7", "192"})
    void checkDuration(String duration) {
        boolean actual = validator.checkDuration(duration);
        Assertions.assertTrue(actual);
    }

    @ParameterizedTest
    @MethodSource("provideDateValues")
    void checkCreateDate(String createDate) {
        boolean actual = validator.checkCreateDate(createDate);
        Assertions.assertFalse(actual);
    }

    @ParameterizedTest
    @MethodSource("provideDateValues")
    void checkLastUpdateDate(String lastUpdateDate) {
        boolean actual = validator.checkLastUpdateDate(lastUpdateDate);
        Assertions.assertTrue(actual);
    }

    @ParameterizedTest
    @MethodSource("provideCertificateData")
    void checkCertificate(Map<String, ?> certificateData) {
        boolean actual = validator.checkAllCertificateData(certificateData);
        Assertions.assertFalse(actual);
    }

    private static Object[][] provideDescription() {
        return new Object[][]{
                {"provides various sales on all of products"},
                {"20% discount every saturday"}
        };
    }

    private static Object[][] provideDateValues() {
        return new Object[][]{
                {"2022-02-18"},
                {"2022-04-02T23:14:23"},
                {"2022-03-31 19:04:55"},
                {"2022-04-02T23:14:23.375"},
                {"2022-03-31 15:12:35.209"}
        };
    }

    private static Object[][] provideCertificateData() {
        return new Object[][]{
                {new HashMap<String, String>() {
                    {
                        put(NAME, "europe 5-day tour");
                        put(DESCRIPTION, "You can use it on any tour around Europe provided by our company");
                        put(PRICE, "100");
                        put(DURATION, "60");
                        put(CREATE_DATE, "2022-03-31 19:04:55");
                        put(LAST_UPDATE_DATE, "2022-04-02T23:14:23.375");
                    }
                }}
        };
    }
}

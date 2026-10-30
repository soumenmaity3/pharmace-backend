package pharmase.com.pharmase_monolithi.notification.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OTPEmailService {

    private static final Logger log = LoggerFactory.getLogger(OTPEmailService.class);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a z");
    private static final String TEMPLATE_PATH = "templates/otp-email.html";

    @Value("${resend.api}")
    private String resendApi;

    @Value("${resend.from}")
    private String from;

//    @Value("${pharmase.support-email:soumenmaity.fd2@gmail.com}")
    private String supportEmail;

//    @Value("${pharmase.help-url:https://yourdomain.com/help}")
    private String helpUrl;

//    @Value("${pharmase.privacy-url:https://yourdomain.com/privacy}")
    private String privacyUrl;

//    @Value("${pharmase.terms-url:https://yourdomain.com/terms}")
    private String termsUrl;

    private Resend resend;
    private String template;

    @PostConstruct
    void init() {
        this.resend = new Resend(resendApi);
        try {
            this.template = StreamUtils.copyToString(
                    new ClassPathResource(TEMPLATE_PATH).getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load email template: " + TEMPLATE_PATH, e);
        }
    }

    public void sendOtp(String to, String name, String otp, int expiryMinutes, String purpose,
                        String device, String location, String ip) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("OTP", safe(otp));
        values.put("EXPIRY", String.valueOf(expiryMinutes));
        values.put("PURPOSE", safe(purpose).toUpperCase(java.util.Locale.ROOT));
        values.put("NAME", safe(name));
        values.put("REQUEST_TIME", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(TIME_FMT));
        values.put("DEVICE", safe(device));
        values.put("LOCATION", safe(location));
        values.put("IP_ADDRESS", safe(ip));
        values.put("SUPPORT_EMAIL", safe(supportEmail));
        values.put("HELP_URL", safe(helpUrl));
        values.put("PRIVACY_URL", safe(privacyUrl));
        values.put("TERMS_URL", safe(termsUrl));
        values.put("YEAR", String.valueOf(LocalDate.now(ZoneId.of("Asia/Kolkata")).getYear()));

        String html = render(template, values);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(supportEmail)
                .to(to)
                .subject("Pharmase - Your verification code")
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("OTP email sent, id={}", response.getId());
        } catch (ResendException e) {
            log.error("Failed to send OTP email", e);
            throw new IllegalStateException("Could not send OTP email", e);
        }
    }

    private String render(String source, Map<String, String> values) {
        String result = source;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            // Escape values before inserting them into HTML to prevent markup injection.
            result = result.replace("{{" + entry.getKey() + "}}", HtmlUtils.htmlEscape(entry.getValue()));
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

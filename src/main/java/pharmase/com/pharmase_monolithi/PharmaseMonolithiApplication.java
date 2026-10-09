package pharmase.com.pharmase_monolithi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PharmaseMonolithiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaseMonolithiApplication.class, args);
    }

}

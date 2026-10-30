package pharmase.com.pharmase_monolithi.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pharmase.com.pharmase_monolithi.notification.service.OTPEmailService;

@RestController
@RequestMapping("v2/noti")
public class OTPController {
    @Autowired
    OTPEmailService otpEmailService;

    @PostMapping("send-otp")
    public void sendOTPTo() {
        otpEmailService.sendOtp("soumenmaity.jd1@gmail.com", "Soumen Maity", "102365", 10, "just fun", "POCO M3 PRO", "L+KOlakta", "3216546.6464");
    }
}

package pharmase.com.pharmase_monolithi.billing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pharmase.com.pharmase_monolithi.billing.model.PaymentStatus;
import pharmase.com.pharmase_monolithi.billing.model.dto.BillingDTO;
import pharmase.com.pharmase_monolithi.billing.service.BillingService;

import java.util.UUID;

@RestController
@RequestMapping("v1/bill")
public class BillingController {
    @Autowired
    BillingService service;

    @PostMapping("create")
    public ResponseEntity<?> createBill(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody BillingDTO dto) {
        return service.createBill(authHeader,dto);
    }
    @PutMapping("update")
    public ResponseEntity<?> updatePaymentStatus(@RequestHeader("Authorization") String authHeader,
                                                 @RequestParam("id") Long billId,
                                                 @RequestParam("status")PaymentStatus statusTo){
        return service.updatePaymentStatus(authHeader,billId,statusTo);
    }
    @GetMapping("get-all")
    public ResponseEntity<?> getAllBill(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam("shopId")UUID shopId){
        return service.getAllBill(authHeader,shopId);
    }

    @GetMapping("details")
    public ResponseEntity<?> getBillDetails(@RequestHeader("Authorization") String authHeader,
                                          @RequestParam("id") Long billId){
        return service.getBillDetails(authHeader, billId);
    }
}

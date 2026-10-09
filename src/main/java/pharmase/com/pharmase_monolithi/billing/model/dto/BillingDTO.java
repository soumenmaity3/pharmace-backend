package pharmase.com.pharmase_monolithi.billing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pharmase.com.pharmase_monolithi.billing.model.BillingMedicine;
import pharmase.com.pharmase_monolithi.billing.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class BillingDTO {

    private String patientName;
    private String referredBy;
    private String patientPhoneNo;
    private String customerBy;
    private List<BillingMedicine> medicineList;
    private BigDecimal discount;

    private String paymentThrough;
    private PaymentStatus paymentStatus;
}

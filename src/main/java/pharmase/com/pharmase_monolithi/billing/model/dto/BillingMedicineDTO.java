package pharmase.com.pharmase_monolithi.billing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BillingMedicineDTO {
    private UUID inventoryId;   // which stock item this line refers to
    private Integer quantity;       // strips/units
    private Integer looseQuantity;  // loose units, if applicable
}
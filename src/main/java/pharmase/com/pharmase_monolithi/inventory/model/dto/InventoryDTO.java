package pharmase.com.pharmase_monolithi.inventory.model.dto;

import lombok.*;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.inventory.model.enumns.DosageForm;
import pharmase.com.pharmase_monolithi.inventory.model.enumns.Schedules;
import pharmase.com.pharmase_monolithi.inventory.model.enumns.StockStatus;
import pharmase.com.pharmase_monolithi.shop.model.Shop;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class InventoryDTO {
    private String medName;
    private String genericName;
    private String brand;
    private String category;
    private String strength;
    private DosageForm dosageForm;
    private Schedules schedule;
    private String imageUrl;
    private String batchNo;
    private Integer quantity;
    private Boolean avLoose;
    private Integer looseQuantity;
    private Integer totalLooseInStrip = 10;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private StockStatus stockStatus;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private LocalDate addDate;
    private LocalDate updateDate;
//    private Shop shop;
//    private ShopEmployee addedBy;
//
//    private ShopEmployee updatedBy;
    private String supplier;
    // VALIDATION
}

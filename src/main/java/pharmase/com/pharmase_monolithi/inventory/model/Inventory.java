package pharmase.com.pharmase_monolithi.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.inventory.model.enumns.DosageForm;
import pharmase.com.pharmase_monolithi.inventory.model.enumns.Schedules;
import pharmase.com.pharmase_monolithi.inventory.model.enumns.StockStatus;
import pharmase.com.pharmase_monolithi.shop.model.Shop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_shop_batch", columnNames = {"shop_id", "batch_no"})
        },

        indexes = {
                @Index(name = "idx_inventory_shop_id", columnList = "shop_id"),
                @Index(name = "idx_inventory_batch_no", columnList = "batch_no"),
                @Index(name = "idx_inventory_med_name", columnList = "med_name"),
                @Index(name = "idx_inventory_expiry_date", columnList = "expiry_date"),
                @Index(name = "idx_inventory_shop_expiry", columnList = "shop_id, expiry_date")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotBlank(message = "Medicine name is required")
    @Column(name = "med_name", nullable = false)
    private String medName;

    @NotBlank(message = "Generic name is required")
    @Column(name = "generic_name", nullable = false)
    private String genericName;

    @Column(name = "brand")
    private String brand;

    @Column(name = "category")
    private String category;

    @Column(name = "strength")
    private String strength;

    @Column(name = "dosage_form")
    @Enumerated(EnumType.STRING)
    private DosageForm dosageForm;

    @Column(name = "schedule")
    @Enumerated(EnumType.STRING)
    private Schedules schedule;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;
    @NotBlank(message = "Batch number is required")
    @Column(name = "batch_no", nullable = false)
    private String batchNo;
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    private Boolean avLoose = false;
    private Integer looseQuantity;

    private Integer totalLooseInStrip = 10;

    @Min(value = 0, message = "Minimum stock level cannot be negative")
    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Min(value = 0, message = "Maximum stock level cannot be negative")
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    private StockStatus stockStatus = StockStatus.inStock;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "MRP cannot be negative")
    @Column(name = "mrp", nullable = false, precision = 12, scale = 2)
    private BigDecimal mrp;
    @NotNull(message = "Manufacture date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "manufacture_date", nullable = false)
    private LocalDate manufactureDate;

    @NotNull(message = "Expiry date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "add_date", nullable = false, updatable = false)
    private LocalDate addDate;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "update_date")
    private LocalDate updateDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @NotNull(message = "Shop is required")
    private Shop shop;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false)
    @NotNull(message = "Mention who added this medicine")
    private ShopEmployee addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = true)
    private ShopEmployee updatedBy;
    // SUPPLIER
    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "supplier_id"
//    )
    private String supplier;
    // VALIDATION
    @AssertTrue(message = "Expiry date must be after manufacture date")
    @Transient
    public boolean isExpiryAfterManufacture() {

        if (manufactureDate == null || expiryDate == null) {
            return true;
        }

        return expiryDate.isAfter(manufactureDate);
    }
    @AssertTrue(message = "Maximum stock level must be greater than or equal to minimum stock level")
    @Transient
    public boolean isStockLevelValid() {

        if (minStockLevel == null || maxStockLevel == null) {
            return true;
        }

        return maxStockLevel >= minStockLevel;
    }
    @AssertTrue(message = "Selling price cannot be greater than MRP")
    @Transient
    public boolean isSellingPriceValid() {

        if (sellingPrice == null || mrp == null) {
            return true;
        }

        return sellingPrice.compareTo(mrp) <= 0;
    }
}
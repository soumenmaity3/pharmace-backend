package pharmase.com.pharmase_monolithi.billing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.shop.model.Shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;
    private String referredBy;
    private String patientPhoneNo;
    private String customerBy;

    @OneToMany(
            mappedBy = "billing",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BillingMedicine> medicineList;

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;

    private String paymentThrough;
    private PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "cashier_id")
    private ShopEmployee cashier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = true)
    private ShopEmployee updatedBy;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_date")
    private LocalDateTime updateDate;
}

package pharmase.com.pharmase_monolithi.shop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.shop.model.enums.Verify;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String shopName;
    private String shopCode;
    private String shopEmail;
    @Column(nullable = true)
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String licenseNumber;
    private String gstNumber;
    @Enumerated(EnumType.STRING)
    private Verify verifyStatus;
    private String logoUrl;
    private Boolean isOpen = false;
    private Boolean isOwnerEmployee = false;
    //    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ShopOwner owner;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

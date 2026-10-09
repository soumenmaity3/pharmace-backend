package pharmase.com.pharmase_monolithi.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShopDTO {
    private String shopName;
    private String shopEmail;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String licenseNumber;
    private String gstNumber;
    private String logoUrl;
}

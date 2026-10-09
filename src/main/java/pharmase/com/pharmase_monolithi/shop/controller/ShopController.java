package pharmase.com.pharmase_monolithi.shop.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pharmase.com.pharmase_monolithi.shop.model.Shop;
import pharmase.com.pharmase_monolithi.shop.model.dto.ShopDTO;
import pharmase.com.pharmase_monolithi.shop.service.ShopService;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("v1/shop")
public class ShopController {
    @Autowired
    ShopService service;

    @PostMapping("add-shop")
    public ResponseEntity<?> addShop(@RequestBody ShopDTO shopDTO, @RequestHeader("Authorization") String authHeader) {
        return service.addShop(shopDTO, authHeader);
    }

    @GetMapping("get-all-shop")
    public List<Shop> getShop() {
        return service.getShop();
    }

    @GetMapping("get-owner-shop")
    public ResponseEntity<?> getOwnerShops(@RequestHeader("Authorization") String authHeader) {
        return service.getOwnerShops(authHeader);
    }
    @Transactional
    @PutMapping("shop-is-open")
    public ResponseEntity<?> shopIsOpen(@RequestHeader("Authorization") String authHeader
            , @RequestParam("shopId") String shopId) {
        return service.toggleShopStatus(authHeader,shopId);
    }
}

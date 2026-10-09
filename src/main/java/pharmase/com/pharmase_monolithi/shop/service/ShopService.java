package pharmase.com.pharmase_monolithi.shop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.repository.owner.OwnerRepository;
import pharmase.com.pharmase_monolithi.auth.config.Authorization;
import pharmase.com.pharmase_monolithi.auth.config.JwtService;
import pharmase.com.pharmase_monolithi.shop.model.Shop;
import pharmase.com.pharmase_monolithi.shop.model.dto.ShopDTO;
import pharmase.com.pharmase_monolithi.shop.model.enums.Verify;
import pharmase.com.pharmase_monolithi.shop.repository.ShopRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ShopService {
    @Autowired
    ShopRepository repo;
    @Autowired
    JwtService jwtService;
    @Autowired
    Authorization authorization;
    @Autowired
    OwnerRepository ownerRepo;

    public ResponseEntity<?> addShop(ShopDTO shopDTO, String authHeader) {
        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopOwner> owner = ownerRepo.findByEmailIgnoreCase(email);
        if (owner.isEmpty()) {
            log.info("Owner not found.");
            return new ResponseEntity<>("Owner not found.", HttpStatus.BAD_REQUEST);
        }
        if (shopDTO.getShopName().isEmpty() ||
                shopDTO.getAddress().isEmpty()) {
            return ResponseEntity.badRequest().body("Shop name and address must not be empty.");
        }
        if (repo.existsByOwnerAndShopNameIgnoreCaseAndAddressIgnoreCase(
                owner.get(),
                shopDTO.getShopName(),
                shopDTO.getAddress())) {

            return ResponseEntity.badRequest()
                    .body("This shop already exists.");
        }
        String shopCode;

        do {
            shopCode = "PHM-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();
        } while (repo.existsByShopCode(shopCode));
        Shop shop = new Shop();
        shop.setShopName(shopDTO.getShopName());
        shop.setShopCode(shopCode);
        shop.setShopEmail(shopDTO.getShopEmail());
        shop.setPhone(shopDTO.getPhone());
        shop.setAddress(shopDTO.getAddress());
        shop.setCity(shopDTO.getCity());
        shop.setState(shopDTO.getState());
        shop.setPincode(shopDTO.getPincode());
        shop.setLicenseNumber(shopDTO.getLicenseNumber());
        shop.setGstNumber(shopDTO.getGstNumber());
        shop.setVerifyStatus(Verify.Pending);
        shop.setOwner(owner.get());

        try {
            repo.save(shop);
            owner.get().setIsShopHave(true);
            ownerRepo.save(owner.get());
            return ResponseEntity.ok(shop);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create shop.");
        }
    }

    public List<Shop> getShop() {

        return repo.findAll();
    }

    public ResponseEntity<?> getOwnerShops(String authHeader) {
        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopOwner> owner = ownerRepo.findByEmailIgnoreCase(email);
        if (owner.isEmpty()) {
            return ResponseEntity.badRequest().body("This owner doesn't exist..");
        }
        List<Shop> getOwnerShop = repo.findAllByOwnerId(owner.get().getId());
        if (getOwnerShop.isEmpty()) {
            return ResponseEntity.badRequest().body("This owner doesn't have any shop..");
        }
        return ResponseEntity.ok(getOwnerShop);
    }

    public Shop getShopByIdForOwner(UUID shopId, ShopOwner owner) {
        return repo.findByIdAndOwner_Id(shopId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop not found"
                ));
    }

    public ResponseEntity<?> toggleShopStatus(String authHeader, String shopId) {
        String email = jwtService.extractUsername(authorization.token(authHeader));
        ShopOwner owner = ownerRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop owner not found"
                ));
        UUID uuid;
        try {
            uuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid shop ID format");
        }
        Shop selectedShop = repo.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop not found"
                ));
        if (!selectedShop.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not the owner of this shop.");
        }
        boolean currentStatus = Boolean.TRUE.equals(selectedShop.getIsOpen());
        boolean newStatus = !currentStatus;
        selectedShop.setIsOpen(newStatus);
//        repo.save(selectedShop);
        repo.saveAndFlush(selectedShop);
        return ResponseEntity.ok(newStatus);
    }

}

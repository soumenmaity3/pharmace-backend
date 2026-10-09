package pharmase.com.pharmase_monolithi.inventory.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pharmase.com.pharmase_monolithi.auth.config.Authorization;
import pharmase.com.pharmase_monolithi.auth.config.JwtService;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.repository.employee.EmployeeRepository;
import pharmase.com.pharmase_monolithi.auth.repository.owner.OwnerRepository;
import pharmase.com.pharmase_monolithi.inventory.model.Inventory;
import pharmase.com.pharmase_monolithi.inventory.model.dto.InventoryDTO;
import pharmase.com.pharmase_monolithi.inventory.repository.InventoryRepository;
import pharmase.com.pharmase_monolithi.shop.model.Shop;
import pharmase.com.pharmase_monolithi.shop.repository.ShopRepository;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class InventoryService {
    @Autowired
    InventoryRepository repo;
    @Autowired
    JwtService jwtService;
    @Autowired
    EmployeeRepository employeeRepo;
    @Autowired
    Authorization authorization;
    @Autowired
    ShopRepository shopRepo;
    @Autowired
    OwnerRepository ownerRepo;

    public ResponseEntity<?> addMedicine(String authHeader, InventoryDTO invDTO, String shopId) {
        if (invDTO == null) {
            return ResponseEntity.badRequest().body("Inventory details are required.");
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid shop ID format");
        }
        Shop selectedShop = shopRepo.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop not found"
                ));
        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopEmployee> employee = employeeRepo.findByEmailAndShopId(email, selectedShop.getId());
        if (employee.isEmpty()) {
            log.info("Employee Not Found..");
            return ResponseEntity.badRequest().body("Employee not found..");
        }

        if (!employee.get().getShop().getId().equals(selectedShop.getId())) {
            return ResponseEntity.badRequest().body("Employee does not belong to the selected shop");
        }

        if (invDTO.getMedName() == null || invDTO.getMedName().isBlank()) {
            return ResponseEntity.badRequest().body("Medicine name is required");
        }
        if (invDTO.getGenericName() == null || invDTO.getGenericName().isBlank()) {
            return ResponseEntity.badRequest().body("Generic name is required");
        }
        if (invDTO.getBatchNo() == null || invDTO.getBatchNo().isBlank()) {
            return ResponseEntity.badRequest().body("Batch number is required");
        }
        if (invDTO.getQuantity() == null) {
            return ResponseEntity.badRequest().body("Quantity is required");
        }
        if (invDTO.getQuantity() < 0) {
            return ResponseEntity.badRequest().body("Quantity cannot be negative");
        }
        if (invDTO.getLooseQuantity() != null && invDTO.getLooseQuantity() < 0) {
            return ResponseEntity.badRequest().body("Loose quantity cannot be negative");
        }
        if (invDTO.getTotalLooseInStrip() != null && invDTO.getTotalLooseInStrip() < 0) {
            return ResponseEntity.badRequest().body("Loose strip count cannot be negative");
        }
        if (invDTO.getMinStockLevel() != null && invDTO.getMinStockLevel() < 0) {
            return ResponseEntity.badRequest().body("Minimum stock level cannot be negative");
        }
        if (invDTO.getMaxStockLevel() != null && invDTO.getMaxStockLevel() < 0) {
            return ResponseEntity.badRequest().body("Maximum stock level cannot be negative");
        }
        if (invDTO.getPurchasePrice() == null) {
            return ResponseEntity.badRequest().body("Purchase price is required");
        }
        if (invDTO.getPurchasePrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Purchase price cannot be negative");
        }
        if (invDTO.getSellingPrice() == null) {
            return ResponseEntity.badRequest().body("Selling price is required");
        }
        if (invDTO.getSellingPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Selling price cannot be negative");
        }
        if (invDTO.getMrp() == null) {
            return ResponseEntity.badRequest().body("MRP is required");
        }
        if (invDTO.getMrp().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("MRP cannot be negative");
        }
        if (invDTO.getManufactureDate() == null) {
            return ResponseEntity.badRequest().body("Manufacture date is required");
        }
        if (invDTO.getExpiryDate() == null) {
            return ResponseEntity.badRequest().body("Expiry date is required");
        }
//        if (invDTO.getShop() == null && employee.get().getShop() == null) {
//            return ResponseEntity.badRequest().body("Shop is required");
//        }
        if (invDTO.getExpiryDate().isBefore(invDTO.getManufactureDate())) {
            return ResponseEntity.badRequest().body("Expiry date must be after manufacture date");
        }
        if (invDTO.getMinStockLevel() != null && invDTO.getMaxStockLevel() != null
                && invDTO.getMaxStockLevel() < invDTO.getMinStockLevel()) {
            return ResponseEntity.badRequest().body("Maximum stock level must be greater than or equal to minimum stock level");
        }
        if (invDTO.getSellingPrice().compareTo(invDTO.getMrp()) > 0) {
            return ResponseEntity.badRequest().body("Selling price cannot be greater than MRP");
        }
        try {


            Inventory inventory = new Inventory();
            inventory.setMedName(invDTO.getMedName().trim());
            inventory.setGenericName(invDTO.getGenericName().trim());
            inventory.setBrand(invDTO.getBrand());
            inventory.setCategory(invDTO.getCategory());
            inventory.setStrength(invDTO.getStrength());
            inventory.setDosageForm(invDTO.getDosageForm());
            inventory.setSchedule(invDTO.getSchedule());
            inventory.setImageUrl(invDTO.getImageUrl());
            inventory.setBatchNo(invDTO.getBatchNo().trim());
            inventory.setQuantity(invDTO.getQuantity());
            inventory.setAvLoose(invDTO.getAvLoose() != null ? invDTO.getAvLoose() : false);
            inventory.setLooseQuantity(invDTO.getLooseQuantity());
            inventory.setTotalLooseInStrip(invDTO.getTotalLooseInStrip() == null ? 10 : invDTO.getTotalLooseInStrip());
            inventory.setMinStockLevel(invDTO.getMinStockLevel());
            inventory.setMaxStockLevel(invDTO.getMaxStockLevel());
            inventory.setPurchasePrice(invDTO.getPurchasePrice());
            inventory.setSellingPrice(invDTO.getSellingPrice());
            inventory.setMrp(invDTO.getMrp());
            inventory.setManufactureDate(invDTO.getManufactureDate());
            inventory.setExpiryDate(invDTO.getExpiryDate());
            inventory.setSupplier(invDTO.getSupplier());
            inventory.setShop(selectedShop);
            inventory.setAddedBy(employee.get());
            inventory.setUpdatedBy(employee.get());

            Inventory savedInventory = repo.save(inventory);
            return ResponseEntity.ok(savedInventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> updateMedicine(String authHeader, InventoryDTO invDTO, String shopId, UUID inventoryId) {
        if (invDTO == null) {
            return ResponseEntity.badRequest().body("Inventory details are required.");
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid shop ID format");
        }
        Shop selectedShop = shopRepo.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop not found"
                ));

        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopEmployee> employeeOpt = employeeRepo.findByEmailAndShopId(email, selectedShop.getId());
        if (employeeOpt.isEmpty()) {
            log.info("Employee Not Found..");
            return ResponseEntity.badRequest().body("Employee not found..");
        }

        ShopEmployee employee = employeeOpt.get();

        if (invDTO.getMedName() != null && invDTO.getMedName().isBlank()) {
            return ResponseEntity.badRequest().body("Medicine name cannot be blank");
        }
        if (invDTO.getGenericName() != null && invDTO.getGenericName().isBlank()) {
            return ResponseEntity.badRequest().body("Generic name cannot be blank");
        }

//        var targetShop = invDTO.getShop() != null ? invDTO.getShop() : employee.getShop();
//        if (targetShop == null) {
//            return ResponseEntity.badRequest().body("Shop is required");
//        }

        Inventory inventory = repo.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"
                ));

        if (!inventory.getShop().getId().equals(selectedShop.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This inventory item does not belong to the selected shop");
        }

        // Resolve effective values (incoming value if present, else existing)
        Integer quantity = invDTO.getQuantity() != null ? invDTO.getQuantity() : inventory.getQuantity();
        Integer looseQuantity = invDTO.getLooseQuantity() != null ? invDTO.getLooseQuantity() : inventory.getLooseQuantity();
        Integer totalLooseInStrip = invDTO.getTotalLooseInStrip() != null ? invDTO.getTotalLooseInStrip() : inventory.getTotalLooseInStrip();
        Integer minStockLevel = invDTO.getMinStockLevel() != null ? invDTO.getMinStockLevel() : inventory.getMinStockLevel();
        Integer maxStockLevel = invDTO.getMaxStockLevel() != null ? invDTO.getMaxStockLevel() : inventory.getMaxStockLevel();
        BigDecimal purchasePrice = invDTO.getPurchasePrice() != null ? invDTO.getPurchasePrice() : inventory.getPurchasePrice();
        BigDecimal sellingPrice = invDTO.getSellingPrice() != null ? invDTO.getSellingPrice() : inventory.getSellingPrice();
        BigDecimal mrp = invDTO.getMrp() != null ? invDTO.getMrp() : inventory.getMrp();
        LocalDate manufactureDate = invDTO.getManufactureDate() != null ? invDTO.getManufactureDate() : inventory.getManufactureDate();
        LocalDate expiryDate = invDTO.getExpiryDate() != null ? invDTO.getExpiryDate() : inventory.getExpiryDate();

        // --- Validation against effective (merged) values ---
        if (quantity != null && quantity < 0) {
            return ResponseEntity.badRequest().body("Quantity cannot be negative");
        }
        if (looseQuantity != null && looseQuantity < 0) {
            return ResponseEntity.badRequest().body("Loose quantity cannot be negative");
        }
        if (totalLooseInStrip != null && totalLooseInStrip < 0) {
            return ResponseEntity.badRequest().body("Loose strip count cannot be negative");
        }
        if (minStockLevel != null && minStockLevel < 0) {
            return ResponseEntity.badRequest().body("Minimum stock level cannot be negative");
        }
        if (maxStockLevel != null && maxStockLevel < 0) {
            return ResponseEntity.badRequest().body("Maximum stock level cannot be negative");
        }
        if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Purchase price cannot be negative");
        }
        if (sellingPrice != null && sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Selling price cannot be negative");
        }
        if (mrp != null && mrp.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("MRP cannot be negative");
        }
        if (manufactureDate != null && expiryDate != null && expiryDate.isBefore(manufactureDate)) {
            return ResponseEntity.badRequest().body("Expiry date must be after manufacture date");
        }
        if (minStockLevel != null && maxStockLevel != null && maxStockLevel < minStockLevel) {
            return ResponseEntity.badRequest().body("Maximum stock level must be greater than or equal to minimum stock level");
        }
        if (sellingPrice != null && mrp != null && sellingPrice.compareTo(mrp) > 0) {
            return ResponseEntity.badRequest().body("Selling price cannot be greater than MRP");
        }

        // --- Apply changes ---
        if (invDTO.getMedName() != null && !invDTO.getMedName().isBlank()) {
            inventory.setMedName(invDTO.getMedName().trim());
        }
        if (invDTO.getGenericName() != null && !invDTO.getGenericName().isBlank()) {
            inventory.setGenericName(invDTO.getGenericName().trim());
        }
        if (invDTO.getBrand() != null) inventory.setBrand(invDTO.getBrand());
        if (invDTO.getCategory() != null) inventory.setCategory(invDTO.getCategory());
        if (invDTO.getStrength() != null) inventory.setStrength(invDTO.getStrength());
        if (invDTO.getDosageForm() != null) inventory.setDosageForm(invDTO.getDosageForm());
        if (invDTO.getSchedule() != null) inventory.setSchedule(invDTO.getSchedule());
        if (invDTO.getImageUrl() != null) inventory.setImageUrl(invDTO.getImageUrl());
        if (invDTO.getBatchNo() != null && !invDTO.getBatchNo().isBlank()) {
            inventory.setBatchNo(invDTO.getBatchNo().trim());
        }
        if (invDTO.getQuantity() != null) inventory.setQuantity(invDTO.getQuantity());
        if (invDTO.getAvLoose() != null) inventory.setAvLoose(invDTO.getAvLoose());
        if (invDTO.getLooseQuantity() != null) inventory.setLooseQuantity(invDTO.getLooseQuantity());
        if (invDTO.getTotalLooseInStrip() != null) inventory.setTotalLooseInStrip(invDTO.getTotalLooseInStrip());
        if (invDTO.getMinStockLevel() != null) inventory.setMinStockLevel(invDTO.getMinStockLevel());
        if (invDTO.getMaxStockLevel() != null) inventory.setMaxStockLevel(invDTO.getMaxStockLevel());
        if (invDTO.getPurchasePrice() != null) inventory.setPurchasePrice(invDTO.getPurchasePrice());
        if (invDTO.getSellingPrice() != null) inventory.setSellingPrice(invDTO.getSellingPrice());
        if (invDTO.getMrp() != null) inventory.setMrp(invDTO.getMrp());
        if (invDTO.getManufactureDate() != null) inventory.setManufactureDate(invDTO.getManufactureDate());
        if (invDTO.getExpiryDate() != null) inventory.setExpiryDate(invDTO.getExpiryDate());
        if (invDTO.getSupplier() != null) inventory.setSupplier(invDTO.getSupplier());
        if (selectedShop != null) inventory.setShop(selectedShop);

        inventory.setUpdatedBy(employee);

        try {
            Inventory saved = repo.save(inventory);
            return ResponseEntity.ok(saved);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Could not save inventory: constraint violation");
        } catch (Exception e) {
            log.error("Failed to update inventory batchNo={}, shopId={}", invDTO.getBatchNo(), selectedShop.getId(), e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred while updating inventory");
        }
    }

    public ResponseEntity<?> searchMedicine(String authHeader, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            log.error("Search query is required..");
            return ResponseEntity.badRequest().body("Search query is required..");
        }

        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopEmployee> employeeOpt = employeeRepo.findByEmailIgnoreCase(email);
        if (employeeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Employee not found..");
        }
        ShopEmployee employee = employeeOpt.get();

        List<Inventory> results = repo.searchByShopAndQuery(
                employee.getShop().getId(),
                searchQuery.trim()
        );

        return ResponseEntity.ok(results);
    }

    public ResponseEntity<?> getInventory(String authHeader, String shopId, int page, int size) {
        UUID uuid;
        try {
            uuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid shop ID format");
        }
        Shop selectedShop = shopRepo.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop not found"
                ));
        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopEmployee> employee = employeeRepo.findByEmailAndShopId(email, selectedShop.getId());
        Optional<ShopOwner> owner = ownerRepo.findById(selectedShop.getOwner().getId());
        if (employee.isEmpty()&&owner.isEmpty()) {
            log.info("Employee Not Found..");
            return ResponseEntity.badRequest().body("Employee not found..");
        }

        if (employee.isPresent()) {
            if (!employee.get().getShop().getId().equals(selectedShop.getId())) {
                return ResponseEntity.badRequest()
                        .body("Employee does not belong to the selected shop");
            }
        }
        Pageable pageable = PageRequest.of(page, size);
        List<Inventory> allInv = repo.findByShopIdOrderByExpiryDate(uuid, pageable);
        return ResponseEntity.ok(allInv);
    }

    public ResponseEntity<?> adjustStock(
            String authHeader,
            String shopId,
            UUID inventoryId,
            int adjStock,
            int adjLooseStock
    ) {

        UUID uuid;

        try {
            uuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid shop ID format"
            );
        }

        Shop selectedShop = shopRepo.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop not found"
                ));

        String email = jwtService.extractUsername(
                authorization.token(authHeader)
        );

        ShopEmployee employee = employeeRepo
                .findByEmailAndShopId(email, selectedShop.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found"
                ));

        Inventory inventory = repo.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"
                ));

        if (!inventory.getShop().getId().equals(selectedShop.getId())) {
            return ResponseEntity.badRequest()
                    .body("Inventory does not belong to the selected shop");
        }

        int newStock = /*inventory.getQuantity() + */adjStock;
        int newLooseStock = /*inventory.getLooseQuantity() + */adjLooseStock;

        if (newStock < 0) {
            return ResponseEntity.badRequest()
                    .body("Stock cannot be negative");
        }

        if (newLooseStock < 0) {
            return ResponseEntity.badRequest()
                    .body("Loose stock cannot be negative");
        }

        inventory.setQuantity(newStock);
        inventory.setLooseQuantity(newLooseStock);
        inventory.setUpdatedBy(employee);

        repo.save(inventory);

        return ResponseEntity.ok(inventory);
    }
}

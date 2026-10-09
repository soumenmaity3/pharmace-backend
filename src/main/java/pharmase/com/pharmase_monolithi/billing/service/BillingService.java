package pharmase.com.pharmase_monolithi.billing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pharmase.com.pharmase_monolithi.auth.config.Authorization;
import pharmase.com.pharmase_monolithi.auth.config.JwtService;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.repository.employee.EmployeeRepository;
import pharmase.com.pharmase_monolithi.auth.repository.owner.OwnerRepository;
import pharmase.com.pharmase_monolithi.billing.model.Billing;
import pharmase.com.pharmase_monolithi.billing.model.BillingMedicine;
import pharmase.com.pharmase_monolithi.billing.model.PaymentStatus;
import pharmase.com.pharmase_monolithi.billing.model.dto.BillingDTO;
import pharmase.com.pharmase_monolithi.billing.repository.BillingRepository;
import pharmase.com.pharmase_monolithi.inventory.model.Inventory;
import pharmase.com.pharmase_monolithi.inventory.repository.InventoryRepository;
import pharmase.com.pharmase_monolithi.shop.model.Shop;
import pharmase.com.pharmase_monolithi.shop.repository.ShopRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class BillingService {
    @Autowired
    JwtService jwtService;
    @Autowired
    EmployeeRepository empRepo;
    @Autowired
    OwnerRepository ownerRepo;
    @Autowired
    BillingRepository repo;
    @Autowired
    InventoryRepository inventoryRepo;
    @Autowired
    Authorization authorization;
    @Autowired
    ShopRepository shopRepo;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.00"); // set your actual GST/tax rule here

    @Transactional
    public ResponseEntity<?> createBill(String authHeader, BillingDTO dto) {
        if (dto == null || dto.getMedicineList() == null || dto.getMedicineList().isEmpty()) {
            log.error("Billing details is required..");
            return ResponseEntity.badRequest().body("Billing details is required..");
        }

        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopEmployee> employeeOpt = empRepo.findByEmailIgnoreCase(email);
        if (employeeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Employee not found..");
        }
        ShopEmployee employee = employeeOpt.get();

        Billing billing = new Billing();
        billing.setPatientName(dto.getPatientName());
        billing.setPatientPhoneNo(dto.getPatientPhoneNo());
        billing.setReferredBy(dto.getReferredBy());
        billing.setCustomerBy(dto.getCustomerBy());
        billing.setPaymentThrough(dto.getPaymentThrough());
        billing.setPaymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : PaymentStatus.Due);
        billing.setCashier(employee);
        billing.setShop(employee.getShop());

        List<BillingMedicine> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (BillingMedicine lineDto : dto.getMedicineList()) {
            if (lineDto.getInventory() == null || lineDto.getQuantity() == null || lineDto.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Each medicine line needs a valid inventoryId and quantity.");
            }

            Inventory inv = inventoryRepo.findById(lineDto.getInventory().getId()).orElse(null);
            if (inv == null) {
                return ResponseEntity.badRequest().body("Medicine not found for one of the line items.");
            }
            // Shop-scope check: this medicine must belong to the same shop as the employee
            if (!inv.getShop().getId().equals(employee.getShop().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Medicine does not belong to your shop.");
            }
            if (inv.getQuantity() < lineDto.getQuantity()) {
                return ResponseEntity.badRequest().body("Not enough stock for " + inv.getMedName()
                        + " (available: " + inv.getQuantity() + ", requested: " + lineDto.getQuantity() + ").");
            }

            if (inv.getLooseQuantity() < lineDto.getLooseQuantity()) {
                return ResponseEntity.badRequest().body("Not enough loose stock for " + inv.getMedName()
                        + " (available: " + inv.getLooseQuantity() + ", requested: " + lineDto.getLooseQuantity() + ").");

            }

            // Deduct stock
            inv.setQuantity(inv.getQuantity() - lineDto.getQuantity());
            inv.setLooseQuantity(inv.getLooseQuantity() - lineDto.getLooseQuantity());
            inventoryRepo.save(inv);

            // Snapshot name/price at time of sale
            BillingMedicine line = new BillingMedicine();
            line.setInventory(inv);
            line.setMedicineName(inv.getMedName());
            line.setPrice(inv.getSellingPrice());
            line.setQuantity(lineDto.getQuantity());
            line.setLooseQuantity(lineDto.getLooseQuantity() != null ? lineDto.getLooseQuantity() : 0);
            line.setBilling(billing);
            lineItems.add(line);

            subtotal = subtotal.add(inv.getSellingPrice().multiply(BigDecimal.valueOf(lineDto.getQuantity())));
        }

        BigDecimal discount = dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) {
            return ResponseEntity.badRequest().body("Discount cannot exceed subtotal.");
        }
        BigDecimal taxableAmount = subtotal.subtract(discount);
        BigDecimal tax = taxableAmount.multiply(TAX_RATE);
        BigDecimal total = taxableAmount.add(tax);

        billing.setSubtotal(subtotal);
        billing.setDiscount(discount);
        billing.setTax(tax);
        billing.setTotal(total);
        billing.setMedicineList(lineItems);

        Billing saved = repo.save(billing);
        return ResponseEntity.ok(saved);
    }

    public ResponseEntity<?> updatePaymentStatus(String authHeader, Long billId, PaymentStatus statusTo) {
        if (billId == null || statusTo == null) {
            log.error("Billing details is required..");
            return ResponseEntity.badRequest().body("Billing details is required..");
        }

        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopEmployee> employeeOpt = empRepo.findByEmailIgnoreCase(email);
        if (employeeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Employee not found..");
        }
        ShopEmployee employee = employeeOpt.get();

        Optional<Billing> billOpt = repo.findById(billId);
        if (billOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Bill is not found..");
        }
        Billing bill = billOpt.get();

        if (!bill.getShop().getId().equals(employee.getShop().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this bill.");
        }

        PaymentStatus statusFrom = bill.getPaymentStatus();

        if (statusFrom == PaymentStatus.Refund) {
            return ResponseEntity.badRequest().body("Cannot change status of a refunded bill.");
        }
        if (statusFrom == statusTo) {
            return ResponseEntity.badRequest().body("Bill is already marked as " + statusTo + ".");
        }

        bill.setPaymentStatus(statusTo);
        bill.setUpdatedBy(employee);
        repo.save(bill);

        log.info("Bill {} status changed {} -> {} by {}", billId, statusFrom, statusTo, employee.getEmail());

        return ResponseEntity.ok(bill);
    }

    public ResponseEntity<?> getAllBill(String authHeader, UUID shopId) {
        if (shopId == null) {
            log.error("Shop id is empty..");
            return ResponseEntity.badRequest().body("Shop is required..");
        }

        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopOwner> ownerOpt = ownerRepo.findByEmailIgnoreCase(email);
        Optional<ShopEmployee> empOpt = empRepo.findByEmailIgnoreCase(email);

        if (ownerOpt.isEmpty() && empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("This email is not authorized..");
        }

        if (empOpt.isPresent()) {
            ShopEmployee employee = empOpt.get();
            if (!shopId.equals(employee.getShop() != null ? employee.getShop().getId() : null)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this shop's bills.");
            }
            return ResponseEntity.ok(repo.findByShop_IdOrderByCreateDateDesc(shopId));
        }

        ShopOwner owner = ownerOpt.get();
        Optional<Shop> shopOpt = shopRepo.findById(shopId);
        if (shopOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Shop not found..");
        }
        Shop shop = shopOpt.get();
        if (shop.getOwner() == null || !shop.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this shop's bills.");
        }

        return ResponseEntity.ok(repo.findByShop_IdOrderByCreateDateDesc(shopId));
    }

    public ResponseEntity<?> getBillDetails(String authHeader, Long billId) {
        if (billId == null) {
            log.error("Bill id is empty..");
            return ResponseEntity.badRequest().body("Bill id is required..");
        }

        String email = jwtService.extractUsername(authorization.token(authHeader));
        Optional<ShopOwner> ownerOpt = ownerRepo.findByEmailIgnoreCase(email);
        Optional<ShopEmployee> empOpt = empRepo.findByEmailIgnoreCase(email);

        if (ownerOpt.isEmpty() && empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("This email is not authorized..");
        }

        Optional<Billing> billOpt = repo.findById(billId);
        if (billOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Bill is not found..");
        }

        Billing bill = billOpt.get();

        if (empOpt.isPresent()) {
            ShopEmployee employee = empOpt.get();
            if (bill.getShop() == null || !bill.getShop().getId().equals(employee.getShop().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this bill.");
            }
            return ResponseEntity.ok(bill);
        }

        ShopOwner owner = ownerOpt.get();
        if (bill.getShop() == null || bill.getShop().getOwner() == null || !bill.getShop().getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this bill.");
        }

        return ResponseEntity.ok(bill);
    }
}
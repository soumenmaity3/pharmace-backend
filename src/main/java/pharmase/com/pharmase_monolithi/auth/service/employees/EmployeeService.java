package pharmase.com.pharmase_monolithi.auth.service.employees;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pharmase.com.pharmase_monolithi.auth.config.Authorization;
import pharmase.com.pharmase_monolithi.auth.config.JwtService;
import pharmase.com.pharmase_monolithi.auth.model.employees.Role;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.auth.model.employees.dto.SignIn;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.repository.employee.EmployeeRepository;
import pharmase.com.pharmase_monolithi.auth.repository.owner.OwnerRepository;
import pharmase.com.pharmase_monolithi.shop.model.Shop;
import pharmase.com.pharmase_monolithi.shop.repository.ShopRepository;

import java.util.*;

@Slf4j
@Service
public class EmployeeService {
    @Autowired
    EmployeeRepository repo;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtService jwtService;
    @Autowired
    OwnerRepository ownerRepo;
    @Autowired
    Authorization authorization;
    @Autowired
    ShopRepository shopRepo;

    public ResponseEntity<?> signUp(ShopEmployee emp, String shopId) {
        if (shopId == null || shopId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Shop Id is required.");
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid shop ID format");
        }

        Optional<Shop> shopOptional = shopRepo.findById(uuid);

        String email = emp.getEmail().trim();
        if (email.isEmpty()) {
            return new ResponseEntity<>("Email is required.", HttpStatus.BAD_REQUEST);
        }
        String password = emp.getPassword().trim();
        if (password.isEmpty()) {
            return new ResponseEntity<>("Password must not be empty.", HttpStatus.BAD_REQUEST);
        }
        Role role = emp.getRole();
        if (role == null) {
            log.error("Role is not selected please do again..");
            return ResponseEntity.badRequest().body("Role is not selected please do again..");
        }

        Optional<ShopEmployee> own = repo.findByEmailIgnoreCase(email);
        if (own.isPresent()) {
            return new ResponseEntity<>("This email already exists.", HttpStatus.BAD_REQUEST);
        }
        try {
            ShopEmployee employee = new ShopEmployee();
            employee.setEmail(emp.getEmail());
            employee.setName(emp.getName());
            employee.setPassword(encoder.encode(password));
            employee.setRole(emp.getRole());
            employee.setShop(shopOptional.get());
            repo.save(employee);
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", employee.getEmail());
            claims.put("name", employee.getName());
            claims.put("id", employee.getId());
            claims.put("role", employee.getRole());
            String token = jwtService.generateToken(claims, email);
            Map<String, Object> response = new HashMap<>();
            employee.setPassword("");
            response.put("token", token);
            response.put("body", employee);
            log.info("Profile add successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Profile have error that is: " + e.getMessage());
            return ResponseEntity.badRequest().body("Profile have error that is: " + e.getMessage());
        }

    }

    public ResponseEntity<?> signIn(SignIn signEmp) {
        String email = signEmp.getEmail().trim();
        if (email.isEmpty()) {
            return new ResponseEntity<>("Email is required.", HttpStatus.BAD_REQUEST);
        }
        String password = signEmp.getPassword().trim();
        if (password.isEmpty()) {
            return new ResponseEntity<>("Password must not be empty.", HttpStatus.BAD_REQUEST);
        }
        Optional<ShopEmployee> employee = repo.findByEmailIgnoreCase(email);
        if (employee.isEmpty()) {
            log.error("Employee not found with email: " + email);
            return new ResponseEntity<>("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }
        if (employee.get().getIsFromOwner()) {
            return new ResponseEntity<>("Owner is not accessible for signin as employee with email: " + email, HttpStatus.NOT_ACCEPTABLE);
        }
        ShopEmployee emp = employee.get();
        if (!encoder.matches(password, emp.getPassword())) {
            log.error("Invalid password for email: " + email);
            return new ResponseEntity<>("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", emp.getEmail());
            claims.put("name", emp.getName());
            claims.put("id", emp.getId());
            claims.put("role", emp.getRole());
            String token = jwtService.generateToken(claims, email);
            Map<String, Object> response = new HashMap<>();
            emp.setPassword("");
            response.put("token", token);
            response.put("body", emp);
            log.info("Employee sign in successful for email: " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Sign in error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Sign in error: " + e.getMessage());
        }
    }


    public ResponseEntity<?> getAllEmp(String authHeader, String shopId) {
        String email = jwtService.extractUsername(
                authorization.token(authHeader)
        );

        if (shopId == null || shopId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Shop Id is required.");
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid shop ID format");
        }

        Optional<Shop> shopOptional = shopRepo.findById(uuid);

        if (shopOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Shop not found.");
        }

        Shop shop = shopOptional.get();


//        List<ShopEmployee> emp =
//                repo.findByEmailIgnoreCaseAndShopId(email, shop.getId());
        Optional<ShopOwner> owner =
                ownerRepo.findById(shop.getOwner().getId());

        boolean isOwner = owner
                .map(shopOwner ->
                        shopOwner.getEmail().equalsIgnoreCase(email))
                .orElse(false);

        Optional<ShopEmployee> manager =
                repo.findByEmailAndShopId(email, uuid);

        boolean isManager = manager
                .map(employee -> employee.getRole() == Role.Manager)
                .orElse(false);

        if (!isOwner && !isManager) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the shop owner or a manager can change employee status.");
        }

        List<ShopEmployee> allEmployee =
                repo.findByShopId(shop.getId());

        return ResponseEntity.ok(allEmployee);
    }

    public ResponseEntity<?> getShopById(String shopId, String ownerId) {
        UUID shopUuid;
        UUID ownerUuid;

        try {
            shopUuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid shop ID format"
            );
        }

        Shop selectedShop = shopRepo.findById(shopUuid)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Shop not found"
                        ));

        try {
            ownerUuid = UUID.fromString(ownerId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid shop ID format"
            );
        }
        ShopOwner shopOwner = ownerRepo.findById(ownerUuid).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Owner not found"
                ));
        if (!selectedShop.getOwner().getId().equals(ownerUuid)) {
            return ResponseEntity.badRequest().body("Owner does not belong to the selected shop");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("shopDetails", selectedShop);
        response.put("ownerDetails", shopOwner);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> changeActive(
            String authHeader, String empId, String shopId) {

        String email = jwtService.extractUsername(authorization.token(authHeader));

        UUID shopUuid;
        UUID empUuid;

        try {
            shopUuid = UUID.fromString(shopId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid shop ID format");
        }

        try {
            empUuid = UUID.fromString(empId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid Employee ID format");
        }
        Optional<Shop> shopOptional = shopRepo.findById(shopUuid);

        if (shopOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Shop not found.");
        }

        Shop shop = shopOptional.get();

        Optional<ShopOwner> owner =
                ownerRepo.findById(shop.getOwner().getId());

        boolean isOwner = owner
                .map(shopOwner ->
                        shopOwner.getEmail().equalsIgnoreCase(email))
                .orElse(false);

        Optional<ShopEmployee> manager =
                repo.findByEmailAndShopId(email, shopUuid);

        boolean isManager = manager
                .map(employee -> employee.getRole() == Role.Manager)
                .orElse(false);

        if (!isOwner && !isManager) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the shop owner or a manager can change employee status.");
        }

        Optional<ShopEmployee> employeeOptional = repo.findById(empUuid);

        if (employeeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employee not found.");
        }

        ShopEmployee selectedEmployee = employeeOptional.get();

        if (!selectedEmployee.getShop().getId().equals(shopUuid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Employee does not belong to this shop.");
        }

        if (selectedEmployee.getEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.badRequest()
                    .body("You cannot change your own active status.");
        }

        selectedEmployee.setActiveStatus(
                !selectedEmployee.getActiveStatus()
        );

        repo.save(selectedEmployee);

        return ResponseEntity.ok(selectedEmployee);
    }

}

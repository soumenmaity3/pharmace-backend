package pharmase.com.pharmase_monolithi.auth.service.owner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pharmase.com.pharmase_monolithi.auth.config.Authorization;
import pharmase.com.pharmase_monolithi.auth.model.employees.Role;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.auth.model.owner.dto.CreateEmp;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.model.owner.dto.SignIn;
import pharmase.com.pharmase_monolithi.auth.repository.employee.EmployeeRepository;
import pharmase.com.pharmase_monolithi.auth.repository.owner.OwnerRepository;
import pharmase.com.pharmase_monolithi.auth.config.JwtService;
import pharmase.com.pharmase_monolithi.shop.model.Shop;
import pharmase.com.pharmase_monolithi.shop.repository.ShopRepository;
import pharmase.com.pharmase_monolithi.shop.service.ShopService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OwnerService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    OwnerRepository repo;
    @Autowired
    JwtService jwtService;
    @Autowired
    Authorization auth;
    @Autowired
    ShopService shopService;
    @Autowired
    EmployeeRepository employeeRepo;
    @Autowired
    ShopRepository shopRepo;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ResponseEntity<Boolean> isDbHasThisOwner(String authHeader) {
        String email = jwtService.extractUsername(auth.token(authHeader));

        return repo.findByEmailIgnoreCase(email)
                .map(owner -> ResponseEntity.ok(true))
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> ownerSignUp(ShopOwner owner) {
        String email = owner.getEmail().trim();
        if (email.isEmpty()) {
            return new ResponseEntity<>("Email is required.", HttpStatus.BAD_REQUEST);
        }
        String password = owner.getPassword().trim();
        if (password.isEmpty()) {
            return new ResponseEntity<>("Password must not be empty.", HttpStatus.BAD_REQUEST);
        }
        Optional<ShopOwner> own = repo.findByEmailIgnoreCase(email);
        if (own.isPresent()) {
            return new ResponseEntity<>("This email already exists.", HttpStatus.BAD_REQUEST);
        }
        ShopOwner shopOwner = new ShopOwner();
        shopOwner.setName(owner.getName());
        shopOwner.setEmail(owner.getEmail());
        shopOwner.setPassword(passwordEncoder.encode(password));
        shopOwner.setRole(owner.getRole());
        try {
            repo.save(shopOwner);
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", shopOwner.getEmail());
            claims.put("name", shopOwner.getName());
            claims.put("id", shopOwner.getId());
            claims.put("role", shopOwner.getRole());
            String token = jwtService.generateToken(claims, email);
            shopOwner.setPassword("");
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("details", shopOwner);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create owner: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> ownerSignIn(SignIn owner) {
        String email = owner.getEmail().trim();
        if (email.isEmpty()) {
            return new ResponseEntity<>("Email is required.", HttpStatus.BAD_REQUEST);
        }
        String password = owner.getPassword().trim();
        if (password.isEmpty()) {
            return new ResponseEntity<>("Password must not be empty.", HttpStatus.BAD_REQUEST);
        }
        Optional<ShopOwner> own = repo.findByEmailIgnoreCase(email);
        if (own.isEmpty()) {
            return new ResponseEntity<>("Email not found.", HttpStatus.BAD_REQUEST);
        }
        if (!passwordEncoder.matches(password, own.get().getPassword())) {
            return new ResponseEntity<>("Password does not match.", HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", own.get().getEmail());
        claims.put("name", own.get().getName());
        claims.put("id", own.get().getId());
        claims.put("role", own.get().getRole());
        String token = jwtService.generateToken(email);
        own.get().setPassword("");

        Map<String, Object> response = Map.of("token", token, "details", own.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Boolean> isHaveShop(String authHeader) {
        String email = jwtService.extractUsername(auth.token(authHeader));

        return repo.findByEmailIgnoreCase(email)
                .map(owner -> ResponseEntity.ok(owner.getIsShopHave()))
                .orElse(ResponseEntity.badRequest().body(false));
    }

    public ResponseEntity<?> markSelfEmployee(String authHeader, String shopId) {
        if (shopId.trim().isEmpty()) {
            return new ResponseEntity<>("Shop Id is required.", HttpStatus.BAD_REQUEST);
        }
        final String email = jwtService.extractUsername(auth.token(authHeader));
        ShopOwner owner = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

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

        if (!selectedShop.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not the owner of this shop.");
        }
        Optional<ShopEmployee> selfEmployee = employeeRepo.findByEmailAndShopId(email, uuid);
        if (selfEmployee.isPresent()) {
            return ResponseEntity.badRequest().body("You are already registered as an employee for this shop.");
        }
//        if(selectedShop.getIsOwnerEmployee())
        if (selectedShop.getIsOwnerEmployee()) {
            return ResponseEntity.badRequest().body("You are already registered.");
        }
        ShopEmployee shopEmployee = new ShopEmployee();
        shopEmployee.setEmail(owner.getEmail());
        shopEmployee.setName(owner.getName());
        shopEmployee.setPassword(passwordEncoder.encode(owner.getName()));
        shopEmployee.setIsFromOwner(true);
        shopEmployee.setRole(Role.Manager);
        shopEmployee.setShop(selectedShop);
        employeeRepo.save(shopEmployee);
        selectedShop.setIsOwnerEmployee(true);
        shopRepo.save(selectedShop);
        return ResponseEntity.ok().body("Done");
    }

    private static final String EMP_PREFIX = "employee:";
    private static final String EMAIL_PREFIX = "employee:email:";

    public ResponseEntity<?> createEmp(
            String authHeader,
            CreateEmp emp,
            String shopId) {

        String email = jwtService.extractUsername(auth.token(authHeader));

        ShopOwner owner = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Owner not found"
                        ));

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
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Shop not found"
                        ));

        if (!selectedShop.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You are not the owner of this shop.");
        }

        // Validate email
        if (emp.getEmpEmail() == null ||
                emp.getEmpEmail().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Please enter a valid email address.");
        }
        if (emp.getEmpName() == null ||
                emp.getEmpName().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Please enter a employee name.");
        }

        // Normalize email
        String employeeEmail = emp.getEmpEmail()
                .trim()
                .toLowerCase();

        emp.setEmpEmail(employeeEmail);
        emp.setEmpName(emp.getEmpName());
        emp.setOwnerId(owner.getId());
//      * 1. Check if this email already has an invitation
        String emailKey = EMAIL_PREFIX + employeeEmail;

        Object oldInviteIdObject =
                redisTemplate.opsForValue().get(emailKey);

        if (oldInviteIdObject != null) {

            String oldInviteId = oldInviteIdObject.toString();
//             * Delete old invitation
//             * employee:EMP-1234
            String oldEmployeeKey = EMP_PREFIX + oldInviteId;

            redisTemplate.delete(oldEmployeeKey);
//             * Delete old email mapping
//             * employee:email:test@gmail.com
            redisTemplate.delete(emailKey);
        }
//         * 2. Generate new invite ID
        String inviteId = generateEmpId();
        emp.setInviteId(inviteId);
        emp.setShopId(uuid);
//         * 3. Store employee invitation
        String redisKey = EMP_PREFIX + inviteId;

        redisTemplate.opsForValue().set(
                redisKey,
                emp,
                Duration.ofMinutes(10)
        );
//         * 4. Store email -> inviteId mapping
        redisTemplate.opsForValue().set(
                emailKey,
                inviteId,
                Duration.ofMinutes(10)
        );

        return ResponseEntity.ok(emp);
    }

    public ResponseEntity<?> getEmployeeByInviteId(String inviteId) {

        String redisKey = EMP_PREFIX + inviteId;

        Object employee = redisTemplate.opsForValue().get(redisKey);

        if (employee == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Invite ID expired or not found.");
        }

        return ResponseEntity.ok(employee);
    }

    private String generateEmpId() {

        String inviteId;

        do {
            inviteId = "EMP-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 6)
                    .toUpperCase();

        } while (Boolean.TRUE.equals(
                redisTemplate.hasKey(EMP_PREFIX + inviteId)
        ));

        return inviteId;
    }
}

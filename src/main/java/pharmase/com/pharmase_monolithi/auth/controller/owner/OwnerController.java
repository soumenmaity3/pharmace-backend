package pharmase.com.pharmase_monolithi.auth.controller.owner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pharmase.com.pharmase_monolithi.auth.model.owner.dto.CreateEmp;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.model.owner.dto.SignIn;
import pharmase.com.pharmase_monolithi.auth.service.owner.OwnerService;

@RestController
@RequestMapping("v1/ownr")
public class OwnerController {
    @Autowired
    OwnerService service;

    @GetMapping("health")
    public String checkHealth() {
        return "Service is healthy.";
    }

    @PostMapping("sign-up")
    public ResponseEntity<?> ownerSignUp(@RequestBody ShopOwner owner) {
        return service.ownerSignUp(owner);
    }

    @PostMapping("sign-in")
    public ResponseEntity<?> ownerSignIn(@RequestBody SignIn owner) {
        return service.ownerSignIn(owner);
    }

    @GetMapping("isShopHave")
    public ResponseEntity<?> isHaveShop(@RequestHeader("Authorization") String authHeader) {
        return service.isHaveShop(authHeader);
    }

    @GetMapping("token-valid")
    public ResponseEntity<?> isTokenValid(@RequestHeader("Authorization") String authHeader) {
        return service.isDbHasThisOwner(authHeader);
    }

    @PutMapping("mark-self-employee")
    public ResponseEntity<?> markSelfEmployee(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam("shopId") String shopId) {
        return service.markSelfEmployee(authHeader, shopId);
    }

    @PostMapping("create-emp/{shopId}")
    public ResponseEntity<?> createEmp(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody CreateEmp emp,
                                       @PathVariable("shopId") String shopId) {
        return service.createEmp(authHeader,emp,shopId);
    }
}

package pharmase.com.pharmase_monolithi.auth.controller.employees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.auth.model.employees.dto.SignIn;
import pharmase.com.pharmase_monolithi.auth.service.employees.EmployeeService;
import pharmase.com.pharmase_monolithi.auth.service.owner.OwnerService;

@RestController
@RequestMapping("v1/empe")
public class EmployeeController {
    @Autowired
    EmployeeService service;
    @Autowired
    OwnerService ownerService;

    @PostMapping("register/{shopId}")
    public ResponseEntity<?> signUp(@RequestBody ShopEmployee employee, @PathVariable("shopId") String shopId) {
        return service.signUp(employee, shopId);
    }

    @PostMapping("sign-in")
    public ResponseEntity<?> signIN(@RequestBody SignIn signEmp) {
        return service.signIn(signEmp);
    }

    @GetMapping("get-all/{shopId}")
    public ResponseEntity<?> getAllEmp(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable("shopId") String shopId) {
        return service.getAllEmp(authHeader, shopId);
    }

    @GetMapping("EmpBYInvtId")
    public ResponseEntity<?> getEmpBYInvtId(@RequestParam("inviteId") String inviteId) {
        return ownerService.getEmployeeByInviteId(inviteId);
    }

    @GetMapping("owner-shopById/{ownerId}/{shopId}")
    public ResponseEntity<?> getShopById(@PathVariable("shopId") String shopId,
                                         @PathVariable("ownerId") String ownerId) {
        return service.getShopById(shopId,ownerId);
    }
}

package pharmase.com.pharmase_monolithi.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pharmase.com.pharmase_monolithi.inventory.model.dto.InventoryDTO;
import pharmase.com.pharmase_monolithi.inventory.service.InventoryService;

import java.util.UUID;

@RestController
@RequestMapping("v1/inv")
public class InventoryController {
    @Autowired
    InventoryService service;

    @GetMapping("health-inv")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok("Health Check From Inventory");
    }

    @PostMapping("add-med/{shopId}")
    public ResponseEntity<?> addMedicine(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody InventoryDTO invDTO,@PathVariable("shopId") String shopId) {
        return service.addMedicine(authHeader, invDTO, shopId);
    }

    @PutMapping("update-med/{shopId}/{inventoryId}")
    public ResponseEntity<?> updateMedicine(@RequestBody InventoryDTO invDTO,
                                            @RequestHeader("Authorization") String authHeader,
                                            @PathVariable("shopId") String shopId,
                                            @PathVariable("inventoryId") UUID inventoryId) {
        return service.updateMedicine(authHeader, invDTO, shopId, inventoryId);
    }

    @GetMapping("search-med")
    public ResponseEntity<?> searchMedicine(@RequestHeader("Authorization") String authHeader,
                                            @RequestParam("query")String searchQuery){
        return service.searchMedicine(authHeader,searchQuery);
    }
    @GetMapping("get-inv/{shopId}")
    public ResponseEntity<?> getMedicine(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable("shopId") String shopId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size){
        return service.getInventory(authHeader, shopId,page,size);
    }
    @PutMapping("adj-stock/{shopId}/{inventoryId}")
    public ResponseEntity<?> adjustStock(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable("shopId") String shopId,
                                         @PathVariable("inventoryId") UUID inventoryId,
                                         @RequestParam(value = "adjustStock", defaultValue = "0") int adjStock,
                                         @RequestParam(value = "adjustLooseStock", defaultValue = "0") int adjLooseStock){
        return service.adjustStock(authHeader,shopId,inventoryId,adjStock,adjLooseStock);
    }
}

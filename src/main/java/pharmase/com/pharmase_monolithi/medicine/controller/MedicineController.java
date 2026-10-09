package pharmase.com.pharmase_monolithi.medicine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pharmase.com.pharmase_monolithi.medicine.service.MedicineService;

@RestController
@RequestMapping("v1/med")
public class MedicineController {
    @Autowired
    MedicineService service;

    @GetMapping("search-med-k")
    public ResponseEntity<?> searchMedByKWord(@RequestHeader("Authorization") String authHeader, @RequestParam("med-key") String keyword) {
        return service.searchMedByKWord(authHeader, keyword);
    }

    @GetMapping("se-med-k-pag")
    public ResponseEntity<?> searchMedByKWordOnPage(@RequestHeader("Authorization") String authHeader,
                                                    @RequestParam("med-key") String keyword,
                                                    @RequestParam(defaultValue = "0") int offset,
                                                    @RequestParam(defaultValue = "25") int limit) {
        return service.searchMedByKWordOnPage(authHeader, keyword,offset,limit);
    }
}

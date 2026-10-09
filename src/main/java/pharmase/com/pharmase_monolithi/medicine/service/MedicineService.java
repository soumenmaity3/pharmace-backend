package pharmase.com.pharmase_monolithi.medicine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pharmase.com.pharmase_monolithi.auth.config.Authorization;
import pharmase.com.pharmase_monolithi.auth.config.JwtService;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.repository.employee.EmployeeRepository;
import pharmase.com.pharmase_monolithi.auth.repository.owner.OwnerRepository;
import pharmase.com.pharmase_monolithi.medicine.model.MedicineModel;
import pharmase.com.pharmase_monolithi.medicine.repository.MedicineRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MedicineService {
    @Autowired
    MedicineRepository repo;
    @Autowired
    JwtService jwtService;
    @Autowired
    Authorization authorization;
    @Autowired
    OwnerRepository ownerRepo;

    public ResponseEntity<?> searchMedByKWord(String authHeader, String keyword) {
        String email = jwtService.extractUsername(authorization.token(authHeader));
        if (!authorizedRequest(email)) {
            log.warn("This email is not an owner's email. Only an owner can enter here.");
            return ResponseEntity.badRequest().body("Only an owner can enter here.");
        }
        List<MedicineModel> kMedicine = repo.findByDrugNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(kMedicine);

    }

    public ResponseEntity<?> searchMedByKWordOnPage(String authHeader, String keyword, int offset, int limit) {
        String email = jwtService.extractUsername(authorization.token(authHeader));
        if (!authorizedRequest(email)) {
            log.warn("This email is not an owner's email. Only an owner can enter here.");
            return ResponseEntity.badRequest().body("Only an owner can enter here.");
        }
        List<MedicineModel> medicine = repo.findMedicines(keyword, PageRequest.of(offset, limit));
        return ResponseEntity.ok(medicine);
    }


    boolean authorizedRequest(String email) {
        boolean authorized = ownerRepo.findByEmailIgnoreCase(email).isPresent();

        if (!authorized) {
            log.warn("Only owners can enter here.");
        }

        return authorized;
    }


}

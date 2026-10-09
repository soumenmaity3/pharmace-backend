package pharmase.com.pharmase_monolithi.medicine.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pharmase.com.pharmase_monolithi.medicine.model.MedicineModel;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<MedicineModel, Long> {
    @Query(value = """
            SELECT *
            FROM all_medicine
            WHERE LOWER(drug_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY
                CASE
                    WHEN LOWER(drug_name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0
                    ELSE 1
                END,
                drug_name
                    LIMIT 10
            """, nativeQuery = true)
    List<MedicineModel> findByDrugNameContainingIgnoreCase(String keyword);

    @Query(value = """
    SELECT *
    FROM all_medicine
    WHERE LOWER(drug_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY
        CASE
            WHEN LOWER(drug_name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0
            ELSE 1
        END,
        drug_name
    """, nativeQuery = true)
    List<MedicineModel> findMedicines(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

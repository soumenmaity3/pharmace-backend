package pharmase.com.pharmase_monolithi.medicine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "all_medicine")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MedicineModel {
    @Id
    private Long id;
    @Column(name = "drug_name")
    private String drugName;
    @Enumerated(EnumType.STRING)
    private DrugType drug_type;
    private String manufacturer;
    private Double mrp;
    private Integer pack_size;
    @Enumerated(EnumType.STRING)
    private PackType pack_type;
    private Boolean prescription_required;
    private Double selling_price;
}

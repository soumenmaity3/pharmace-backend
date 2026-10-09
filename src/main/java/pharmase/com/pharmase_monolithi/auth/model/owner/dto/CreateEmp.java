package pharmase.com.pharmase_monolithi.auth.model.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pharmase.com.pharmase_monolithi.auth.model.employees.Role;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmp {
    private String inviteId;
    private String empEmail;
    private String empName;
    private Role empRole;
    private UUID shopId;
    private UUID ownerId;
}

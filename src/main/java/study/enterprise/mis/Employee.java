package study.enterprise.mis;

import java.math.BigDecimal;
import javax.validation.constraints.*;
import com.baomidou.mybatisplus.annotation.*;

@TableName("emp")
public class Employee {
    @TableId(value = "emp_id", type = IdType.AUTO)
    @Positive
    public Long empId;

    @NotBlank @Size(max = 50)
    public String empName;

    @NotNull @Pattern(regexp = "男|女")
    public String gender;

    @NotBlank @Size(max = 50)
    public String dept;

    @NotBlank @Size(max = 50)
    public String post;

    @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2)
    public BigDecimal salary;

    @NotBlank @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}")
    public String hireDate;

    @NotNull @Min(0) @Max(1)
    public Integer status;
}

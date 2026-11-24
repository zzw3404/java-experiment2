package study.trade.mis;

import java.math.BigDecimal;
import javax.validation.constraints.*;
import com.baomidou.mybatisplus.annotation.*;

@TableName("lab_trade_product")
public class Product {
    @TableId(type = IdType.AUTO)
    public Long id;
    @NotBlank @Size(max = 100)
    public String productName;
    @NotBlank @Size(max = 40)
    public String category;
    public String brand;
    @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2)
    public BigDecimal price;
    @NotNull @Min(0)
    public Integer stock;
    @NotNull @Pattern(regexp = "ON_SALE|OFF_SALE")
    public String saleStatus;
    @NotNull @Positive
    public Long brandId;
}

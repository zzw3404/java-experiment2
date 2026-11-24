package study.trade.mis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    @Select("SELECT brand_name FROM lab_trade_brand WHERE id = #{id}")
    String findBrandName(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM lab_trade_order_line WHERE product_id = #{id}")
    int countOrderReferences(@Param("id") long id);
}

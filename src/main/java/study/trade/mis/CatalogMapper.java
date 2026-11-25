package study.trade.mis;

import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CatalogMapper {
    @Select("SELECT b.id AS `id`, b.brand_name AS brand_name, b.website AS `website`, COUNT(p.id) AS product_count "
            + "FROM lab_trade_brand b LEFT JOIN lab_trade_product p ON p.brand_id=b.id "
            + "GROUP BY b.id, b.brand_name, b.website ORDER BY b.id")
    List<CatalogRows.Brand> brands();

    @Select("SELECT o.id AS `id`, o.order_no AS order_no, c.customer_name AS customer_name, "
            + "CAST(o.ordered_on AS CHAR(10)) AS ordered_on, o.order_status AS order_status "
            + "FROM lab_trade_order o JOIN lab_trade_customer c ON o.customer_id=c.id ORDER BY o.id DESC")
    List<CatalogRows.Order> orders();

    @Select("SELECT id FROM lab_trade_order WHERE id=#{id}")
    Long orderId(@Param("id") long id);

    @Select("SELECT l.id AS `id`, l.product_id AS product_id, p.product_name AS product_name, "
            + "l.quantity AS `quantity`, l.deal_price AS deal_price, l.quantity*l.deal_price AS `subtotal` "
            + "FROM lab_trade_order_line l JOIN lab_trade_product p ON l.product_id=p.id "
            + "WHERE l.order_id=#{id} ORDER BY l.id")
    List<CatalogRows.Line> orderLines(@Param("id") long id);
}

package study.trade.mis;

import java.util.List;

public class CatalogService {
    private final CatalogMapper mapper;

    public CatalogService(CatalogMapper mapper) {
        this.mapper = mapper;
    }

    public List<CatalogRows.Brand> brands() {
        return mapper.brands();
    }

    public List<CatalogRows.Order> orders() {
        return mapper.orders();
    }

    public List<CatalogRows.Line> orderLines(long id) {
        if (mapper.orderId(id) == null) {
            throw new Failure(404, "订单不存在");
        }
        return mapper.orderLines(id);
    }
}

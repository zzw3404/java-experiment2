package study.trade.mis;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog")
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/brands")
    public Result<List<CatalogRows.Brand>> brands() {
        return Result.ok(service.brands());
    }

    @GetMapping("/orders")
    public Result<List<CatalogRows.Order>> orders() {
        return Result.ok(service.orders());
    }

    @GetMapping("/orders/{id}/lines")
    public Result<List<CatalogRows.Line>> orderLines(@PathVariable("id") long id) {
        return Result.ok(service.orderLines(id));
    }
}

package study.trade.mis;

import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Page<Product>> page(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brandId", required = false) Long brandId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return Result.ok(service.search(name, category, brandId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable("id") long id) {
        return Result.ok(service.find(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody Product product) {
        return Result.ok(service.create(product));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Product product) {
        service.update(product);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") long id) {
        service.delete(Collections.singletonList(id));
        return Result.ok(null);
    }

    @DeleteMapping
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        service.delete(ids);
        return Result.ok(null);
    }
}

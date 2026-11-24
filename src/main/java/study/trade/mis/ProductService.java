package study.trade.mis;

import java.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.transaction.annotation.Transactional;

public class ProductService {
    private final ProductMapper mapper;

    public ProductService(ProductMapper mapper) {
        this.mapper = mapper;
    }

    public Page<Product> search(String name, String category, Long brandId, String status, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new Failure(400, "page 至少为 1，size 为 1 至 100");
        }
        if (status != null && !Arrays.asList("ON_SALE", "OFF_SALE").contains(status)) {
            throw new Failure(400, "状态无效");
        }
        QueryWrapper<Product> query = new QueryWrapper<>();
        query.like(name != null && !name.trim().isEmpty(), "product_name", name);
        query.eq(category != null && !category.trim().isEmpty(), "category", category);
        query.eq(brandId != null, "brand_id", brandId);
        query.eq(status != null, "sale_status", status);
        return mapper.selectPage(new Page<>(page, size), query.orderByDesc("id"));
    }

    public Product find(long id) {
        Product product = mapper.selectById(id);
        if (product == null) {
            throw new Failure(404, "商品不存在");
        }
        return product;
    }

    private void applyBrand(Product product) {
        product.brand = mapper.findBrandName(product.brandId);
        if (product.brand == null) {
            throw new Failure(400, "品牌不存在");
        }
    }

    public long create(Product product) {
        product.id = null;
        applyBrand(product);
        mapper.insert(product);
        return product.id;
    }

    public void update(Product product) {
        if (product.id == null) {
            throw new Failure(400, "缺少商品 ID");
        }
        applyBrand(product);
        if (mapper.updateById(product) == 0) {
            throw new Failure(404, "商品不存在");
        }
    }

    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 100 ||
                ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new Failure(400, "请提供 1 至 100 个有效商品 ID");
        }
        for (Long id : new LinkedHashSet<>(ids)) {
            if (mapper.countOrderReferences(id) > 0) {
                throw new Failure(409, "商品已有订单记录，无法删除");
            }
            if (mapper.deleteById(id) == 0) {
                throw new Failure(404, "商品不存在");
            }
        }
    }
}

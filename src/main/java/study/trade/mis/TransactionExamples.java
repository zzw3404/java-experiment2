package study.trade.mis;

import org.springframework.transaction.annotation.Transactional;

public class TransactionExamples {
    private final ProductService service;

    public TransactionExamples(ProductService service) {
        this.service = service;
    }

    public void withoutTransaction(Product product) {
        service.create(product);
        throw new IllegalStateException("故意中断事务实验");
    }

    @Transactional
    public void withTransaction(Product product) {
        withoutTransaction(product);
    }

    @Transactional
    public void swallowed(Product product) {
        try {
            withoutTransaction(product);
        } catch (IllegalStateException expected) {
            // 演示异常未传到代理，正常返回将提交。
        }
    }

    public void selfCall(Product product) {
        this.withTransaction(product);
    }

    public void privateCall(Product product) {
        privateTransaction(product);
    }

    @Transactional
    private void privateTransaction(Product product) {
        withoutTransaction(product);
    }
}

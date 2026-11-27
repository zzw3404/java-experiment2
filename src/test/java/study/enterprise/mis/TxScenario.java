package study.enterprise.mis;

import org.springframework.transaction.annotation.Transactional;

public class TxScenario {
    private final EmployeeService service;

    public TxScenario(EmployeeService service) {
        this.service = service;
    }

    public void withoutTransaction(Employee employee) {
        service.create(employee);
        throw new IllegalStateException("故意中断事务实验");
    }

    @Transactional
    public void withTransaction(Employee employee) {
        withoutTransaction(employee);
    }

    @Transactional
    public void swallowed(Employee employee) {
        try {
            withoutTransaction(employee);
        } catch (IllegalStateException expected) {
            // 演示异常未传到代理，正常返回将提交。
        }
    }

    public void selfCall(Employee employee) {
        this.withTransaction(employee);
    }

    public void privateCall(Employee employee) {
        privateTransaction(employee);
    }

    @Transactional
    private void privateTransaction(Employee employee) {
        withoutTransaction(employee);
    }
}

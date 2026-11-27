package study.enterprise.mis;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.transaction.annotation.Transactional;

public class EmployeeService {
    private final EmployeeMapper mapper;

    public EmployeeService(EmployeeMapper mapper) {
        this.mapper = mapper;
    }

    public Page<Employee> search(String name, String dept, String gender, Integer status, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new Failure(400, "页码至少为 1，每页记录数必须为 1 至 100");
        }
        if (status != null && status != 0 && status != 1) {
            throw new Failure(400, "员工状态仅允许 0 或 1");
        }
        QueryWrapper<Employee> query = new QueryWrapper<>();
        if (name != null && !name.isBlank()) query.like("emp_name", name.strip());
        if (dept != null && !dept.isBlank()) query.eq("dept", dept.strip());
        query.eq(gender != null && !gender.isBlank(), "gender", gender);
        query.eq(status != null, "status", status);
        return mapper.selectPage(new Page<>(page, size), query.orderByDesc("emp_id"));
    }

    public Employee find(long id) {
        Employee employee = mapper.selectById(id);
        if (employee == null) throw new Failure(404, "员工不存在");
        return employee;
    }

    public long create(Employee employee) {
        employee.empId = null;
        normalize(employee);
        mapper.insert(employee);
        return employee.empId;
    }

    public void update(Employee employee) {
        if (employee.empId == null) throw new Failure(400, "缺少员工编号");
        normalize(employee);
        if (mapper.updateById(employee) == 0) throw new Failure(404, "员工不存在");
    }

    private void normalize(Employee employee) {
        try {
            LocalDate date = LocalDate.parse(employee.hireDate);
            if (date.getYear() < 1000) throw new Failure(400, "入职日期年份至少为 1000");
        } catch (DateTimeParseException ex) {
            throw new Failure(400, "入职日期无效");
        }
        employee.empName = employee.empName.strip();
        employee.dept = employee.dept.strip();
        employee.post = employee.post.strip();
    }

    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 100 ||
                ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new Failure(400, "请提供 1 至 100 个有效员工编号");
        }
        for (Long id : new LinkedHashSet<>(ids)) {
            if (mapper.deleteById(id) == 0) throw new Failure(404, "员工不存在：" + id);
        }
    }

    public List<Employee> department(String name) {
        if (name == null || name.isBlank()) throw new Failure(400, "部门名称不能为空");
        return mapper.selectList(new QueryWrapper<Employee>().eq("dept", name.strip())
                .orderByAsc("emp_id"));
    }

    public List<DepartmentSummary> departmentSummary() {
        return mapper.summarizeDepartments();
    }
}

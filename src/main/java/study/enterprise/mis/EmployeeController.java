package study.enterprise.mis;

import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emps")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Page<Employee>> page(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "dept", required = false) String dept,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return Result.ok(service.search(name, dept, gender, status, page, size));
    }

    @GetMapping("/departments")
    public Result<List<DepartmentSummary>> departments() {
        return Result.ok(service.departmentSummary());
    }

    @GetMapping("/department")
    public Result<List<Employee>> department(@RequestParam("name") String name) {
        return Result.ok(service.department(name));
    }

    @GetMapping("/{id}")
    public Result<Employee> detail(@PathVariable("id") long id) {
        return Result.ok(service.find(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody Employee employee) {
        return Result.ok(service.create(employee));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Employee employee) {
        service.update(employee);
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

package study.enterprise.mis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
    @Select("SELECT dept, COUNT(*) AS total, SUM(status) AS active FROM emp GROUP BY dept ORDER BY dept")
    List<DepartmentSummary> summarizeDepartments();
}

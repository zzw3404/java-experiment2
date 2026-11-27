CREATE TABLE emp (
    emp_id INT PRIMARY KEY AUTO_INCREMENT,
    emp_name VARCHAR(50) NOT NULL,
    gender CHAR(1) NOT NULL DEFAULT '男',
    dept VARCHAR(50) NOT NULL,
    post VARCHAR(50) NOT NULL,
    salary DECIMAL(10,2) NOT NULL CHECK (salary >= 0),
    hire_date DATE NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 CHECK (status IN (0, 1)),
    CHECK (gender IN ('男', '女'))
);

CREATE INDEX idx_emp_name ON emp(emp_name);
CREATE INDEX idx_emp_dept_status ON emp(dept, status);

INSERT INTO emp (emp_name, gender, dept, post, salary, hire_date, status) VALUES
('林晨', '女', '研发部', 'Java 工程师', 12500.00, '2023-04-17', 1),
('陈远', '男', '研发部', '测试工程师', 9800.00, '2024-02-21', 1),
('何晓', '女', '人力资源部', '招聘专员', 8500.00, '2022-07-11', 1),
('周宁', '男', '财务部', '会计', 9200.00, '2021-09-06', 0),
('宋琳', '女', '运营部', '运营专员', 8800.00, '2025-03-03', 1);

package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private EmployeeService employeeService;

    /**
     * 查询信息
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee= employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 启用和禁用
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee=Employee.builder().status(status).id(id).build();
        employeeMapper.update(employee);
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO 分页查询条件（页码、每页条数、员工姓名关键词）
     * @return 分页查询结果（总记录数 + 当前页数据集合）
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 1、设置分页参数：将页码和每页条数存入 ThreadLocal，PageHelper 会拦截紧随其后的第一个查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 2、执行分页查询：PageHelper 自动拦截，帮你执行 count 查询（查总数）和 limit 查询（查当前页数据）
        //    返回的 Page 对象是加强版 List，既装着当前页数据，又存着总记录数等分页信息
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        // 3、从 Page 对象中取出总记录数
        long total = page.getTotal();

        // 4、从 Page 对象中取出当前页的数据集合
        List<Employee> records = page.getResult();

        // 5、封装成统一的分页结果对象返回
        return new PageResult(total, records);
    }

    /**
     * 新增员工
     *
     * @param employeeDTO 前端传来的员工信息（用户名、姓名、手机号等）
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 1、创建员工实体对象，用于和数据库表做映射
        Employee employee = new Employee();

        // 2、属性拷贝：将 DTO 中同名的属性值自动拷贝到实体对象中
        //    （DTO 用来接收前端数据，实体对应数据库表，两者字段大部分相同）
        BeanUtils.copyProperties(employeeDTO, employee);

        // 3、设置账号状态为启用（1=启用，0=禁用）
        employee.setStatus(StatusConstant.ENABLE);

        // 4、设置初始密码：使用默认密码 123456，进行 MD5 加密后存入数据库
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 5、调用 Mapper 将员工数据插入到数据库（公共字段由切面自动填充）
        employeeMapper.insert(employee);
    }

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端来的密码加密处理
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO 前端传来的员工信息（包含id、姓名、用户名、手机号等）
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        // 1、创建员工实体对象
        Employee employee = new Employee();

        // 2、属性拷贝：将 DTO 中同名的属性值自动拷贝到实体对象中（包含id，用于定位更新哪条记录）
        BeanUtils.copyProperties(employeeDTO, employee);

        // 3、调用 Mapper 动态更新员工信息（公共字段由切面自动填充）
        employeeMapper.update(employee);
    }

}

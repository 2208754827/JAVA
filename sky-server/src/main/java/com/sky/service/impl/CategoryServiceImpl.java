package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务层
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增分类
     *
     * @param categoryDTO 前端传来的分类信息（类型、名称、排序）
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        // 1、创建分类实体对象
        Category category = new Category();

        // 2、属性拷贝：将 DTO 中同名的属性值自动拷贝到实体对象中（type、name、sort）
        BeanUtils.copyProperties(categoryDTO, category);

        // 3、设置分类状态默认为禁用（0=禁用，1=启用），新增的分类默认不展示
        category.setStatus(StatusConstant.DISABLE);

        // 4、调用 Mapper 将分类数据插入到数据库（公共字段由切面自动填充）
        categoryMapper.insert(category);
    }

    /**
     * 根据类型查询分类
     *
     * @param type 分类类型（1=菜品分类，2=套餐分类）
     * @return
     */
    @Override
    public List<Category> get(Integer type) {
        List<Category> categoryList = categoryMapper.get(type);
        return categoryList;
    }

    /**
     * 条件查询分类（用户端，只查启用状态）
     *
     * @param type 分类类型（1=菜品分类，2=套餐分类）
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO 分页查询条件（页码、每页条数、名称、类型）
     * @return
     */
    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 1、开启分页插件，设置当前页码和每页条数
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        // 2、执行查询，PageHelper 会自动在 SQL 后拼接 limit，并返回 Page 对象
        Page<Category> page = categoryMapper.page(categoryPageQueryDTO);

        // 3、从 Page 对象中取出总记录数
        long total = page.getTotal();

        // 4、从 Page 对象中取出当前页数据集合
        List<Category> records = page.getResult();

        // 5、封装成 PageResult 返回
        return new PageResult(total, records);
    }

    /**
     * 根据id删除分类
     *
     * @param id 分类id
     */
    @Override
    public void del(Long id) {
        // 1、查询当前分类下有没有关联菜品，有就不能删
        Integer dishCount = categoryMapper.countCategoryDish(id);
        if (dishCount > 0) {
            throw new DeletionNotAllowedException("当前分类下关联了菜品，不能删除");
        }

        // 2、查询当前分类下有没有关联套餐，有就不能删
        Integer setmealCount = categoryMapper.countCategorySetmeal(id);
        if (setmealCount > 0) {
            throw new DeletionNotAllowedException("当前分类下关联了套餐，不能删除");
        }

        // 3、都没关联才执行删除
        categoryMapper.del(id);
    }

    /**
     * 启用/禁用分类
     *
     * @param status 状态（0=禁用，1=启用）
     * @param id     分类id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 1、创建分类对象，只设置需要更新的字段（公共字段由切面自动填充）
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();

        // 2、执行动态更新（只更新非空字段）
        categoryMapper.update(category);
    }

    /**
     * 修改分类
     *
     * @param categoryDTO 前端传来的修改信息
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        // 1、创建分类实体对象
        Category category = new Category();

        // 2、属性拷贝：将 DTO 中同名的属性值拷贝到实体对象中（id、type、name、sort）
        BeanUtils.copyProperties(categoryDTO, category);

        // 3、执行动态更新（公共字段由切面自动填充）
        categoryMapper.update(category);
    }

}

package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品（同时插入菜品和口味）
     *
     * @param dishDTO 前端传来的菜品信息（含口味列表）
     */
    void insertDish(DishDTO dishDTO);

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO 分页查询条件
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     *
     * @param ids 菜品id集合
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品（回显）
     *
     * @param id 菜品id
     * @return
     */
    DishVO getDish(Long id);

    /**
     * 修改菜品（同时更新菜品和口味）
     *
     * @param dishDTO 前端传来的菜品信息（含口味列表）
     */
    void update(DishDTO dishDTO);

    /**
     * 菜品起售停售
     *
     * @param status 状态（0停售 1起售）
     * @param id 菜品id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询起售中的菜品（套餐新增/修改时的下拉框用）
     *
     * @param categoryId 分类id
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 条件查询菜品和口味（用户端）
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

}

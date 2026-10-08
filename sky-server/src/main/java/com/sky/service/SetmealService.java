package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 新增套餐（同时保存套餐和菜品的关联关系）
     *
     * @param setmealDTO 前端传来的套餐信息（含套餐菜品关系列表）
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO 分页查询条件
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id集合
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐和关联的菜品数据（回显）
     *
     * @param id 套餐id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐（同时更新套餐和菜品的关联关系）
     *
     * @param setmealDTO 前端传来的套餐信息（含套餐菜品关系列表）
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 套餐起售停售
     *
     * @param status 状态（0停售 1起售）
     * @param id 套餐id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询套餐（用户端）
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项（用户端）
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

}

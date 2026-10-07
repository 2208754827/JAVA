package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询关联的套餐数量
     *
     * @param dishId 菜品id
     * @return 关联的套餐数量
     */
    @Select("select count(*) from setmeal_dish where dish_id = #{dishId}")
    Integer countByDishId(Long dishId);

    /**
     * 根据菜品id集合查询关联的套餐id集合
     *
     * @param dishIds 菜品id集合
     * @return 套餐id集合
     */
    List<Long> getSetmealIdsByDishIds(@Param("dishIds") List<Long> dishIds);

    /**
     * 批量插入套餐菜品关系
     *
     * @param setmealDishes 套餐菜品关系集合
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐菜品关系
     *
     * @param setmealId 套餐id
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐id查询套餐菜品关系
     *
     * @param setmealId 套餐id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

}

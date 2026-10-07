package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 插入菜品
     *
     * @param dish 菜品对象
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO 分页查询条件
     * @return
     */
    Page<DishVO> queryPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     *
     * @param id 菜品id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据id集合批量删除菜品
     *
     * @param ids 菜品id集合
     */
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据菜品id查询口味
     *
     * @param id 菜品id
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getFlavor(Long id);

    /**
     * 修改菜品
     *
     * @param dish 菜品对象
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 动态条件查询菜品（name/categoryId/status）
     *
     * @param dish 查询条件对象
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐id查询包含的菜品（左连接查询）
     *
     * @param setmealId 套餐id
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

}

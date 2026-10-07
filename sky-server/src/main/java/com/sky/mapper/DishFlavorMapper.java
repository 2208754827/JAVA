package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味
     *
     * @param flavors 口味集合
     */
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    /**
     * 根据菜品id批量删除口味
     *
     * @param dishIds 菜品id集合
     */
    void deleteByDishIds(@Param("dishIds") List<Long> dishIds);

}

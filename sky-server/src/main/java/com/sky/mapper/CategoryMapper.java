package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 插入数据
     * @param category
     */
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Category category);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Select("select * from category where type = #{type}")
    List<Category> get(Integer type);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> page(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    void del(Long id);

    /**
     * 根据分类id查询关联的菜品数量
     * @param id
     * @return
     */
    @Select("select count(*) from dish where category_id = #{id}")
    Integer countCategoryDish(Long id);

    /**
     * 根据分类id查询关联的套餐数量
     * @param id
     * @return
     */
    @Select("select count(*) from setmeal where category_id = #{id}")
    Integer countCategorySetmeal(Long id);

    /**
     * 修改分类
     * @param category
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

}

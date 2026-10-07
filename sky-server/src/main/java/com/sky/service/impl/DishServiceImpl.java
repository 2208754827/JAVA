package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page= dishMapper.queryPage(dishPageQueryDTO);

        return new PageResult (page.getTotal(),page.getResult());

    }

    /**
     * 新增菜品（同时插入菜品和口味）
     *
     * @param dishDTO 前端传来的菜品信息（含口味列表）
     */
    @Override
    @Transactional
    public void insertDish(DishDTO dishDTO) {

        // 1、创建菜品实体对象，属性拷贝（name、categoryId、price、image、description）
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 2、设置菜品默认状态为起售（1=起售，0=停售）
        dish.setStatus(StatusConstant.ENABLE);

        // 3、插入菜品表（插入后自增主键 id 会回填到 dish 对象中）
        dishMapper.insert(dish);

        // 4、获取刚才插入菜品的自增 id
        Long dishId = dish.getId();

        // 5、获取前端传来的口味列表
        List<DishFlavor> flavors = dishDTO.getFlavors();

        // 6、口味列表不为空时，批量插入口味
        if (flavors != null && flavors.size() > 0) {
            // 遍历口味列表，给每个口味设置菜品 id
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            // 批量插入口味表
            dishFlavorMapper.insertBatch(flavors);
        }

    }


    @Override
    public DishVO getDish(Long id) {

             Dish dish= dishMapper.getById(id);

             List<DishFlavor> flavors=dishMapper.getFlavor(id);

             DishVO dishVO=new DishVO();
             BeanUtils.copyProperties(dish,dishVO);
             dishVO.setFlavors(flavors);

             return dishVO;
    }

    /**
     * 批量删除菜品
     *
     * @param ids 菜品id集合
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {

        // 1、遍历每个菜品id，校验是否可以删除
        for (Long id : ids) {
            // 1.1、根据id查询菜品
            Dish dish = dishMapper.getById(id);

            // 1.2、如果菜品处于起售中（status=1），不能删除
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

            // 1.3、查询菜品是否被套餐关联，如果关联了不能删除
            Integer count = setmealDishMapper.countByDishId(id);
            if (count > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }

        // 2、批量删除菜品表数据
        dishMapper.deleteByIds(ids);

        // 3、批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 修改菜品（同时更新菜品和口味）
     *
     * @param dishDTO 前端传来的菜品信息（含口味列表）
     */
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {

        // 1、属性拷贝，更新菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 2、删除该菜品的旧口味
        List<Long> dishIds = new ArrayList<>();
        dishIds.add(dishDTO.getId());
        dishFlavorMapper.deleteByDishIds(dishIds);

        // 3、插入新口味（如果有）
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品起售停售
     *
     * @param status 状态（0停售 1起售）
     * @param id 菜品id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {

        // 1、更新菜品状态
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        // 2、如果是停售操作，需要把包含当前菜品的套餐也停售
        if (status == StatusConstant.DISABLE) {
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // 查询包含该菜品的套餐id集合
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                // 遍历套餐id，逐个停售
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }

    }

    /**
     * 根据分类id查询起售中的菜品
     *
     * @param categoryId 分类id
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        // 用Builder构建查询条件，只查起售中的菜品
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

}

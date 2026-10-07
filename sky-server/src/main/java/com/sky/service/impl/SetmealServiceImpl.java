package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐（同时保存套餐和菜品的关联关系）
     *
     * @param setmealDTO 前端传来的套餐信息（含套餐菜品关系列表）
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 1、属性拷贝，新增套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        // 2、获取生成的套餐id，给套餐菜品关系设置setmealId
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            // 3、批量插入套餐菜品关系
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO 分页查询条件
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id集合
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 1、遍历校验：起售中的套餐不能删除
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        // 2、遍历删除：删套餐表 + 删套餐菜品关系表
        ids.forEach(setmealId -> {
            setmealMapper.deleteById(setmealId);
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }

    /**
     * 根据id查询套餐和关联的菜品数据（回显）
     *
     * @param id 套餐id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        // 1、查套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);
        // 2、查套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        // 3、组装成VO返回
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐（同时更新套餐和菜品的关联关系）
     *
     * @param setmealDTO 前端传来的套餐信息（含套餐菜品关系列表）
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 1、属性拷贝，修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 2、删除该套餐的旧菜品关系
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);

        // 3、重新插入套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐起售停售
     *
     * @param status 状态（0停售 1起售）
     * @param id 套餐id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 1、起售套餐时，校验套餐内是否有停售的菜品
        if (status == StatusConstant.ENABLE) {
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    if (dish.getStatus() == StatusConstant.DISABLE) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        // 2、更新套餐状态
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

}

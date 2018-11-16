package cn.itcast.core.service.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;

public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    List<Brand> findAll();

    /**
     * 分页查询
     * @param pageSize
     * @param pageNo
     * @return
     */
    PageResult findPage(Integer pageNo, Integer pageSize);

    /**
     * 条件查询
     * @param pageNo
     * @param pageSize
     * @param brand
     * @return PageResult
     */
    PageResult search(Integer pageNo, Integer pageSize, Brand brand);

    /**
     * 保存
     * @param brand
     */
    void add(Brand brand);

    /**
     * 根据id查询
     * @param id
     * @return Brand
     */
    Brand findOne(Long id);

    /**
     * 更新
     * @param brand
     */
    void update(Brand brand);

    /**
     * 批量删除
     * @param ids
     */
    void del(Long[] ids);
}

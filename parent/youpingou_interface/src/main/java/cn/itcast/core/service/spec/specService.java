package cn.itcast.core.service.spec;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.vo.specificationVo;

public interface specService {

    /**
     * 分页查询
     * @return
     */
    PageResult findPage(Integer pageNum, Integer pageSize);

    /**
     * 条件查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    PageResult search(Integer page, Integer rows, Specification specification);

    /**
     * 新增规格
     * @param specificationVo
     * @return Result
     */
    void add(specificationVo specificationVo);

    /**
     * 修改回显
     * @param id
     * @return
     */
    specificationVo findOne(Long id);

    /**
     * 修改
     * @param specificationVo
     */
    void update(specificationVo specificationVo);

    /**
     * 批量删除
     * @param ids
     */
    void dele(Long[] ids);
}

package cn.itcast.core.service.tempalte;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

public interface Type_TemplateService {
    /**
     * 条件查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

    /**
     * 添加
     * @param typeTemplate
     */
    void add(TypeTemplate typeTemplate);
    //批量删除
    void delete(Long[] ids);
    //
    TypeTemplate findOne(Long id);
}

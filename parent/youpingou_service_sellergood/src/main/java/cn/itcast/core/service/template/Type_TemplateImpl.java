package cn.itcast.core.service.template;

import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.service.tempalte.Type_TemplateService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class Type_TemplateImpl implements Type_TemplateService {
    @Resource
    private TypeTemplateDao typeTemplateDao;

    /**
     * 分页条件查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        PageHelper.startPage(page,rows);
        TypeTemplateQuery query = new TypeTemplateQuery();
        if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName().trim())) {
            query.createCriteria().andNameLike("%"+typeTemplate.getName().trim()+"%");
        }
        query.setOrderByClause("id desc");
        Page<TypeTemplate> pag = (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);

        return new PageResult(pag.getTotal(),pag.getResult());
    }

    /**
     * 添加
     * @param typeTemplate
     */
    @Transactional
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }
}

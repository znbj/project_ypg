package cn.itcast.core.service.template;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.service.brand.BrandService;
import cn.itcast.core.service.tempalte.Type_TemplateService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class Type_TemplateImpl implements Type_TemplateService {
    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;
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

    /**
     * 批量删除
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            typeTemplateDao.deleteMany(ids);
        }
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public List<Map> findBySpecList(long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String brandIds = typeTemplate.getBrandIds();
        List<Map> list = JSON.parseArray(brandIds, Map.class);
        for (Map map : list) {
            String specId = map.get("id").toString();
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
            specificationOptionQuery.createCriteria().andSpecIdEqualTo(Long.parseLong(specId));
            List<SpecificationOption> options = specificationOptionDao.selectByExample(specificationOptionQuery);
            map.put("options",options);
        }

        return list;
    }
}

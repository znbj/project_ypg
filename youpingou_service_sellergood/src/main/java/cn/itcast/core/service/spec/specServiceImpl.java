package cn.itcast.core.service.spec;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.vo.specificationVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class specServiceImpl implements specService {

    @Resource
    SpecificationDao specificationDao;
    @Resource
    SpecificationOptionDao specificationOptionDao;
    /**
     * 分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page<Specification> page= (Page<Specification>) specificationDao.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 条件查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        SpecificationQuery specificationQuery = new SpecificationQuery();
        if (specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())) {
            SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
            criteria.andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        PageHelper.startPage(page,rows);
        specificationQuery.setOrderByClause("id desc");
        Page<Specification> pag= (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        //PageHelper.orderBy("id desc");
        return new PageResult(pag.getTotal(),pag.getResult());
    }

    /**
     *添加
     * @param specificationVo
     */
    @Transactional
    @Override
    public void add(specificationVo specificationVo) {
        Specification specification = specificationVo.getSpecification();
        //保存规格
        specificationDao.insertSelective(specification);
        //
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if (specificationOptionList != null && specificationOptionList.size() > 0) {
            //设置规格选项  规格id
            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specification.getId());
            }
            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    /**
     * 回显
     * @param id
     * @return
     */
    @Override
    public specificationVo findOne(Long id) {
        Specification specification = specificationDao.selectByPrimaryKey(id);
        SpecificationOptionQuery specificationOptionQuery=new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
        return new specificationVo(specification,specificationOptionList);
    }

    /**
     * 修改
     * @param specificationVo
     */
    @Transactional
    @Override
    public void update(specificationVo specificationVo) {
        Specification specification = specificationVo.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);
        //先删除规格项 再添加
        SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = optionQuery.createCriteria();
        criteria.andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(optionQuery);
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if (specificationOptionList != null && specificationOptionList.size() >0) {
            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specification.getId());
            }
        }
        specificationOptionDao.insertSelectives(specificationOptionList);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Transactional
    @Override
    public void dele(Long[] ids) {
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = optionQuery.createCriteria();
                criteria.andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(optionQuery);
                specificationDao.deleteByPrimaryKey(id);
            }
        }

    }


}

package cn.itcast.core.service.brand;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService{

    @Resource
    private BrandDao brandDao;
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    @Override
    public PageResult findPage(Integer pageNo, Integer pageSize) {
        //设置分页条件
        PageHelper.startPage(pageNo,pageSize);
        //查询
        Page<Brand> page= (Page<Brand>) brandDao.selectByExample(null);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public PageResult search(Integer pageNo, Integer pageSize, Brand brand) {
        //设置分页条件
        PageHelper.startPage(pageNo,pageSize);
        BrandQuery brandQuery=new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if (brand.getName() != null && !"".equals(brand.getName().trim())) {
            criteria.andNameLike("%"+brand.getName().trim()+"%");
        }
        if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())) {
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }
        //查询
        PageHelper.orderBy("id desc");
        Page<Brand> page= (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Transactional
    @Override
    public void del(Long[] ids) {
        if (ids != null && ids.length >0) {
            brandDao.deleteByPrimaryKeys(ids);
        }
    }

    @Override
    public List<Map<String,String>> selectOptionList() {
        return brandDao.selectOptionList();
    }
}

package cn.itcast.core.task;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class RedisTask {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;


    // 将商品分类同步到缓存中
    // cron：指定执行的时间  格式：种类非常多  年月日时分秒
    @Scheduled(cron = "00 45 20 * * ?")
    public void autoDBToRedisForItemCat(){
        List<ItemCat> list = itemCatDao.selectByExample(null);
        if(list != null && list.size() > 0){
            for (ItemCat itemCat : list) {
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
            }
        }
        System.out.println("定时器:更新项目表");
    }

    // 将商品品牌以及规格同步到缓存中
    @Scheduled(cron = "00 45 20 * * ?")
    public void autoDBToRedisForTemplate(){
        List<TypeTemplate> list = typeTemplateDao.selectByExample(null);
        if(list != null && list.size() > 0){
            for (TypeTemplate template : list) {
                // 将品牌同步到redis中
                String brandIds = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);
                // 将规格以及规格选项同步到redis中
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(), specList);
            }
        }
        System.out.println("定时器:更新规格,模板缓存");
    }

    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        // 获取到的规格结果集：json串
        // 栗子：[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();
        // 将json串转成对象
        List<Map> list = JSON.parseArray(specIds, Map.class);
        // 获取具体的规格选项
        for (Map map : list) {
            String specId = map.get("id").toString();
            // 获取对应的规格选项
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            query.createCriteria().andSpecIdEqualTo(Long.valueOf(specId));
            List<SpecificationOption> options = specificationOptionDao.selectByExample(query);
            map.put("options", options);
        }
        return list;
    }

}

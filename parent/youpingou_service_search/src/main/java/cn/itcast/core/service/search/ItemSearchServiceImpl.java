package cn.itcast.core.service.search;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Resource
    private ItemDao itemDao;
    /**
     * 前台系统的检索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        // 页面显示的数据：商品列表、分类列表、品牌列表、规格列表等
        Map<String, Object> resultMap = new HashMap<>();
        // 1、根据关键字检索并且分页
//        Map<String, Object> goodsMap = searchForPage(searchMap);
        // 1、根据关键字检索并且分页,而且关键字高亮显示
        Map<String, Object> goodsMap = searchForHighLightPage(searchMap);
        // 2、商品分类的列表
        List<String> categoryList = searchForGroupPage(searchMap);
        if(categoryList != null && categoryList.size() > 0){
            // 默认查询第一个分类下的品牌以及规格
            Map<String, Object> brandAndSpecMap = searchBrandAndSpecByCategory(categoryList.get(0));
            resultMap.putAll(brandAndSpecMap);
            resultMap.put("categoryList", categoryList);
        }
        resultMap.putAll(goodsMap);

        return resultMap;
    }

    @Override
    public void updateSolr(long id) {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1").andIsDefaultEqualTo("1")
                .andGoodsIdEqualTo(id);

        List<Item> itemList = itemDao.selectByExample(itemQuery);
        for (Item item : itemList) {
            String spec = item.getSpec();
            Map map = JSON.parseObject(spec, Map.class);
            item.setMap(map);
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleteItemFromSolr(Long id) {
        SimpleQuery query = new SimpleQuery("item_goodsid:" + id);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //    // 默认查询第一个分类下的品牌以及规格
    private Map<String,Object> searchBrandAndSpecByCategory(String category) {
        // 根据分类获取模板id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(category);
        // 通过模板id获取对应的品牌
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        // 通过模板id获取对应的规格以及规格选项
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        // 将数据封装到map中
        Map<String, Object> brandAndSpecMap = new HashMap<>();
        brandAndSpecMap.put("brandList", brandList);
        brandAndSpecMap.put("specList", specList);
        return brandAndSpecMap;
    }


    // 商品分类的列表
    private List<String> searchForGroupPage(Map<String, String> searchMap) {
        // 封装查询条件
        Criteria criteria = new Criteria("item_keywords");
        String keywords = searchMap.get("keywords");
        if (keywords != null && !"".equals(keywords)) {
            criteria.is(keywords);
        }
        SimpleQuery query = new SimpleQuery(criteria);
        // 封装分组条件
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        // 分组查询
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        // 将数据封装到集合中
        List<String> categoryList = new ArrayList<>();
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String value = groupEntry.getGroupValue();
            categoryList.add(value);
        }
        return categoryList;
    }

    // 根据关键字检索并且分页,而且关键字高亮显示
    private Map<String, Object> searchForHighLightPage(Map<String, String> searchMap) {

        // 封装检索条件
        Criteria criteria = new Criteria("item_keywords");
        String keywords = searchMap.get("keywords");
        //处理关键字
        if (keywords != null && !"".equals(keywords)) {
            //去空格
            String replace = keywords.replace(" ", "");
            searchMap.put("keywords", replace);

        }
        if (keywords != null && !"".equals(keywords)) {
            criteria.is(keywords);
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);
        // 封装分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);  // 其始行
        query.setRows(pageSize);    // 每页显示的条数
        // 关键字高亮显示
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);


        //根据商品过滤
        String category = searchMap.get("category");
        if (category != null && !"".equals(category)) {
            Criteria cri = new Criteria(category);
            cri.is(category);
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(simpleFilterQuery);
        }
        //根据品牌价格 过滤
        String brand = searchMap.get("brand");
        if (brand != null && !"".equals(brand)) {
            Criteria cri = new Criteria("item_brand");
            cri.is(brand);
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(simpleFilterQuery);
        }

        String price = searchMap.get("price");
        if (price != null && !"".equals(price)) {
            String[] split = price.split("-");
            Criteria cri = new Criteria("item_price");
            cri.between(split[0], split[1], true, false);
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(simpleFilterQuery);
        }
        //根据分类过滤
        String spec = searchMap.get("spec");
        if (spec != null && !"".equals(spec)) {
            Map<String,String> map = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                Criteria cri = new Criteria("item_spec_" + entry.getKey());
                cri.is(entry.getValue());
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(simpleFilterQuery);
            }
        }

        // 根据条件查询
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);
        // 取出高亮的结果
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if(highlighted != null && highlighted.size() > 0){
            for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
                Item item = itemHighlightEntry.getEntity(); // 普通结果
                List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
                if(highlights != null && highlights.size() > 0){
                    String title = highlights.get(0).getSnipplets().get(0);
                    item.setTitle(title);
                }
            }
        }
        // 将数据封装到map中
        Map<String, Object> goodsMap = new HashMap<>();
        goodsMap.put("totalPages", highlightPage.getTotalPages()); // 总页数
        goodsMap.put("total", highlightPage.getTotalElements());   // 总条数
        goodsMap.put("rows", highlightPage.getContent());          // 结果集
        return goodsMap;
    }

    // 根据关键字检索并且分页
    private Map<String, Object> searchForPage(Map<String, String> searchMap) {
        // 封装检索条件
        Criteria criteria = new Criteria("item_keywords");
        String keywords = searchMap.get("keywords");
        if (keywords != null && !"".equals(keywords)) {
            criteria.is(keywords);
        }
        SimpleQuery query = new SimpleQuery(criteria);
        // 封装分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);  // 其始行
        query.setRows(pageSize);    // 每页显示的条数

        // 根据条件查询
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);

        // 将数据封装到map中
        Map<String, Object> goodsMap = new HashMap<>();
        goodsMap.put("totalPages", scoredPage.getTotalPages()); // 总页数
        goodsMap.put("total", scoredPage.getTotalElements());   // 总条数
        goodsMap.put("rows", scoredPage.getContent());          // 结果集
        return goodsMap;
    }


}

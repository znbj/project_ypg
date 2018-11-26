package cn.itcast.core.service.search;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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

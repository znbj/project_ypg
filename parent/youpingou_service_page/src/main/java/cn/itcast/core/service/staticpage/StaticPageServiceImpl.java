package cn.itcast.core.service.staticpage;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StaticPageServiceImpl implements StaticPageService, ServletContextAware {


    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;
    @Resource
    private ItemDao itemDao;
    @Resource
    private ItemCatDao itemCatDao;



    private ServletContext servletContext;


    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    private Configuration configuration;

    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.configuration = freeMarkerConfigurer.getConfiguration();
    }

    @Override
    public void getHtml(Long id) {

        try {
            Template template = configuration.getTemplate("item.ftl");
            Map<String, Object> dataModel = getDataModel(id);
            String pathName = "/" + id + ".html";
            //todo ????????
            String path = servletContext.getRealPath(pathName);
            File file = new File(path);
            Writer out = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
            template.process(dataModel,out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<String, Object> getDataModel(Long id) {
        Map<String, Object> dataModel = new HashMap<>();
        Goods goods = goodsDao.selectByPrimaryKey(id);
        dataModel.put("goods", goods);
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        dataModel.put("goodsDesc", goodsDesc);
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        dataModel.put("itemCat1", itemCat1);
        dataModel.put("itemCat2", itemCat2);
        dataModel.put("itemCat3", itemCat3);
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id).andNumGreaterThan(0);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        dataModel.put("itemList", itemList);
        return dataModel;
    }


}

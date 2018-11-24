package cn.itcast.core.service.goods;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;
    @Resource
    private ItemDao itemDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private BrandDao brandDao;

    @Resource
    private SolrTemplate solrTemplate;
    /**
     * 保存商品
     * @param goodsVo
     */
    @Transactional
    @Override
    public void add(GoodsVo goodsVo) {
        Goods goods = goodsVo.getGoods();
        //保存商品
        goods.setAuditStatus("0");
        goodsDao.insertSelective(goods);
        //保存商品明细 tb_goods_desc
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());
        goodsDescDao.insertSelective(goodsDesc);
        //保存库存
        if ("1".equals(goods.getIsEnableSpec())) {
            //一对多
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                String title = goods.getGoodsName();
                Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entrySet = map.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    title += "" + entry.getValue();
                }
                item.setTitle(title);
                //定义共同方法
                setAttributeForItem(goods,goodsDesc,item);
                itemDao.insertSelective(item);
            }
        } else {
            //一对一
            Item item = new Item();
            item.setTitle(goods.getGoodsName());
            item.setPrice(goods.getPrice());
            item.setNum(9999);
            item.setIsDefault("1");
            item.setSpec("{}");
            //
            setAttributeForItem(goods,goodsDesc,item);
            itemDao.insertSelective(item);

        }

    }

    /**
     * 修改商品回显
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(itemQuery);
        goodsVo.setItemList(items);
        return goodsVo;
    }

    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName().trim())) {
            goodsQuery.createCriteria().andGoodsNameLike("%"+goods.getGoodsName().trim()+"%");
        }
        goodsQuery.setOrderByClause("id desc");
        Page<Goods> pag= (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new  PageResult(pag.getTotal(),pag.getResult());
    }
    @Transactional
    @Override
    public void update(GoodsVo goodsVo) {
        Goods goods = goodsVo.getGoods();
        goodsDao.updateByPrimaryKeySelective(goods);
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(itemQuery);
        //保存库存
        if ("1".equals(goods.getIsEnableSpec())) {
            //一对多
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                String title = goods.getGoodsName();
                Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entrySet = map.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    title += "" + entry.getValue();
                }
                item.setTitle(title);
                //定义共同方法
                setAttributeForItem(goods,goodsDesc,item);
                itemDao.insertSelective(item);
            }
        } else {
            //一对一
            Item item = new Item();
            item.setTitle(goods.getGoodsName()+ " " + goods.getCaption());
            item.setPrice(goods.getPrice());
            item.setStatus("1"); // 可用的状态
            item.setNum(9999);
            item.setIsDefault("1");
            item.setSpec("{}");
            //
            setAttributeForItem(goods,goodsDesc,item);
            itemDao.insertSelective(item);

        }


    }

    @Override
    public PageResult searchForManager(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus().trim())) {
            goodsQuery.createCriteria().andIsDeleteIsNull();
        }
        goodsQuery.setOrderByClause("id desc");
        Page<Goods> pag= (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new  PageResult(pag.getTotal(),pag.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null && ids.length > 0) {
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)) {
                    // TODO:将商品保存到索引库
                    dataImportToSolr();
                    // TODO:生成商品详情的静态页

                }
            }
        }
    }

    private void dataImportToSolr() {
        List<Item> itemList = itemDao.selectByExample(null);
        if (itemList != null && itemList.size() > 0) {
            for (Item item : itemList) {
                String spec = item.getSpec();
                Map<String,String> map = JSON.parseObject(spec, Map.class);
                item.setMap(map);
            }
            solrTemplate.saveBeans(itemList);
            solrTemplate.commit();
        }
    }

    @Override
    public void delete(Long[] ids, String status) {
        if (ids != null && ids.length > 0) {
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)) {
                    // TODO:将商品保存到索引库
                    // TODO:生成商品详情的静态页

                }
            }
        }
    }


    private void setAttributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        // 商品图片：
        // 测试数据：[{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},
        // {"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
        String itemImages = goodsDesc.getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        if(images != null && images.size() > 0){
            String image = images.get(0).get("url").toString();
            item.setImage(image); // 商品图片
        }
        item.setCategoryid(goods.getCategory3Id()); // 商品的三级分类id
        item.setCreateTime(new Date()); // 创建日期
        item.setUpdateTime(new Date()); // 更新日期
        item.setGoodsId(goods.getId()); // 商品id
        item.setSellerId(goods.getSellerId()); // 商家id
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName()); // 分类名称
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());   // 品牌名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName()); // 店铺名称
    }
}

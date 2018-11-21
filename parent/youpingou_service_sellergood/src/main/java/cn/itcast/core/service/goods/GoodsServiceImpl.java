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
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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

    @Override
    public Goods findOne(Long id) {
        return goodsDao.selectByPrimaryKey(id);
    }

    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        goodsQuery.setOrderByClause("id desc");
        if (goods.getSellerId() != null && !"".equals(goods.getGoodsName().trim())) {
            goodsQuery.createCriteria().andSellerIdEqualTo(goods.getGoodsName().trim());
        }
        Page<Goods> pag= (Page<Goods>) goodsDao.selectByExample(goodsQuery);

        return new  PageResult(pag.getTotal(),pag.getResult());
    }


    private void setAttributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        List<Map> list = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
        if (list != null && list.size() >0) {
            String image = list.get(0).get("url").toString();
            item.setImage(image);
        }
        item.setCategoryid(goods.getCategory3Id());
        item.setStatus("1");
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(goods.getId());
        String sellerId = goods.getSellerId();
        item.setSellerId(sellerId);
        item.setSeller(sellerDao.selectByPrimaryKey(sellerId).getNickName());
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());

    }
}

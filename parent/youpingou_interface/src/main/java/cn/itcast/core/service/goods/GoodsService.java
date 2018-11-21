package cn.itcast.core.service.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.vo.GoodsVo;

public interface GoodsService {
    /**
     * 新增商品
     * @param goodsVo
     */
    void add(GoodsVo goodsVo);

    GoodsVo findOne(Long id);

    PageResult search(Integer page, Integer rows, Goods goods);

    void update(GoodsVo goodsVo);
    //运营商
    PageResult searchForManager(Integer page, Integer rows, Goods goods);
    //运营商
    void updateStatus(Long[] ids, String status);

    void delete(Long[] ids,String status);
}

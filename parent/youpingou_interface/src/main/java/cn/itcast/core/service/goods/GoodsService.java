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

    Goods findOne(Long id);

    PageResult search(Integer page, Integer rows, Goods goods);
}

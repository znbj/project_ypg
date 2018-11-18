package cn.itcast.core.service.itemCat;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    /**
     * 商品分类列表查询
     * @param parentId
     * @return
     */
    List<ItemCat> findByParentId(Long parentId);
}

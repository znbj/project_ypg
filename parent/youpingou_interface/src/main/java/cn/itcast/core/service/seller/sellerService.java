package cn.itcast.core.service.seller;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.seller.Seller;

public interface sellerService {
    /**
     * 商家入驻
     * @param seller
     */
    void add(Seller seller);

    /**
     *
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    PageResult search(Integer page,Integer rows,Seller seller);

    /**
     *
     * @param id
     * @return
     */
    Seller findOne(String id);

    void updateStatus(String sellerId,String status);
}

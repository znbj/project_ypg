package cn.itcast.core.service.seller;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class SellerServiceImpl implements SellerService{
    @Resource
    private SellerDao sellerDao;

    /**
     * 商家入驻
     * @param seller
     */
    @Transactional
    @Override
    public void add(Seller seller) {
        //设置审核状态 0 ; 密码加密
        seller.setStatus("0");
        String password = seller.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
        seller.setPassword(bCryptPasswordEncoder.encode(password));
        sellerDao.insertSelective(seller);
    }

    /**
     *
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page,rows);
        SellerQuery sellerQuery = new SellerQuery();
        if (seller.getStatus() != null && !"".equals(seller.getStatus().trim())) {
            sellerQuery.createCriteria().andStatusEqualTo(seller.getStatus().trim());
        }
        Page<Seller> pag= (Page<Seller>) sellerDao.selectByExample(sellerQuery);
        return new PageResult(pag.getTotal(),pag.getResult());
    }

    /**
     * 运营商设置商家状态回显
     * @param id
     * @return
     */
    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }
    @Transactional
    @Override
    public void updateStatus(String sellerId,String status) {
        Seller seller = new Seller();
        seller.setStatus(status);
        seller.setSellerId(sellerId);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
